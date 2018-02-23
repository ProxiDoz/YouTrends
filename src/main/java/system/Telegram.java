package system;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import system.access.BannedChannelDAO;
import system.access.BannedTagDAO;
import system.access.MessagesHistoryDAO;
import system.access.UserDAO;
import system.shared.Settings;
import system.shared.User;
import system.shared.Video;

public class Telegram extends TelegramLongPollingBot
{
    static
    {
        ApiContextInitializer.init(); // Telegram API (must be init in static context)
    }

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
    private static final String ON_SUBSCRIBE_TEXT = "You subscribe on trends.\nYou will receive trends at 20:00 (MSK)";
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

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private BannedTagDAO bannedTagDAO;

    @Autowired
    private BannedChannelDAO bannedChannelDAO;

    @Autowired
    private MessagesHistoryDAO messagesHistoryDAO;

    @Autowired
    private LastFeedContainer lastFeedContainer;

    @Autowired
    private WordsFrequencyAnalyser wordsFrequencyAnalyser;

    public Telegram(Settings settings)
    {
        this.settings = settings;

        try
        {
            TelegramBotsApi botsApi = new TelegramBotsApi();
            botsApi.registerBot(this);
        }
        catch (TelegramApiRequestException e)
        {
            logger.error("Error creating bot", e);
        }
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

                messagesHistoryDAO.insertMessage(chatId, "bot", text);

                Matcher commandMatcher = commandPattern.matcher(text.toLowerCase());

                User user = new User(message.getFrom());

                if (!userDAO.isUserRegistered(user))
                {
                    userDAO.registerUser(user);
                    sendMessage(chatId, REGISTER_MESSAGE);
                }

                if (text.equalsIgnoreCase(SUBSCRIBE_CMD))
                {
                    userDAO.subscribeUser(user);
                    sendMessage(chatId, ON_SUBSCRIBE_TEXT);
                }
                else if (text.equalsIgnoreCase(UNSUBSCRIBE_CMD))
                {
                    userDAO.unsubscribeUser(user);
                    sendMessage(chatId, ON_UNSUBSCRIBE_TEXT);
                }
                else if (text.equalsIgnoreCase(GET_TRENDS_CMD))
                {
                    sendFeedToUser(lastFeedContainer.getVideosForUser(chatId), chatId);
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
                                            bannedTagDAO::add,
                                            args,
                                            ON_SUCCESS_ADD_TEXT,
                                            ON_ALREADY_ADDED_TEXT);
                            break;
                        case ADD_BANNED_CHANNEL_CMD:
                            commandExecutor(user,
                                            bannedChannelDAO::add,
                                            args,
                                            ON_SUCCESS_ADD_TEXT,
                                            ON_ALREADY_ADDED_TEXT);
                            break;
                        case REMOVE_BANNED_TAG_CMD:
                            commandExecutor(user,
                                            bannedTagDAO::remove,
                                            args,
                                            ON_SUCCESS_REMOVE_TEXT,
                                            ON_ALREADY_REMOVED_TEXT);
                            break;
                        case REMOVE_BANNED_CHANNEL_CMD:
                            commandExecutor(user,
                                            bannedChannelDAO::remove,
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
                    List<String> userBannedChannels = bannedChannelDAO.getBannedChannels(chatId);

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
                    List<String> userBannedTags = bannedTagDAO.getBannedTags(chatId);

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
            List<Entry<String,Integer>> words = wordsFrequencyAnalyser.getPopularWords(5);

            StringBuffer wordsBuffer = new StringBuffer(5);

            for (Entry<String, Integer> word: words)
            {
                wordsBuffer.append(" #");
                wordsBuffer.append(word.getKey());
            }

            sendMessage(chatId, "Most popular in the last week:\n" + wordsBuffer);

            for (Video video : videos)
            {
                try
                {
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(chatId);
                    sendPhoto.setCaption(video.getTitle());

                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    inlineKeyboardButton.setText("Watch on " + video.getChannel());
                    inlineKeyboardButton.setUrl("https://www.youtube.com/watch?v=" + video.getId());
                    List<InlineKeyboardButton> row = Lists.newArrayList();
                    row.add( inlineKeyboardButton);
                    List<List<InlineKeyboardButton>> columns = Lists.newArrayList();
                    columns.add(row);
                    inlineKeyboardMarkup.setKeyboard(columns);
                    sendPhoto.setReplyMarkup(inlineKeyboardMarkup);

                    if (video.getFileId() == null)
                    {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(video.getImage(), "jpg", baos);
                        InputStream is = new ByteArrayInputStream(baos.toByteArray());

                        sendPhoto.setNewPhoto("name", is);
                        Message response = sendPhoto(sendPhoto);
                        // Get id from last photo from list
                        String fileId = response.getPhoto().get(response.getPhoto().size() - 1).getFileId();
                        video.setFileId(fileId);
                    }
                    else
                    {
                        sendPhoto.setPhoto(video.getFileId());
                        sendPhoto(sendPhoto);
                    }

                    messagesHistoryDAO.insertMessage("bot", chatId, video.getTitle());
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
            messagesHistoryDAO.insertMessage("bot", chatId, text);
        }
        catch (Exception e)
        {
            logger.error("Send message trouble", e);
        }
    }
}
