package system.access;

import java.util.List;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import system.shared.Video;

public class VideoDAO extends AbstractDAO
{
    private static final Logger logger = LogManager.getLogger(VideoDAO.class);

    public VideoDAO(DataSource dataSource)
    {
        super(dataSource);
    }

    public void insertVideos(List<Video> videos)
    {
        videos.forEach(this::insertVideo);
    }

    public void insertVideo(Video video)
    {
        String query = "INSERT INTO Video (videoId, title, description, channel, imgUrl, old, viewCount, date) " +
                       "VALUES (?,?,?,?,?,?,?, now())";

        try
        {
            jdbcTemplate.update(query,
                                video.getId(),
                                video.getTitle(),
                                video.getDescription(),
                                video.getChannel(),
                                video.getImgUrl(),
                                video.getOld(),
                                video.getViewCount());
        }
        catch (Exception e)
        {
            logger.error("Insert video error");
        }
    }
}
