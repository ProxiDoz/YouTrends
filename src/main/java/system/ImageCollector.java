package system;

import java.net.URL;

import javax.imageio.ImageIO;


public class ImageCollector
{
    void collectImages(Feed feed)
    {
        for (Video video : feed.getVideos())
        {
            try
            {
                URL url = new URL(video.getImgUrl());

                video.setImage(ImageIO.read(url));
            }
            catch (Exception e)
            {
                MyLogger.logErr("collectImages error");
            }
        }
    }
}
