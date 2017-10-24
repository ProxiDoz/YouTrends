package system;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FeedParserRunnable implements Runnable
{
    private final ImageCollector imageCollector = new ImageCollector();
    private final Telegram telegram;

    public FeedParserRunnable(Telegram telegram)
    {
        this.telegram = telegram;
    }

    @Override
    public void run()
    {
        try
        {
            MyLogger.logInfo("Start feed collect");
            ScheduledFuture<Feed> future = Executors.newSingleThreadScheduledExecutor()
                                                    .schedule(new YouTubeParser(),
                                                              0,
                                                              TimeUnit.MICROSECONDS);

            Feed feed = future.get();
            MyLogger.logInfo("Feed was collect. Feed size: " + feed.getVideos().size());

            MyLogger.logInfo("Start filtration feed");
            List<Video> filteredVideos = feed.filtration();

            Feed filteredFeed = new Feed();
            filteredFeed.setVideos(filteredVideos);

            MyLogger.logInfo("Feed size after filtration: " + filteredFeed.getVideos().size());
            MyLogger.logInfo("Start collect images");
            imageCollector.collectImages(filteredFeed);

            LastFeedContainer.setFeed(filteredFeed);
        }
        catch (Exception e)
        {
            MyLogger.logErr("EveryDayFeedDispatcherRunnable error");
            e.printStackTrace();
        }
    }
}
