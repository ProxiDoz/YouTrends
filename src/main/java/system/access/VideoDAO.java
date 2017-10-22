package system.access;

import java.sql.Timestamp;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import system.MyLogger;
import system.Video;

public class VideoDAO
{
    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insertVideos(List<Video> videos, long feedId)
    {
        String insertQuery = "INSERT INTO video (feedId, name, description, channel, videoId, imageUrl) " +
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
            MyLogger.logErr("InsertVideos error");
            e.printStackTrace();
        }
    }
}
