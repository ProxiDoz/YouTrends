package system.access;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import system.shared.User;

public class BannedTagDAO extends AbstractDAO
{
    private static final Logger logger = LogManager.getLogger(BannedTagDAO.class);

    public BannedTagDAO(DataSource dataSource)
    {
        super(dataSource);
    }

    public List<String> getBannedTags(String chatId)
    {
        List<String> bannedTags = new ArrayList<>();

        String query = "SELECT name FROM BannedTag, UserBannedTag " +
                       "WHERE UserBannedTag.tagId = BannedTag.name AND " +
                       "UserBannedTag.userId = ?";

        try
        {
            jdbcTemplate.query(query, result ->
            {
                bannedTags.add(result.getString("name"));
            }, Long.valueOf(chatId));

            return bannedTags;
        }
        catch (Exception e)
        {
            logger.error("Error", e);
        }

        return bannedTags;
    }

    public boolean add(User user, String tag)
    {
        List<String> bannedTags = getBannedTags(user.getId().toString());

        if (!bannedTags.contains(tag))
        {
            String insertBannedTagQuery = "INSERT INTO BannedTag (name) VALUES (?) ON CONFLICT DO NOTHING";
            String insertUserBannedTagQuery = "INSERT INTO UserBannedTag (userid, tagid) VALUES (?, ?) ";

            jdbcTemplate.update(insertBannedTagQuery, tag);
            jdbcTemplate.update(insertUserBannedTagQuery, user.getId(), tag);

            return true;
        }

        return false;
    }

    public boolean remove(User user, String tag)
    {
        String removeUserBannedTagQuery = "DELETE FROM UserBannedTag WHERE userid = ? AND tagid = ? ";

        int affectedRows = jdbcTemplate.update(removeUserBannedTagQuery, user.getId(), tag);

        return affectedRows == 1;
    }
}
