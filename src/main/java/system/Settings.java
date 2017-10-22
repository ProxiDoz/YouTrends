package system;

public class Settings
{
    public String getBotToken()
    {
        return botToken;
    }

    public void setBotToken(String botToken)
    {
        this.botToken = botToken;
    }

    private String botToken;

    public Settings(String botToken)
    {
        this.botToken = botToken;
    }
}
