package system;

import java.util.List;

import system.shared.Feed;
import system.shared.Video;

public class LastFeedContainer
{
    private static Feed feed;

    public static List<Video> getVideosForUser(String chatId)
    {
        return feed.filtration(chatId);
    }

    public static void setFeed(Feed feed)
    {
        LastFeedContainer.feed = feed;
    }
}
