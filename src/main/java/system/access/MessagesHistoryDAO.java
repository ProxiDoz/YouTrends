package system.access;

public class MessagesHistoryDAO extends AbstractDAO
{
    private static MessagesHistoryDAO instance;

    public MessagesHistoryDAO()
    {
        instance = this;
    }

    public static MessagesHistoryDAO getInstance()
    {
        return instance;
    }

    public void insertMessage(String sender, String recepient, String message)
    {
        String sql = "INSERT INTO messageshistory (sender, recepient, message, date) VALUES (?,?,?, now())";

        jdbcTemplate.update(sql, sender, recepient, message);
    }
}
