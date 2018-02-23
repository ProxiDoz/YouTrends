package system.access;

import java.util.List;
import java.util.Map.Entry;
import javax.sql.DataSource;

import com.google.common.collect.Lists;
import system.shared.KeyValueEntry;

public class ChannelAnalytics extends AbstractDAO
{
    public ChannelAnalytics(DataSource dataSource)
    {
        super(dataSource);
    }

    public List<Entry<String, Integer>> getTopChannelsByHoursInTrends()
    {
        String query = "SELECT " +
                       "  channel, " +
                       "  COUNT(channel) AS countHours " +
                       "FROM video " +
                       "WHERE date BETWEEN now() - INTERVAL '1 week' AND now() " +
                       "GROUP BY channel " +
                       "ORDER BY countHours DESC " +
                       "LIMIT 10";

        List<Entry<String, Integer>> channels = Lists.newArrayList();

        jdbcTemplate.query(query,
                           rs ->
                           {
                               channels.add(new KeyValueEntry<>(rs.getString("channel"),
                                                                rs.getInt("countHours")))
                                ;
                           });
        return channels;
    }

    public List<Entry<String, Integer>> getTopChannelsByCountUniqueVideoInTrends()
    {
        String query = "SELECT " +
                       "  channel, " +
                       "  COUNT(channel) count " +
                       "FROM ( " +
                       "       SELECT DISTINCT " +
                       "         videoid, " +
                       "         channel " +
                       "       FROM video " +
                       "       WHERE date BETWEEN now() - INTERVAL '1 week' AND now() " +
                       "     ) AS uniqueVideosOnChannels " +
                       "GROUP BY channel " +
                       "ORDER BY count DESC " +
                       "LIMIT 10";

        List<Entry<String, Integer>> channels = Lists.newArrayList();

        jdbcTemplate.query(query,
                           rs ->
                           {
                               channels.add(new KeyValueEntry<>(rs.getString("channel"),
                                                                rs.getInt("count")))
                               ;
                           });
        return channels;
    }
}
