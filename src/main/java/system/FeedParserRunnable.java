package system;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import system.parser.YouTubeParser;
import system.shared.Feed;

public class FeedParserRunnable implements Runnable
{
    private static final Logger logger = LogManager.getLogger(FeedParserRunnable.class);

    private final ImageCollector imageCollector = new ImageCollector();

    @Override
    public void run()
    {
        try
        {
            logger.info("Start feed collect");
            ScheduledFuture<Feed> future = Executors.newSingleThreadScheduledExecutor()
                                                    .schedule(new YouTubeParser(),
                                                              0,
                                                              TimeUnit.MICROSECONDS);

            Feed feed = future.get();
            logger.info("Feed was collect. Feed size: {}", feed.getVideos().size());
            logger.info("Start collect images");
            imageCollector.collectImages(feed);
            LastFeedContainer.setFeed(feed);
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }
    }
}
