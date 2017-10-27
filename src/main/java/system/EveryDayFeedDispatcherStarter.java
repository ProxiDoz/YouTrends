package system;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.TelegramBotsApi;

public class EveryDayFeedDispatcherStarter
{
    private static final Logger logger = LogManager.getLogger(EveryDayFeedDispatcherStarter.class);

    private static final int DISPATCHER_PERIOD = 1; // Days

    private static final int DISPATCHER_HOUR = 20;
    private static final int DISPATCHER_MINUTES = 0;
    private static final int DISPATCHER_SECONDS = 0;

    public EveryDayFeedDispatcherStarter(Telegram telegram)
    {
        try
        {
            TelegramBotsApi botsApi = new TelegramBotsApi();

            try {
                botsApi.registerBot(telegram);
            } catch (Exception e) {
                logger.error("Bot can't register", e);
            }

            // Берём сегодняшний день и задаем время рассылки
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, DISPATCHER_HOUR);
            calendar.set(Calendar.MINUTE, DISPATCHER_MINUTES);
            calendar.set(Calendar.SECOND, DISPATCHER_SECONDS);

            // Высчитываем через сколько миллисекунд нужно будет запустить рассылку
            long startDelay = calendar.getTimeInMillis() - System.currentTimeMillis();

            // Если уже больше времени рассылки
            if (startDelay < 0)
            {
                calendar.add(Calendar.DAY_OF_YEAR, DISPATCHER_PERIOD);

                startDelay = calendar.getTimeInMillis() - System.currentTimeMillis();
            }

            // Start every day feed dispatcher
            Executors.newSingleThreadScheduledExecutor()
                     .scheduleWithFixedDelay(new EveryDayFeedDispatcherRunnable(telegram),
                                             startDelay,
                                             TimeUnit.DAYS.toMillis(DISPATCHER_PERIOD),
                                             TimeUnit.MILLISECONDS);

            // Start once feed parser for access to feed from start
            Executors.newSingleThreadExecutor().execute(new FeedParserRunnable());
        }
        catch (Exception e)
        {
            logger.error("Some error", e);
        }
    }
}
