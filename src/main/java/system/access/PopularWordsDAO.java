package system.access;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map.Entry;
import javax.sql.DataSource;

public class PopularWordsDAO extends AbstractDAO
{
    public PopularWordsDAO(DataSource dataSource)
    {
        super(dataSource);
    }

    public void insertPopularWords(List<Entry<String, Integer>> popularWords, Timestamp date)
    {
        int countWordsForSaving = 1000;

        String query = "INSERT INTO PopularWords (words, date) VALUES (?,?)";

        StringBuilder words = new StringBuilder(countWordsForSaving);

        for (int i = 0; i < countWordsForSaving; i++)
        {
            words.append(popularWords.get(i).getKey());
            words.append(" [");
            words.append(popularWords.get(i).getValue());
            words.append("],");
        }

        jdbcTemplate.update(query, words, date);
    }

    public Timestamp getLastInsertDate()
    {
        String query = "SELECT date FROM PopularWords ORDER BY date DESC LIMIT 1";

        Timestamp date = new Timestamp(0);

        // Incredible crutch, but jdbcTemplate.queryForObject not working. smth throwable exception.
        jdbcTemplate.query(query,
                           rs ->
                           {
                               date.setTime(rs.getTimestamp("date").getTime());
                           }
                          );

        return date;
    }

}
