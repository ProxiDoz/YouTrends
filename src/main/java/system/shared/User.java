package system.shared;

public class User
{
    // Properties like a User from TelegramAPI
    private Integer id;
    private String firstName;
    private String lastName;
    private String userName;
    private String languageCode;
    private Boolean isBot;

    // Properties for this service
    private boolean isBanned;
    private boolean isSubscribe;

    public User()
    {
    }

    public User(org.telegram.telegrambots.api.objects.User telegramUser)
    {
        this.id = telegramUser.getId();
        this.firstName = telegramUser.getFirstName();
        this.lastName = telegramUser.getLastName();
        this.userName = telegramUser.getUserName();
        this.languageCode = telegramUser.getLanguageCode();
        this.isBot = telegramUser.getBot();
        this.id = telegramUser.getId();
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getLanguageCode()
    {
        return languageCode;
    }

    public void setLanguageCode(String languageCode)
    {
        this.languageCode = languageCode;
    }

    public Boolean getBot()
    {
        return isBot;
    }

    public void setBot(Boolean bot)
    {
        isBot = bot;
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
