package system.access;

import java.util.List;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import system.Video;

public class VideoDAO
{
    private static final Logger logger = LogManager.getLogger(VideoDAO.class);

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insertVideos(List<Video> videos, long feedId)
    {
        String insertQuery = "INSERT INTO Video (feedId, name, description, channel, videoId, imageUrl) " +
                             "VALUES (?,?,?,?,?,?)";

        try
        {
            for (Video video : videos)
            {
                jdbcTemplate.update(insertQuery,
                                    feedId,
                                    video.getName(),
                                    video.getDescription(),
                                    video.getChannel(),
                                    video.getId(),
                                    video.getImgUrl());
            }
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }
    }
}
