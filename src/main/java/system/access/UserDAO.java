package system.access;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import system.MyLogger;
import system.User;
import system.Video;

public class UserDAO
{
    // поднять сервер на digitalocean
    // bitbucket
    // скрипт запуска
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

    public void insertUser(User user)
    {
        String insertQuery = "INSERT INTO user (chatId) VALUES (?)";

        try
        {
            jdbcTemplate.update(insertQuery,
                                user.getChatId());
        }
        catch (DuplicateKeyException e)
        {
            // нихуя. потому что это нормальное поведение
        }
        catch (Exception e)
        {
            MyLogger.logErr("insertUser error");
            e.printStackTrace();
        }
    }

    public List<User> getUsers()
    {
        List<User> users = new ArrayList<>();

        String query = "SELECT * FROM user";

        try
        {
            jdbcTemplate.query(query, result ->
            {
                User user = new User();
                user.setChatId(result.getString("chatId"));
                user.setBanned(result.getBoolean("isBanned"));
                users.add(user);
            });

            return users;
        }
        catch (Exception e)
        {
            MyLogger.logErr("getUsers error");
            e.printStackTrace();
        }

        return users;
    }
}
