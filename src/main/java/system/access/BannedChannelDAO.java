package system.access;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import system.shared.User;

public class BannedChannelDAO
{
    private static final Logger logger = LogManager.getLogger(BannedChannelDAO.class);

    private JdbcTemplate jdbcTemplate;

    private static BannedChannelDAO instance;

    public BannedChannelDAO()
    {
        instance = this;
    }

    public static BannedChannelDAO getInstance()
    {
        return instance;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<String> getBannedChannels(String userId)
    {
        List<String> bannedChannels = new ArrayList<>();

        String query = "SELECT name FROM BannedChannel, UserBannedChannel " +
                       "WHERE UserBannedChannel.channelId = BannedChannel.name AND " +
                       "UserBannedChannel.userId = ?";

        try
        {
            jdbcTemplate.query(query, result ->
            {
                bannedChannels.add(result.getString("name"));
            }, Long.valueOf(userId));

            return bannedChannels;
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        return bannedChannels;
    }

    public boolean add(User user, String channel)
    {
        List<String> bannedChannels = getBannedChannels(user.getId().toString());

        if (!bannedChannels.contains(channel))
        {
            String insertBannedChannelQuery = "INSERT INTO BannedChannel (name) VALUES (?) ON CONFLICT DO NOTHING";
            String insertUserBannedChannelQuery = "INSERT INTO UserBannedChannel (userid, channelid) VALUES (?, ?) " +
                                                  "ON CONFLICT DO NOTHING";

            jdbcTemplate.update(insertBannedChannelQuery, channel);
            jdbcTemplate.update(insertUserBannedChannelQuery, user.getId(), channel);

            return true;
        }

        return false;
    }

    public boolean remove(User user, String channel)
    {
        String removeUserBannedChannelQuery = "DELETE FROM UserBannedChannel WHERE userid = ? AND channelid = ? ";

        int affectedRows = jdbcTemplate.update(removeUserBannedChannelQuery, user.getId(), channel);

        return affectedRows == 1;
    }

}
