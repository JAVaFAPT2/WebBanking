package service.accountservice.event;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import service.shared.event.AccountEvent;

@Component
public class AccountEventPublisher {

    private final KafkaTemplate<String, AccountEvent> kafkaTemplate;
    private final String topic = "account-events";

    @Autowired
    public AccountEventPublisher(KafkaTemplate<String, AccountEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(AccountEvent event) {
        // Use the accountId as the key so that messages with the same key are sent to the same partition
        kafkaTemplate.send(topic, String.valueOf(event.getEventId()), event)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        System.out.println("AccountEvent published successfully: " + event);
                    } else {
                        System.err.println("Failed to publish AccountEvent: " + event + ", error: " + throwable);
                    }
                });
    }
}
