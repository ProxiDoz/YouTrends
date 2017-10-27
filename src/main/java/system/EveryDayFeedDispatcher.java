package system;

import java.util.List;

import system.access.UserDAO;
import system.shared.User;

public class EveryDayFeedDispatcher implements Runnable
{
    private Telegram telegram;

    public EveryDayFeedDispatcher(Telegram telegram)
    {
        this.telegram = telegram;
    }

    @Override
    public void run()
    {
        List<User> subscribedUsers = UserDAO.getInstance().getSubscribeUsers();

        for (User user: subscribedUsers)
        {
            String chatId = user.getId().toString();
            telegram.sendFeedToUser(LastFeedContainer.getVideosForUser(chatId), chatId);
        }
    }
}
