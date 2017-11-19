package system.access;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

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

    public List<String> getBannedChannels(String chatId)
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
            }, Long.valueOf(chatId));

            return bannedChannels;
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        return bannedChannels;
    }
}
