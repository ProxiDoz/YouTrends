package system.access;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import system.shared.User;
import system.shared.UserSettingsData;

public class UserDAO
{
    private static final Logger logger = LogManager.getLogger(UserDAO.class);

    private JdbcTemplate jdbcTemplate;

    private static UserDAO instance;

    public UserDAO()
    {
        instance = this;
    }

    public static UserDAO getInstance()
    {
        return instance;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public String registerUserIfNotExist(User user)
    {
        String password = RandomStringUtils.random(10, "zvnjfsqw1234567890");

        String query = "SELECT * FROM Users WHERE id = ?";
        String insertQuery = "INSERT INTO Users (id, firstName, lastName, userName, language, isBot, password) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                             "ON CONFLICT (id) DO UPDATE SET " +
                             "firstName = ?," +
                             "lastName =?," +
                             "userName = ?," +
                             "language = ?";

        try
        {
            StringBuilder stringBuilder = new StringBuilder();

            jdbcTemplate.query(query, result ->
            {
                String pas = result.getString("password");
                if (pas != null)
                {
                    stringBuilder.append(pas);
                }
            }, user.getId());

            if (stringBuilder.length() == 0)
            {
                jdbcTemplate.update(insertQuery,
                                    user.getId(),
                                    user.getFirstName(),
                                    user.getLastName(),
                                    user.getUserName(),
                                    user.getLanguageCode(),
                                    user.getBot(),
                                    password,

                                    user.getFirstName(),
                                    user.getLastName(),
                                    user.getUserName(),
                                    user.getLanguageCode()
                                    );
                return password;
            }
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        return null;
    }

    public void subscribeUser(User user)
    {
        String updateQuery = "UPDATE Users SET isSubscribe = TRUE WHERE id = ?";

        try
        {
            jdbcTemplate.update(updateQuery, user.getId());
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        logger.info("Subscription: " + user.getId());
    }

    public void unsubscribeUser(User user)
    {
        String updateQuery = "UPDATE Users SET isSubscribe = FALSE WHERE id = ?";

        try
        {
            jdbcTemplate.update(updateQuery, user.getId());
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        logger.info("Unsubscribing: " + user.getId());
    }

    public List<User> getSubscribeUsers()
    {
        List<User> users = new ArrayList<>();

        String query = "SELECT * FROM Users WHERE isSubscribe = TRUE";

        try
        {
            jdbcTemplate.query(query, result ->
            {
                User user = new User();

                user.setId(result.getInt("id"));
                user.setFirstName(result.getString("firstName"));
                user.setLastName(result.getString("lastName"));
                user.setUserName(result.getString("userName"));
                user.setLanguageCode(result.getString("language"));
                user.setBot(result.getBoolean("isBot"));
                user.setBanned(result.getBoolean("isBanned"));
                user.setSubscribe(result.getBoolean("isSubscribe"));

                users.add(user);
            });

            return users;
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        return users;
    }

    public boolean checkCredential(String chatId, String password)
    {
        String query = "SELECT password FROM Users WHERE id = ?";

        StringBuilder passwordFromDB = new StringBuilder();

        try
        {
            jdbcTemplate.query(query, result ->
            {
                passwordFromDB.append(result.getString("password"));
            }, Long.valueOf(chatId));

            if (!passwordFromDB.toString().isEmpty())
            {
                return passwordFromDB.toString().equals(password);
            }
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        return false;
    }

    public UserSettingsData getUserSettingsData(String chatId)
    {
        // TODO: move getBannedChannelsByUser and getBannedTagsByUser to bannedChannelsDAO and bannedTagsDAO accordingly
        List<String> bannedChannels = getBannedChannelsByUser(chatId);
        List<String> bannedTags = getBannedTagsByUser(chatId);

        UserSettingsData userSettingsData = new UserSettingsData();
        userSettingsData.setBannedChannels(bannedChannels);
        userSettingsData.setBannedTags(bannedTags);

        return userSettingsData;
    }


    public UserSettingsData setUserSettingData(UserSettingsData userSettingData)
    {
        String login = userSettingData.getCredentials().getLogin();

        insertBannedChannels(userSettingData.getBannedChannels());
        insertBannedTags(userSettingData.getBannedTags());

        String deleteChannelsQuery = "DELETE FROM UserBannedChannel WHERE userId = ?";
        String deleteTagsQuery = "DELETE FROM UserBannedTag WHERE userId = ?";


        String insertChannelQuery = "INSERT INTO UserBannedChannel (userId, channelId) " +
                                    "VALUES (?,?)";

        String insertTagsQuery = "INSERT INTO UserBannedTag (userId, tagId) " +
                                 "VALUES (?,?)";

        try
        {
            userSettingData.setBannedChannels(userSettingData.getBannedChannels().stream().distinct().collect(Collectors.toList()));
            userSettingData.setBannedTags(userSettingData.getBannedTags().stream().distinct().collect(Collectors.toList()));

            jdbcTemplate.update(deleteChannelsQuery, Long.valueOf(login));
            jdbcTemplate.update(deleteTagsQuery, Long.valueOf(login));

            for (String bannedChannel : userSettingData.getBannedChannels())
            {
                if (bannedChannel != null && !bannedChannel.isEmpty() && bannedChannel.length() < 64 && !bannedChannel.equals(
                        " "))
                {
                    jdbcTemplate.update(insertChannelQuery, Long.valueOf(login), bannedChannel);
                }
            }

            for (String bannedTag : userSettingData.getBannedTags())
            {
                if (bannedTag != null && !bannedTag.isEmpty() && bannedTag.length() < 64 && !bannedTag.equals(" "))
                {
                    jdbcTemplate.update(insertTagsQuery, Long.valueOf(login), bannedTag);
                }
            }

            return getUserSettingsData(login);
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        return null;
    }

    private List<String> getBannedChannelsByUser(String chatId)
    {
        String query = "SELECT channelId FROM UserBannedChannel WHERE userId = ?";

        List<String> bannedChannels = new ArrayList<>();

        try
        {
            jdbcTemplate.query(query, result ->
            {
                bannedChannels.add(result.getString("channelId"));
            }, Long.valueOf(chatId));
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        return bannedChannels;
    }

    private List<String> getBannedTagsByUser(String chatId)
    {
        String query = "SELECT tagId FROM UserBannedTag WHERE userId = ?";

        List<String> bannedTags = new ArrayList<>();

        try
        {
            jdbcTemplate.query(query, result ->
            {
                bannedTags.add(result.getString("tagId"));
            }, Long.valueOf(chatId));
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        return bannedTags;
    }

    private void insertBannedChannels(List<String> bannedChannels)
    {
        String insertQuery = "INSERT INTO BannedChannel (name) " +
                             "VALUES (?)" +
                             "ON CONFLICT DO NOTHING";

        multipleInsert(insertQuery, bannedChannels);
    }

    private void insertBannedTags(List<String> bannedTags)
    {
        String insertQuery = "INSERT INTO BannedTag (name) " +
                             "VALUES (?)" +
                             "ON CONFLICT DO NOTHING";

        multipleInsert(insertQuery, bannedTags);
    }

    private void multipleInsert(String query, List<String> values)
    {
        try
        {
            for (String value : values)
            {
                if (value != null && !value.isEmpty() && value.length() < 64 && !value.equals(" "))
                {
                    jdbcTemplate.update(query, value);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }
    }

    public String getUserPassword(String chatId)
    {
        String query = "SELECT password FROM Users WHERE id = ?";

        StringBuilder passwordFromDB = new StringBuilder();

        try
        {
            jdbcTemplate.query(query, result ->
            {
                passwordFromDB.append(result.getString("password"));
            }, Long.valueOf(chatId));

            return passwordFromDB.toString();
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        return null;
    }
}
