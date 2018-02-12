package system;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import system.access.BannedChannelDAO;
import system.access.BannedTagDAO;
import system.shared.Feed;
import system.shared.Video;

public class LastFeedContainer
{
    private Feed feed;
    private FeedHandler feedHandler = new FeedHandler();

    @Autowired
    private BannedChannelDAO bannedChannelDAO;

    @Autowired
    private BannedTagDAO bannedTagDAO;

    public List<Video> getVideosForUser(String userId)
    {
        return feedHandler.filtration(feed,
                                      bannedChannelDAO.getBannedChannels(userId),
                                      bannedTagDAO.getBannedTags(userId));
    }

    public void setFeed(Feed feed)
    {
        this.feed = feed;
    }
}
