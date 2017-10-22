package system.access;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import system.MyLogger;

public class BannedTagDAO
{
    private JdbcTemplate jdbcTemplate;

    private static BannedTagDAO instance;

    public BannedTagDAO()
    {
        instance = this;
    }

    public static BannedTagDAO getInstance()
    {
        return instance;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<String> getBannedTags()
    {
        List<String> bannedTags = new ArrayList<>();

        String query = "SELECT * FROM BannedTag";

        try
        {
            jdbcTemplate.query(query, result ->
            {
                bannedTags.add(result.getString("name"));
            });

            return bannedTags;
        }
        catch (Exception e)
        {
            MyLogger.logErr("getBannedTags error");
            e.printStackTrace();
        }

        return bannedTags;
    }
}
