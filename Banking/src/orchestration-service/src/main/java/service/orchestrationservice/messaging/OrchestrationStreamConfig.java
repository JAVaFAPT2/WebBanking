package service.orchestrationservice.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;

import java.util.function.Supplier;

@Configuration
public class OrchestrationStreamConfig {

    @Bean
    public Supplier<Message<String>> orchestrationSupplier() {
        return () -> MessageBuilder.withPayload("Orchestration Event")
                .build();
    }

    @Bean
    public Consumer<String> orchestrationConsumer() {
        return message -> {
            System.out.println("Received message: " + message);
        };

    }
}
