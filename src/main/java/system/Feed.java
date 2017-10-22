package system;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import system.access.BannedChannelDAO;
import system.access.BannedTagDAO;

public class Feed
{
    private List<Video> videos = new ArrayList<>();

    private List<String> bannedTags;
    private List<String> bannedChannels;

    public Feed()
    {
        List<String> bannedTags = BannedTagDAO.getInstance().getBannedTags();
        List<String> bannedChannels = BannedChannelDAO.getInstance().getBannedChannels();

        this.bannedTags = new ArrayList<>(bannedTags);
        this.bannedChannels = new ArrayList<>(bannedChannels);
    }

    public List<Video> getVideos()
    {
        return videos;
    }

    public void setVideos(List<Video> videos)
    {
        this.videos = videos;
    }

    public List<Video> filtration()
    {
        return videos.stream()
                     .filter(this::oldFilter)
                     .filter(this::tagNameFilter)
                     .filter(this::tagDescriptionFilter)
                     .filter(this::channelFilter)
                     .sorted((v2, v1) -> (int)(v1.getViewCount() - v2.getViewCount()))
                     .limit(10)
                     .collect(Collectors.toList());
    }

    private boolean tagNameFilter(Video video)
    {
        for (String word : bannedTags)
        {
            Pattern pattern = Pattern.compile(word.toLowerCase());
            Matcher matcher = pattern.matcher(video.getName().toLowerCase());

            if (matcher.find())
            {
                MyLogger.logInfo("Тег-фильтрация в названии: [" + word + "] " + video.getName());
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
                MyLogger.logInfo("Тег-фильтрация в описании: [" + word + "] " + video.getDescription());
                return false;
            }
        }

        return true;
    }

    private boolean oldFilter(Video video)
    {
        try
        {
            // Если не hours, тогда будет day(s), week но нам это ненадо
            // У нас гораничение 24 часа
            return video.getOld().matches("[0-9]+ hour.*");
        }
        catch (Exception e)
        {
            MyLogger.logErr("oldFilter error");
            MyLogger.logErr(video.toString());
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
                MyLogger.logInfo("Канал-фильтрация: [" + channel + "] " + video.getName());
                return false;
            }
        }

        return true;
    }
}
