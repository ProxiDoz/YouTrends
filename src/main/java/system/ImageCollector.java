package system;

import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import system.shared.Feed;
import system.shared.Video;

public class ImageCollector
{
    private static final Logger logger = LogManager.getLogger(ImageCollector.class);

    void collectImages(Feed feed)
    {
        for (Video video : feed.getVideos())
        {
            try
            {
                URL url = new URL(video.getImgUrl());

                video.setImage(ImageIO.read(url));
            }
            catch (MalformedURLException e)
            {
                logger.warn("collect image error on video {}", video.getTitle());
            }
            catch (Exception e)
            {
                logger.error("Error", e);
            }
        }
    }
}
