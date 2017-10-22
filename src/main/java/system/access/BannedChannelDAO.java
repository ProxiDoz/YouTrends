package system.access;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import system.MyLogger;

public class BannedChannelDAO
{
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

    public List<String> getBannedChannels()
    {
        List<String> bannedChannels = new ArrayList<>();

        String query = "SELECT * FROM bannedchannel";

        try
        {
            jdbcTemplate.query(query, result ->
            {
                bannedChannels.add(result.getString("name"));
            });

            return bannedChannels;
        }
        catch (Exception e)
        {
            MyLogger.logErr("getBannedChannels error");
            e.printStackTrace();
        }

        return bannedChannels;
    }
}
