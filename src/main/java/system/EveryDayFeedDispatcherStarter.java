package system;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EveryDayFeedDispatcherStarter
{
    private static final int DISPATCHER_PERIOD = 1; // Days

    private static final int DISPATCHER_HOUR = 20;
    private static final int DISPATCHER_MINUTES = 00;
    private static final int DISPATCHER_SECONDS = 0;

    public EveryDayFeedDispatcherStarter(Telegram telegram)
    {
        try
        {
            MyLogger.logInfo("Start");
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

            Executors.newSingleThreadScheduledExecutor()
                     .scheduleWithFixedDelay(new EveryDayFeedDispatcherRunnable(telegram),
                                             startDelay,
                                             TimeUnit.DAYS.toMillis(DISPATCHER_PERIOD),
                                             TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            MyLogger.logErr("EveryDayFeedDispatcherStarter throw exception");
            e.printStackTrace();
        }
    }
}
