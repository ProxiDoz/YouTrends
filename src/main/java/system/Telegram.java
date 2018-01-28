package system;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import system.access.BannedChannelDAO;
import system.access.BannedTagDAO;
import system.access.MessagesHistoryDAO;
import system.access.UserDAO;
import system.shared.Settings;
import system.shared.User;
import system.shared.Video;

public class Telegram extends TelegramLongPollingBot
{
    private static final Logger logger = LogManager.getLogger(Telegram.class);

    private static final String GET_TRENDS_CMD = "/trends";
    private static final String SUBSCRIBE_CMD = "/subscribe";
    private static final String UNSUBSCRIBE_CMD = "/unsubscribe";
    private static final String GET_BANNED_CHANNELS_CMD = "/banned_channels";
    private static final String GET_BANNED_TAGS_CMD = "/banned_tags";
    private static final String HELP_CMD = "/help";
    private static final String ADD_BANNED_TAG_CMD = "addtag";
    private static final String ADD_BANNED_CHANNEL_CMD = "addch";
    private static final String REMOVE_BANNED_TAG_CMD = "rmtag";
    private static final String REMOVE_BANNED_CHANNEL_CMD = "rmch";

    private static final String COMMAND_REGEXP = "(\\S+)\\s+(.+)";

    private static final String ON_BANNED_CHANNELS_NOT_EXISTS_TEXT = "You don't have banned channels.\n" +
                                                                     "For add channel in ban list use: addch channel_name";
    private static final String ON_BANNED_TAGS_NOT_EXISTS_TEXT = "You don't have banned tags.\n" +
                                                                 "For add tag in ban list use: addtag channel_name";
    private static final String ON_SUBSCRIBE_TEXT = "You subscribe on trends.\nYou will get trends on 20:00 (MSK)";
    private static final String ON_UNSUBSCRIBE_TEXT = "You unsubscribed from trends";
    private static final String ON_SUCCESS_ADD_TEXT = "successfully added";
    private static final String ON_SUCCESS_REMOVE_TEXT = "successfully removed";
    private static final String ON_ALREADY_ADDED_TEXT = " already added";
    private static final String ON_ALREADY_REMOVED_TEXT = " already removed";
    private static final String ON_UNRECOGNIZED_TEXT = "This command unrecognized";
    private static final String REGISTER_MESSAGE = "Welcome to trends bot. Use /help for see command description";
    private static final String HELP_TEXT = "You can exclude unwanted videos from the feed by using filtration system.\n" +
                                            "You can exclude videos by channel name and tags in description\n" +
                                            "For ban some tags and channels use the next command:\n" +
                                            "Ban channels: addch channel_name1, channel_name2...\n" +
                                            "Ban tags: addtag tag1, tag2...\n" +
                                            "For remove ban use:\n" +
                                            "Remove ban for channels: rmch channel_name1, channel_name2...\n" +
                                            "Remove ban for tags: rmtag tag1, tag2...";

    private Settings settings;

    Telegram(Settings settings)
    {
        this.settings = settings;
    }

