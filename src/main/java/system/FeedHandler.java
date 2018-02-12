package system;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import system.shared.Feed;
import system.shared.Video;

public class FeedHandler
{
    private static final Logger logger = LogManager.getLogger(FeedHandler.class);

    private List<String> bannedChannels;
    private List<String> bannedTags;

    public List<Video> filtration(Feed feed, List<String> bannedChannels, List<String> bannedTags)
    {
        this.bannedChannels = bannedChannels;
        this.bannedTags = bannedTags;

        return feed.getVideos()
                   .stream()
                   .filter(this::oldFilter)
                   .filter(this::tagNameFilter)
                   .filter(this::tagDescriptionFilter)
                   .filter(this::channelFilter)
                   .sorted((v2, v1) -> (int) (v1.getViewCount() - v2.getViewCount()))
                   .limit(10)
                   .collect(Collectors.toList());
    }

    private boolean tagNameFilter(Video video)
    {
        for (String word : bannedTags)
        {
            Pattern pattern = Pattern.compile(word.toLowerCase());
            Matcher matcher = pattern.matcher(video.getTitle().toLowerCase());

            if (matcher.find())
            {
                logger.info("Тег-фильтрация в названии: [{}] {}", word, video.getTitle());
                return false;
            }
        }

        return true;
    }

    private boolean tagDescriptionFilter(Video video)
    {
        if (video.getDescription() == null)
        {
            return true;
        }

        for (String word : bannedTags)
        {
            Pattern pattern = Pattern.compile(word.toLowerCase());
            Matcher matcher = pattern.matcher(video.getDescription().toLowerCase());

            if (matcher.find())
            {
                logger.info("Тег-фильтрация в описании: [{}]{} ", word, video.getDescription());
                return false;
            }
        }

        return true;
    }

    private boolean oldFilter(Video video)
    {
        try
        {
            if (video.getOld() == null)
            {
                logger.warn("Video no have old. {}", video.toString());
                return false;
            }

            // Если не hours, тогда будет day(s), week но нам это ненадо
            // У нас гораничение 24 часа
            return video.getOld().matches("[0-9]+ hour.*");
        }
        catch (Exception e)
        {
            logger.error("Error on filtration by old", e);
        }

        return false;
    }

    private boolean channelFilter(Video video)
    {
        for (String channel : bannedChannels)
        {
            Pattern pattern = Pattern.compile(channel.toLowerCase());
            Matcher matcher = pattern.matcher(video.getChannel().toLowerCase());

            if (matcher.find())
            {
                logger.info("Канал-фильтрация: [{}] {}", channel, video.getTitle());
                return false;
            }
        }

        return true;
    }
}
