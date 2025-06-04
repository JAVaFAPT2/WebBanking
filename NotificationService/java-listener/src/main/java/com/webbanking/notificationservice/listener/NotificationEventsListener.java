package com.webbanking.notificationservice.listener;

import com.webbanking.notificationservice.dto.SendNotificationCommand;
import com.webbanking.notificationservice.dto.NotificationSentReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationEventsListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventsListener.class);

    private final KafkaTemplate<String, NotificationSentReply> kafkaTemplate;

    @Value("${app.kafka.topics.notification-send-command}")
    private String notificationSendCommandTopic;

    @Value("${app.kafka.topics.notification-sent-reply}")
    private String notificationSentReplyTopic;

    @Autowired
    public NotificationEventsListener(KafkaTemplate<String, NotificationSentReply> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${app.kafka.topics.notification-send-command}", 
                   groupId = "${spring.kafka.consumer.group-id:notification-listener-group}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleSendNotificationCommand(@Payload SendNotificationCommand command) {
        LOGGER.info("Received SendNotificationCommand for sagaId: {}, transactionId: {}, userId: {}, type: {}",
                command.sagaId(), command.transactionId(), command.userId(), command.messageType());

        String notificationId = UUID.randomUUID().toString(); 
        boolean success = false;
        String failureReason = null;

        try {
            LOGGER.info("Simulating attempt to send {} notification for sagaId: {}, userId: {}. Subject: {}. NotificationId: {}",
                    command.messageType(), command.sagaId(), command.userId(), command.subject(), notificationId);
            
            if ("EMAIL".equalsIgnoreCase(command.messageType()) || "SMS".equalsIgnoreCase(command.messageType()) || "PUSH".equalsIgnoreCase(command.messageType())) {
                success = true;
                LOGGER.info("Simulated {} notification successful for sagaId: {}", command.messageType(), command.sagaId());
            } else {
                failureReason = "Unsupported notification message type: " + command.messageType();
                LOGGER.warn("Simulated {} notification failed for sagaId: {}: {}", command.messageType(), command.sagaId(), failureReason);
            }

        } catch (Exception e) {
            LOGGER.error("Exception during notification sending for sagaId: {}, userId: {}. Error: {}",
                    command.sagaId(), command.userId(), e.getMessage(), e);
            success = false;
            failureReason = "Exception during notification sending: " + e.getMessage();
        }

        NotificationSentReply reply = new NotificationSentReply(
                command.sagaId(),
                command.transactionId(),
                notificationId,
                success,
                failureReason
        );

        kafkaTemplate.send(notificationSentReplyTopic, command.sagaId(), reply);
        LOGGER.info("Sent NotificationSentReply for sagaId: {}. Success: {}. NotificationId: {}", command.sagaId(), success, notificationId);
    }
} 