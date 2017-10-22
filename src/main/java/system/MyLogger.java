package system;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyLogger
{
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";

    private static Date date = new Date(System.currentTimeMillis());
    private static SimpleDateFormat formatForDateNow = new SimpleDateFormat("YYYY:MM:dd HH:mm:ss");

    /*synchronized*/ public static void logInfo(String text)
    {
        date.setTime(System.currentTimeMillis());
        System.out.println(formatForDateNow.format(date) + " " + GREEN + text + RESET);
    }

    /*synchronized*/ public static void logWarn(String text)
    {
        date.setTime(System.currentTimeMillis());
        System.out.println(formatForDateNow.format(date) + " " + YELLOW + text + RESET);
    }

    /*synchronized*/ public static void logErr(String text)
    {
        date.setTime(System.currentTimeMillis());
        System.out.println(formatForDateNow.format(date) + " " + RED + text + RESET);
    }
}