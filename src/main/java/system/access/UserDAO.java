package system.access;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import system.MyLogger;
import system.User;

public class UserDAO
{
    private JdbcTemplate jdbcTemplate;

    private static UserDAO instance;

    public UserDAO()
    {
        instance = this;
    }

    public static UserDAO getInstance()
    {
        return instance;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * @return - true - new user was inserted. false - user was exist.
     */
    public boolean insertUser(User user)
    {
        String insertQuery = "INSERT INTO User (chatId, isSubscribe) VALUES (?, 0)";

        try
        {
            jdbcTemplate.update(insertQuery, user.getChatId());
        }
        catch (DuplicateKeyException e)
        {
            return false;
        }
        catch (Exception e)
        {
            MyLogger.logErr("insertUser error");
            e.printStackTrace();
            return false;
        }

        MyLogger.logWarn("New user: " + user.getChatId());
        return true;
    }

    public void subscribeUser(User user)
    {
        String updateQuery = "UPDATE User SET isSubscribe = 1 WHERE chatId = ?";

        try
        {
            jdbcTemplate.update(updateQuery, user.getChatId());
        }
        catch (Exception e)
        {
            MyLogger.logErr("subscribeUser error");
            e.printStackTrace();
        }

        MyLogger.logWarn("Subscribe. User: " + user.getChatId());
    }

    public void unsubscribeUser(User user)
    {
        String updateQuery = "UPDATE User SET isSubscribe = 0 WHERE chatId = ?";

        try
        {
            jdbcTemplate.update(updateQuery, user.getChatId());
        }
        catch (Exception e)
        {
            MyLogger.logErr("unsubscribeUser error");
            e.printStackTrace();
        }

        MyLogger.logWarn("Unubscribe. User: " + user.getChatId());
    }

    public List<User> getSubscribedUsers()
    {
        List<User> users = new ArrayList<>();

        String query = "SELECT * FROM User WHERE isSubscribe = 1";

        try
        {
            jdbcTemplate.query(query, result ->
            {
                User user = new User();
                user.setChatId(result.getString("chatId"));
                user.setBanned(result.getBoolean("isBanned"));
                user.setSubscribe(result.getBoolean("isSubscribe"));
                users.add(user);
            });

            return users;
        }
        catch (Exception e)
        {
            MyLogger.logErr("getSubscribedUsers error");
            e.printStackTrace();
        }

        return users;
    }
}
