package system.access;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class AbstractDAO
{
    JdbcTemplate jdbcTemplate;

    AbstractDAO(DataSource dataSource)
    {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
