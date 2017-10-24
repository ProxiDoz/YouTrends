package system.access;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import system.MyLogger;
import system.User;

public class UserDAO
{
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

    public void registerUserIfNotExist(User user)
    {
        String insertQuery = "INSERT INTO User (id, firstName, lastName, userName, language, isBot) " +
                             "VALUES (?, ?, ?, ?, ?, ?)" +
                             "ON DUPLICATE KEY UPDATE " +
                             "firstName = VALUES(firstName)," +
                             "lastName = VALUES(lastName)," +
                             "userName = VALUES(userName)," +
                             "language = VALUES(language)," +
                             "isBot = VALUES(isBot)";

        try
        {
            jdbcTemplate.update(insertQuery,
                                user.getId(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getUserName(),
                                user.getLanguageCode(),
                                user.getBot());
        }
        catch (Exception e)
        {
            MyLogger.logErr("registerUserIfNotExist error");
            e.printStackTrace();
        }

        MyLogger.logWarn("New user: " + user.getId() + " " + user.getFirstName());
    }

    public void subscribeUser(User user)
    {
        String updateQuery = "UPDATE User SET isSubscribe = 1 WHERE id = ?";

        try
        {
            jdbcTemplate.update(updateQuery, user.getId().toString());
        }
        catch (Exception e)
        {
            MyLogger.logErr("subscribeUser error");
            e.printStackTrace();
        }

        MyLogger.logWarn("Subscribe. User: " + user.getId().toString());
    }

    public void unsubscribeUser(User user)
    {
        String updateQuery = "UPDATE User SET isSubscribe = 0 WHERE id = ?";

        try
        {
            jdbcTemplate.update(updateQuery, user.getId().toString());
        }
        catch (Exception e)
        {
            MyLogger.logErr("unsubscribeUser error");
            e.printStackTrace();
        }

        MyLogger.logWarn("Unubscribe. User: " + user.getId().toString());
    }

    public List<User> getSubscribeUsers()
    {
        List<User> users = new ArrayList<>();

        String query = "SELECT * FROM User WHERE isSubscribe = 1";

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
            MyLogger.logErr("getSubscribeUsers error");
            e.printStackTrace();
        }

        return users;
    }
}
