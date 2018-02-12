package system.Configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import system.LastFeedContainer;
import system.Starter;
import system.Telegram;
import system.parser.YouTubeParser;
import system.shared.Settings;

@Configuration
@PropertySource("classpath:settings.properties")
public class AppConfiguration
{
    @Value("${botName}")
    private String botName;

    @Value("${botToken}")
    private String botToken;

    @Autowired
    private JdbcConfiguration jdbcConfiguration;

    @Bean
    public Starter starter()
    {
        return new Starter(telegram(), lastFeedContainer(), jdbcConfiguration.userDAO(), jdbcConfiguration.videoDAO());
    }

    @Bean
    public Telegram telegram()
    {
        return new Telegram(settings());
    }

    @Bean
    public Settings settings()
    {
        return new Settings(botName, botToken);
    }

    @Bean
    public YouTubeParser youTubeParser()
    {
        return new YouTubeParser();
    }

    @Bean
    public LastFeedContainer lastFeedContainer()
    {
        return new LastFeedContainer();
    }
}
