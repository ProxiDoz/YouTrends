package system;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EveryDayFeedDispatcherRunnable implements Runnable
{
    private final ImageCollector imageCollector = new ImageCollector();
    private final Telegram telegram;

    public EveryDayFeedDispatcherRunnable(Telegram telegram)
    {
        this.telegram = telegram;
    }

    @Override
    public void run()
    {
        try
        {
            MyLogger.logInfo("Start every day feed collect");
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

            MyLogger.logInfo("Start send feed");
            telegram.sendFeed(filteredFeed);
        }
        catch (Exception e)
        {
            MyLogger.logErr("EveryDayFeedDispatcherRunnable error");
            e.printStackTrace();
        }
    }
}
