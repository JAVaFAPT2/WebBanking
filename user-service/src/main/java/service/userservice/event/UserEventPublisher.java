package service.userservice.event;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String topic = "user-events";
    @Autowired
    public UserEventPublisher(KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

        public void publish(UserEvent event) {
            // Use the userId as the key to ensure messages with the same key go to the same partition
            kafkaTemplate.send(topic, String.valueOf(event.getUserId()), event)
                    .whenComplete((success, failure) -> {
                        if (failure == null) {
                            System.out.println("UserEvent published successfully: " + event);
                        } else {
                            System.err.println("Failed to publish UserEvent: " + event + ", error: " + failure);
                        }
                    });
        }}
