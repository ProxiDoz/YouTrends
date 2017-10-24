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
    private int lastMessageId = 0;

    private TelegramBot bot;

    Telegram(Settings settings)
    {
        bot = new TelegramBot(settings.getBotToken());
        MyLogger.logInfo("Telegram bot init");

        GetUpdates getUpdates = new GetUpdates();
        GetUpdatesResponse response = bot.execute(getUpdates);

        MyLogger.logInfo("Init updates: " + response.updates().size());

        if (response.updates().size() > 0)
        {
            lastMessageId = response.updates().get(response.updates().size() - 1).updateId() + 1;
        }
        else
        {
            lastMessageId = 0; // Тут поидее тянуть его из базы надо
        }

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

                             String chatId = message.from().id().toString();
                             String text = message.text();

                             MyLogger.logInfo("From: " + chatId + " Text: " + text);

                             User user = new User();
                             user.setChatId(chatId);

                             // Register new user (if him real new)
                             UserDAO.getInstance().insertUser(user);

                             if (text != null)
                             {
                                 if (text.equalsIgnoreCase("/subscribe"))
                                 {
                                     UserDAO.getInstance().subscribeUser(user);

                                     SendMessage sendMessage = new SendMessage(chatId,
                                                                               "You subscribe on trends.\nYou will get trends on 20:00 (MSK).");
                                     bot.execute(sendMessage);
                                 }
                                 else if (text.equalsIgnoreCase("/unsubscribe"))
                                 {
                                     UserDAO.getInstance().unsubscribeUser(user);

                                     SendMessage sendMessage = new SendMessage(chatId,
                                                                               "You unsubscribed from trends.");
                                     bot.execute(sendMessage);
                                 }
                                 else if(text.equalsIgnoreCase("/trends"))
                                 {
                                     sendFeedToUser(LastFeedContainer.getFeed(), chatId);
                                 }
                                 else
                                 {
                                     MyLogger.logInfo("Unrecognized text from " + chatId + ": " + text);

                                     SendMessage sendMessage = new SendMessage(chatId,
                                                                               "This command unrecognized.");
                                     bot.execute(sendMessage);
                                 }
                             }
                         }
                     }
                 }, 5, 2, TimeUnit.SECONDS);
    }

    public void sendFeed(Feed feed)
    {
        try
        {
            List<User> users = UserDAO.getInstance().getSubscribedUsers();

            for (User user : users)
            {
                sendFeedToUser(feed, user.getChatId());
            }
        }
        catch (Exception e)
        {
            MyLogger.logErr("Send feed error");
            e.printStackTrace();
        }
    }

    public void sendFeedToUser(Feed feed, String chatId)
    {
        try
        {
            SendMessage sendMessage = new SendMessage(chatId, "You trends :)");
            bot.execute(sendMessage);

            for (Video video : feed.getVideos())
            {
                // TODO: эту хуйню можно оптимизировать. Но только не сегодня.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(video.getImage(), "jpg", baos);
                baos.flush();
                byte[] imageInByte = baos.toByteArray();
                baos.close();

                SendPhoto sendPhoto = new SendPhoto(chatId, imageInByte);
                sendPhoto.caption("https://www.youtube.com/watch?v=" + video.getId() + "\n" + video.getName());
                bot.execute(sendPhoto);
            }
        }
        catch (Exception e)
        {
            MyLogger.logErr("Send feed to user error");
            e.printStackTrace();
        }
    }
}
