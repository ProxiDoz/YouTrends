package system;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import system.access.PopularWordsDAO;
import system.access.VideoDAO;
import system.shared.BannedPopularWord;
import system.shared.KeyValueEntry;

/**
 * Class provide processing popular word in video titles by last week.
 * This class controlling schedule of calculate and writing words into database.
 */
public class WordsFrequencyAnalyser implements Runnable
{
    private static final Logger logger = LogManager.getLogger(WordsFrequencyAnalyser.class);

    private VideoDAO videoDAO;
    private PopularWordsDAO popularWordsDAO;

    private List<Entry<String, Integer>> popularWords;

    public WordsFrequencyAnalyser(VideoDAO videoDAO, PopularWordsDAO popularWordsDAO)
    {
        this.videoDAO = videoDAO;
        this.popularWordsDAO = popularWordsDAO;

        Executors.newSingleThreadScheduledExecutor().schedule(this, 0, TimeUnit.SECONDS);

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        // Set calendar to begin next hour
        calendar.set(Calendar.HOUR_OF_DAY, currentHour + 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long initialDelay = calendar.getTimeInMillis() - System.currentTimeMillis();

        Executors.newSingleThreadScheduledExecutor()
                 .scheduleWithFixedDelay(this,
                                         initialDelay,
                                         TimeUnit.HOURS.toMillis(1),
                                         TimeUnit.MILLISECONDS);
    }

    public List<Entry<String, Integer>> getPopularWords(long limit)
    {
        return popularWords.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public void run()
    {
        popularWords = getPopularWords();

        Timestamp lastInsertDate = popularWordsDAO.getLastInsertDate();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // this check need for don't write popular words into database on start project when its not required
        if (System.currentTimeMillis() - lastInsertDate.getTime() > TimeUnit.MINUTES.toMillis(59) ||
            calendar.getTimeInMillis() - lastInsertDate.getTime() > 0)
        {
            popularWordsDAO.insertPopularWords(popularWords, new Timestamp(calendar.getTimeInMillis()));
        }
    }

    /**
     * Get most popular words from videos title in desc order.
     * Format: List of Entry when key - word, value - count of word entry
     */
    private List<Entry<String, Integer>> getPopularWords()
    {
        List<BannedPopularWord> bannedPopularWords = getBannedPopularWords();

        Map<String, Integer> popularWords = getPopularWords(bannedPopularWords);

        return sortPopularWordsByValue(popularWords);
    }

    /**
     * Get banned popular word from file
     */
    private List<BannedPopularWord> getBannedPopularWords()
    {
        List<BannedPopularWord> bannedPopularWordList = Lists.newArrayList();

        InputStream is = getClass().getResourceAsStream("/bannedPopularWords.csv");

        try (InputStreamReader inputStreamReader = new InputStreamReader(is))
        {
            char[] charBuffer = new char[2048];

            inputStreamReader.read(charBuffer, 0, charBuffer.length);

            String bannedPopularWordsString = String.copyValueOf(charBuffer);

            String[] bannedPopularWords = bannedPopularWordsString.split(",");

            for (String bannedPopularWord : bannedPopularWords)
            {
                bannedPopularWordList.add(new BannedPopularWord(bannedPopularWord));
            }
        }
        catch (IOException e)
        {
            logger.error("Something wrong with bannedPopularWords.csv reading", e);
        }

        return bannedPopularWordList;
    }

    /**
     * Get popular words from video titles by last week.
     *
     * @param bannedPopularWords - words that will be filtered form results
     * @return - map that contain word as key and count entries of this word in titles as value.
     */
    private Map<String, Integer> getPopularWords(List<BannedPopularWord> bannedPopularWords)
    {
        Map<String, Integer> popularWords = Maps.newHashMap();

        List<String> titles = videoDAO.getUniqueTitlesByLastWeek();

        for (String title : titles)
        {
            String[] wordsFormTitle = title.split(" ");

            for (String word : wordsFormTitle)
            {
                // pass word if it banned or if word is number
                if (bannedPopularWords.contains(new BannedPopularWord(word))
                    || StringUtils.isNumericSpace(word)
                    || StringUtils.containsOnly(word, "0123456789."))
                {
                    continue;
                }

                // Calculate count entries of this word
                Integer countWordEntry = popularWords.putIfAbsent(word, 1);

                if (countWordEntry != null)
                {
                    popularWords.put(word, ++countWordEntry);
                }
            }
        }

        return popularWords;
    }

    private List<Entry<String, Integer>> sortPopularWordsByValue(Map<String, Integer> popularWords)
    {
        List<Entry<String, Integer>> words = Lists.newArrayList();

        // Convert Map to List of Entries because we want do sort
        popularWords.forEach((k, v) -> words.add(new KeyValueEntry<>(k, v)));

        words.sort(new EntryValueComparator());

        return words;
    }

    /**
     * Comparator that compare Entry by value field
     * TODO: Впринципе можно обобщить, но надо ли?
     */
    private class EntryValueComparator implements Comparator<Entry<String, Integer>>
    {
        @Override
        public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2)
        {
            return o2.getValue() - o1.getValue();
        }
    }
}
