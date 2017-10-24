package system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.telegram.telegrambots.ApiContextInitializer;

@ComponentScan
@EnableAutoConfiguration
@ImportResource("appContext.xml")
public class Application
{
    public static void main(String[] args)
    {
        ApiContextInitializer.init(); // Telegram API (must be init in static context)
        SpringApplication.run(Application.class, args);
    }
}