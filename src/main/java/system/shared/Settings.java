package system.shared;

public class Settings
{
    private String botName;
    private String botToken;

    public Settings(String botName, String botToken)
    {
        this.botName = botName;
        this.botToken = botToken;
    }


    public String getBotName()
    {
        return botName;
    }

    public void setBotName(String botName)
    {
        this.botName = botName;
    }

    public String getBotToken()
    {
        return botToken;
    }

    public void setBotToken(String botToken)
    {
        this.botToken = botToken;
    }
}
