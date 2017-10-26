package system;

import java.util.List;

public class UserSettingsData
{
    private Credentials credentials;
    private List<String> bannedChannels;
    private List<String> bannedTags;

    public Credentials getCredentials()
    {
        return credentials;
    }

    public void setCredentials(Credentials credentials)
    {
        this.credentials = credentials;
    }

    public List<String> getBannedChannels()
    {
        return bannedChannels;
    }

    public void setBannedChannels(List<String> bannedChannels)
    {
        this.bannedChannels = bannedChannels;
    }

    public List<String> getBannedTags()
    {
        return bannedTags;
    }

    public void setBannedTags(List<String> bannedTags)
    {
        this.bannedTags = bannedTags;
    }
}
