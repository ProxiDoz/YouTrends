package system;

import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import system.access.UserDAO;

public class Telegram extends TelegramLongPollingBot
{
    private static final Logger logger = LogManager.getLogger(Telegram.class);

    private static final String SUBSCRIBE_CMD = "/subscribe";
    private static final String UNSUBSCRIBE_CMD = "/unsubscribe";
    private static final String GET_TRENDS_CMD = "/trends";

    private static final String ON_SUBSCRIBE_TEXT = "You subscribe on trends.\nYou will get trends on 20:00 (MSK).";
    private static final String ON_UNSUBSCRIBE_TEXT = "You unsubscribed from trends.";
    private static final String ON_GET_TRENDS_TEXT = "You trends :)";
    private static final String ON_UNRECOGNIZED_TEXT = "This command unrecognized";

    private Settings settings;

    Telegram(Settings settings)
    {
        this.settings = settings;
    }

    @Override
    public void onUpdateReceived(Update update)
    {
        try
        {
            Message message = update.getMessage();

            if (update.hasMessage() && message.hasText())
            {
                String chatId = message.getChatId().toString();
                String text = message.getText();

                logger.info("From: {} Text: {}", chatId, text);

                User user = new User(message.getFrom());
                // TODO: add cache (don't use DAO)
                // Register new user (if him real new)
                UserDAO.getInstance().registerUserIfNotExist(user);

                if (text.equalsIgnoreCase(SUBSCRIBE_CMD))
                {
                    UserDAO.getInstance().subscribeUser(user);

                    SendMessage responseMessage = new SendMessage(chatId, ON_SUBSCRIBE_TEXT);
                    execute(responseMessage);
                }
                else if (text.equalsIgnoreCase(UNSUBSCRIBE_CMD))
                {
                    UserDAO.getInstance().unsubscribeUser(user);

                    SendMessage responseMessage = new SendMessage(chatId, ON_UNSUBSCRIBE_TEXT);
                    execute(responseMessage);
                }
                else if (text.equalsIgnoreCase(GET_TRENDS_CMD))
                {
                    sendFeedToUser(LastFeedContainer.getFeed(), chatId);
                }
                else
                {
                    logger.info("Unrecognized text from {}: {}", chatId, text);

                    SendMessage responseMessage = new SendMessage(chatId, ON_UNRECOGNIZED_TEXT);
                    execute(responseMessage);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }
    }

    @Override
    public String getBotUsername()
    {
        return settings.getBotName();
    }

    @Override
    public String getBotToken()
    {
        return settings.getBotToken();
    }

    public void sendFeed(Feed feed)
    {
        try
        {
            List<User> users = UserDAO.getInstance().getSubscribeUsers();

            for (User user : users)
            {
                sendFeedToUser(feed, user.getId().toString());
            }
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }
    }

    public void sendFeedToUser(Feed feed, String chatId)
    {
        try
        {
            SendMessage sendMessage = new SendMessage(chatId, ON_GET_TRENDS_TEXT);
            execute(sendMessage);

            for (Video video : feed.getVideos())
            {
                if (video.getFileId() == null)
                {
                    File image = new File("image");
                    ImageIO.write(video.getImage(), "jpg", image);

                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(chatId);
                    sendPhoto.setNewPhoto(image);
                    Message response = sendPhoto(sendPhoto);
                    // Get id from last photo from list
                    String fileId = response.getPhoto().get(response.getPhoto().size() - 1).getFileId();
                    video.setFileId(fileId);
                }
                else
                {
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(chatId);
                    sendPhoto.setPhoto(video.getFileId());
                    sendPhoto(sendPhoto);
                }

                SendMessage messageWithLink = new SendMessage(chatId, "[" + video.getName() + "](https://www.youtube.com/watch?v=" + video.getId() + ")");
                messageWithLink.setParseMode(ParseMode.MARKDOWN);
                messageWithLink.disableWebPagePreview();
                execute(messageWithLink);
            }
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }
    }
}
