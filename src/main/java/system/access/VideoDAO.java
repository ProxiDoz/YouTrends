package system.access;

import java.util.List;
import javax.sql.DataSource;

import com.google.common.collect.Lists;
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

    public List<String> getUniqueTitlesByLastWeek()
    {
        String query = "SELECT DISTINCT title FROM Video WHERE date > (now() - INTERVAL '1 week')";

        List<String> titles = Lists.newArrayList();

        try
        {
            jdbcTemplate.query(query,
                               rs ->
                               {
                                   titles.add(rs.getString("title"));
                               }
                              );
        }
        catch (Exception e)
        {
            logger.error("Insert video error");
        }

        return titles;
    }
}
