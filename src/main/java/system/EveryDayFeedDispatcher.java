package system;

import java.util.List;

import system.access.UserDAO;
import system.shared.User;

public class EveryDayFeedDispatcher implements Runnable
{
    private UserDAO userDAO;
    private Telegram telegram;
    private LastFeedContainer lastFeedContainer;

    public EveryDayFeedDispatcher(Telegram telegram, UserDAO userDAO, LastFeedContainer lastFeedContainer)
    {
        this.telegram = telegram;
        this.userDAO = userDAO;
        this.lastFeedContainer = lastFeedContainer;
    }

    @Override
    public void run()
    {
        List<User> subscribedUsers = userDAO.getSubscribeUsers();

        for (User user : subscribedUsers)
        {
            String chatId = user.getId().toString();
            telegram.sendFeedToUser(lastFeedContainer.getVideosForUser(chatId), chatId);
        }
    }
}
