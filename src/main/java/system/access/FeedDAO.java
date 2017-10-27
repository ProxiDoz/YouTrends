package system.access;

import java.sql.Timestamp;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import system.Feed;

public class FeedDAO
{
    private static final Logger logger = LogManager.getLogger(FeedDAO.class);

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public long insertFeed(Feed feed)
    {
        String insertQuery = "INSERT INTO Feed (dateCollect) VALUES (?)";

        String getIdQuery = "SELECT LAST_INSERT_ID()";

        try
        {
            jdbcTemplate.update(insertQuery, new Timestamp(System.currentTimeMillis()));

            class Long2
            {
                public long id;
            }

            Long2 id = new Long2();

            jdbcTemplate.query(getIdQuery,
                               result ->
                               {
                                   id.id = result.getLong(0);
                               });

            return id.id;
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        return -1;
    }
}
