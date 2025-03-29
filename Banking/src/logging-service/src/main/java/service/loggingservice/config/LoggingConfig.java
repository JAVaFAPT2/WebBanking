package service.loggingservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import service.loggingservice.repository.LogRepository;
import service.shared.models.Log;

@Configuration
public class LoggingConfig {
    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);
    @Bean
    @Scope("prototype")
    public Log createLog() {
        return new Log();
    }

}
