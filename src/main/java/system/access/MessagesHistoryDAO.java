package system.access;

import javax.sql.DataSource;

public class MessagesHistoryDAO extends AbstractDAO
{
    public MessagesHistoryDAO(DataSource dataSource)
    {
        super(dataSource);
    }

    public void insertMessage(String sender, String recepient, String message)
    {
        String sql = "INSERT INTO messageshistory (sender, recepient, message, date) VALUES (?,?,?, now())";

        jdbcTemplate.update(sql, sender, recepient, message);
    }
}
