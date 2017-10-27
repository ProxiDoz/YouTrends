package system.access;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

public class BannedTagDAO
{
    private static final Logger logger = LogManager.getLogger(BannedTagDAO.class);

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

    public List<String> getBannedTags(String chatId)
    {
        List<String> bannedTags = new ArrayList<>();

        String query = "SELECT name FROM BannedTag, UserBannedTag " +
                       "WHERE UserBannedTag.tagId = BannedTag.name AND " +
                       "UserBannedTag.userId = ?";

        try
        {
            jdbcTemplate.query(query, result ->
            {
                bannedTags.add(result.getString("name"));
            }, chatId);

            return bannedTags;
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        return bannedTags;
    }
}
