package system.access;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class AbstractDAO
{
    JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
