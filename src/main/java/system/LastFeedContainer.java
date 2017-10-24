package system;

public class LastFeedContainer
{
    private static Feed feed;

    public static Feed getFeed()
    {
        return feed;
    }

    public static void setFeed(Feed feed)
    {
        LastFeedContainer.feed = feed;
    }
}
