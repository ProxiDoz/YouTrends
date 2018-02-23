package system.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import system.access.BannedChannelDAO;
import system.access.BannedTagDAO;
import system.access.MessagesHistoryDAO;
import system.access.PopularWordsDAO;
import system.access.UserDAO;
import system.access.VideoDAO;

@Configuration
@PropertySource("classpath:jdbc.properties")
public class JdbcConfiguration
{
    @Value("${jdbc.driverClassName}")
    private String driverClassName;

    @Value("${jdbc.url}")
    private String url;

    @Value("${jdbc.username}")
    private String username;

    @Value("${jdbc.password}")
    private String password;

    @Bean
    public DriverManagerDataSource dataSource()
    {
        DriverManagerDataSource dmds = new DriverManagerDataSource();
        dmds.setDriverClassName(driverClassName);
        dmds.setUrl(url);
        dmds.setUsername(username);
        dmds.setPassword(password);
        return dmds;
    }

    @Bean
    public MessagesHistoryDAO messagesHistoryDAO()
    {
        return new MessagesHistoryDAO(dataSource());
    }

    @Bean
    public BannedTagDAO bannedTagDAO()
    {
        return new BannedTagDAO(dataSource());
    }

    @Bean
    public BannedChannelDAO bannedChannelDAO()
    {
        return new BannedChannelDAO(dataSource());
    }

    @Bean
    public UserDAO userDAO()
    {
        return new UserDAO(dataSource());
    }

    @Bean
    public VideoDAO videoDAO()
    {
        return new VideoDAO(dataSource());
    }

    @Bean
    public PopularWordsDAO popularWordsDAO()
    {
        return new PopularWordsDAO(dataSource());
    }
}
