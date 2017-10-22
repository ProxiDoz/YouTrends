package system;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestStarter
{
    private static final int DISPATCHER_PERIOD = 1; // Days

    public TestStarter(Telegram telegram)
    {
        try
        {
            // Берём сегодняшний день и задаем время рассылки
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 50);
            calendar.setTime(date);

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
