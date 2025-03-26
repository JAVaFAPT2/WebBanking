package service.orchestrationservice.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Supplier;

@Configuration
public class OrchestrationStreamConfig {

    @Bean
    public Supplier<Message<String>> orchestrationSupplier() {
        // Here you generate or fetch the message payload as needed
        return () -> MessageBuilder.withPayload("Orchestration Event")
                .build();
    }
}