    @Override
    public void onUpdateReceived(Update update)
    {
        Pattern commandPattern = Pattern.compile(COMMAND_REGEXP);

        try
        {
            Message message = update.getMessage();

            if (update.hasMessage() && message.hasText())
            {
                String chatId = message.getChatId().toString();
                String text = message.getText();

                MessagesHistoryDAO.getInstance().insertMessage(chatId, "bot", text);

                Matcher commandMatcher = commandPattern.matcher(text.toLowerCase());

                User user = new User(message.getFrom());

                if (!UserDAO.getInstance().isUserRegistered(user))
                {
                    UserDAO.getInstance().registerUser(user);
                    sendMessage(chatId, REGISTER_MESSAGE);
                }

                if (text.equalsIgnoreCase(SUBSCRIBE_CMD))
                {
                    UserDAO.getInstance().subscribeUser(user);
                    sendMessage(chatId, ON_SUBSCRIBE_TEXT);
                }
                else if (text.equalsIgnoreCase(UNSUBSCRIBE_CMD))
                {
                    UserDAO.getInstance().unsubscribeUser(user);
                    sendMessage(chatId, ON_UNSUBSCRIBE_TEXT);
                }
                else if (text.equalsIgnoreCase(GET_TRENDS_CMD))
                {
                    sendFeedToUser(LastFeedContainer.getVideosForUser(chatId), chatId);
                }
                else if (commandMatcher.matches())
                {
                    String command = commandMatcher.group(1).toLowerCase();
                    String arguments = commandMatcher.group(2).toLowerCase();

                    String[] args = arguments.split(", ");

                    switch (command)
                    {
                        case ADD_BANNED_TAG_CMD:
                            commandExecutor(user,
                                            BannedTagDAO.getInstance()::add,
                                            args,
                                            ON_SUCCESS_ADD_TEXT,
                                            ON_ALREADY_ADDED_TEXT);
                            break;
                        case ADD_BANNED_CHANNEL_CMD:
                            commandExecutor(user,
                                            BannedChannelDAO.getInstance()::add,
                                            args,
                                            ON_SUCCESS_ADD_TEXT,
                                            ON_ALREADY_ADDED_TEXT);
                            break;
                        case REMOVE_BANNED_TAG_CMD:
                            commandExecutor(user,
                                            BannedTagDAO.getInstance()::remove,
                                            args,
                                            ON_SUCCESS_REMOVE_TEXT,
                                            ON_ALREADY_REMOVED_TEXT);
                            break;
                        case REMOVE_BANNED_CHANNEL_CMD:
                            commandExecutor(user,
                                            BannedChannelDAO.getInstance()::remove,
                                            args,
                                            ON_SUCCESS_REMOVE_TEXT,
                                            ON_ALREADY_REMOVED_TEXT);
                            break;
                        default:
                            sendMessage(chatId, ON_UNRECOGNIZED_TEXT);
                            break;
                    }
                }
                else if (text.equalsIgnoreCase(HELP_CMD))
                {
                    sendMessage(chatId, HELP_TEXT);
                }
                else if (text.equalsIgnoreCase(GET_BANNED_CHANNELS_CMD))
                {
                    List<String> userBannedChannels = BannedChannelDAO.getInstance().getBannedChannels(chatId);

                    if (userBannedChannels.isEmpty())
                    {
                        sendMessage(chatId, ON_BANNED_CHANNELS_NOT_EXISTS_TEXT);
                    }
                    else
                    {
                        sendMessage(chatId, "Your banned channels:\n" + userBannedChannels.toString());
                    }
                }
                else if (text.equalsIgnoreCase(GET_BANNED_TAGS_CMD))
                {
                    List<String> userBannedTags = BannedTagDAO.getInstance().getBannedTags(chatId);

                    if (userBannedTags.isEmpty())
                    {
                        sendMessage(chatId, ON_BANNED_TAGS_NOT_EXISTS_TEXT);
                    }
                    else
                    {
                        sendMessage(chatId, "Your banned tags:\n" + userBannedTags.toString());
                    }
                }
                else
                {
                    sendMessage(chatId, ON_UNRECOGNIZED_TEXT);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }
    }

    private void commandExecutor(User user,
                                 TagAddingFunction tagAddingFunction,
                                 String[] args,
                                 String successMessage,
                                 String errorMessage)
    {
        for (String arg : args)
        {
            try
            {
                boolean isSuccess = tagAddingFunction.execute(user, arg);

                if (isSuccess)
                {
                    sendMessage(user.getId().toString(), arg + " " + successMessage);
                }
                else
                {
                    sendMessage(user.getId().toString(), arg + " " + errorMessage);
                }
            }
            catch (Exception e)
            {
                logger.error("Command executor error", e);
            }
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

    public void sendFeedToUser(List<Video> videos, String chatId)
    {
        try
        {
            for (Video video : videos)
            {
                try
                {
                    if (video.getFileId() == null)
                    {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(video.getImage(), "jpg", baos);
                        InputStream is = new ByteArrayInputStream(baos.toByteArray());

                        SendPhoto sendPhoto = new SendPhoto();
                        sendPhoto.setChatId(chatId);
                        sendPhoto.setNewPhoto("name", is);
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

                    SendMessage messageWithLink = new SendMessage(chatId,
                                                                  video.getChannel() + "\n[" + video.getName() + "](https://www.youtube.com/watch?v=" + video.getId() + ")");
                    messageWithLink.setParseMode(ParseMode.MARKDOWN);
                    messageWithLink.disableWebPagePreview();
                    execute(messageWithLink);
                }
                catch (Exception e)
                {
                    logger.error("Can't send video", e);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }
    }

    private void sendMessage(String chatId, String text)
    {
        try
        {
            SendMessage message = new SendMessage(chatId, text);
            execute(message);
            MessagesHistoryDAO.getInstance().insertMessage("bot", chatId, text);
        }
        catch (Exception e)
        {
            logger.error("Send message trouble", e);
        }
    }
}
