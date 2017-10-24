package system;

public class User
{
    private String chatId;
    private boolean isBanned;
    private boolean isSubscribe;

    public String getChatId()
    {
        return chatId;
    }

    public void setChatId(String chatId)
    {
        this.chatId = chatId;
    }

    public boolean isBanned()
    {
        return isBanned;
    }

    public void setBanned(boolean banned)
    {
        isBanned = banned;
    }

    public boolean isSubscribe()
    {
        return isSubscribe;
    }

    public void setSubscribe(boolean subscribe)
    {
        isSubscribe = subscribe;
    }
}
