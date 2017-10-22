package system;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import system.access.UserDAO;

public class Telegram
{
    private List<User> users;
    private int lastMessageId = 0;

    private TelegramBot bot;

    Telegram(Settings settings)
    {
        bot = new TelegramBot(settings.getBotToken());

        GetUpdates getUpdates = new GetUpdates();
        GetUpdatesResponse response = bot.execute(getUpdates);

        if (response.updates().size() > 0)
        {
            lastMessageId = response.updates().get(response.updates().size() - 1).updateId() + 1;

            Executors.newSingleThreadScheduledExecutor()
                     .scheduleWithFixedDelay(() -> {
                         GetUpdates getUpdates1 = new GetUpdates();
                         getUpdates1.offset(lastMessageId);
                         GetUpdatesResponse response1 = bot.execute(getUpdates1);
                         List<Update> updates = response1.updates();

                         if (response1.updates().size() > 0)
                         {
                             MyLogger.logInfo("Have updates: " + response1.updates().size());

                             lastMessageId = response1.updates().get(response1.updates().size() - 1).updateId() + 1;

                             for (Update update : updates)
                             {
                                 Message message = update.message();
                                 String text = message.text();

                                 MyLogger.logInfo("text: " + text);

                                 if (text != null)
                                 {
                                     if (text.equalsIgnoreCase("Получать тренды"))
                                     {
                                         String chatId = message.from().id().toString();

                                         MyLogger.logWarn("Subscribe. user: " + chatId);

                                         User user = new User();
                                         user.setChatId(chatId);

                                         UserDAO.getInstance().insertUser(user);
                                         SendMessage sendMessage = new SendMessage(chatId,
                                                                                   "Вы подписались на тренды.\nПо вечерам вам будут приходить тренды.");
                                         bot.execute(sendMessage);
                                     }
                                 }
                             }
                         }
                     }, 5, 2, TimeUnit.SECONDS);
        }
    }

    void sendFeed(Feed feed)
    {
        try
        {
            users = UserDAO.getInstance().getUsers();

            for (User user : users)
            {
                SendMessage sendMessage = new SendMessage(user.getChatId(), "Наслаждайся");
                bot.execute(sendMessage);

                for (Video video : feed.getVideos())
                {
                    // TODO: эту хуйню можно оптимизировать. Но только не сегодня.
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(video.getImage(), "jpg", baos);
                    baos.flush();
                    byte[] imageInByte = baos.toByteArray();
                    baos.close();

                    SendPhoto sendPhoto = new SendPhoto(user.getChatId(), imageInByte);
                    sendPhoto.caption("https://www.youtube.com/watch?v=" + video.getId() + "\n" + video.getName());
                    bot.execute(sendPhoto);
                }
            }
        }
        catch (Exception e)
        {
            MyLogger.logErr("Ошибка отправки Feed");
            e.printStackTrace();
        }
    }
}
