package com.webbanking.notificationservice.listener;

import com.webbanking.notificationservice.dto.NotificationSentReply;
import com.webbanking.notificationservice.dto.SendNotificationCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for events from FundTransferService and sends appropriate notifications
 */
@Service
public class FundTransferSagaListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FundTransferSagaListener.class);

    private final KafkaTemplate<String, NotificationSentReply> kafkaTemplate;
    
    @Value("${app.kafka.topics.fund-transfer-notification-command}")
    private String fundTransferNotificationCommandTopic;
    
    @Value("${app.kafka.topics.fund-transfer-notification-reply}")
    private String fundTransferNotificationReplyTopic;
    
    @Value("${app.kafka.topics.transaction-notification-command}")
    private String transactionNotificationCommandTopic;
    
    @Value("${app.notification.service.grpc.url}")
    private String notificationServiceUrl;

    @Autowired
    public FundTransferSagaListener(KafkaTemplate<String, NotificationSentReply> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${app.kafka.topics.fund-transfer-notification-command}", 
                   groupId = "${spring.kafka.consumer.group-id:notification-listener-group}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleFundTransferNotification(@Payload SendNotificationCommand command) {
        LOGGER.info("Received fund transfer notification command: sagaId={}, transactionId={}, messageType={}",
                command.sagaId(), command.transactionId(), command.messageType());
        
        try {
            // Here you would call the notification service via gRPC to send the notification
            // For now we'll simulate a successful notification
            boolean success = true;
            String failureReason = null;
            
            // In a real implementation, we would use a gRPC client to call the notification service
            // NotificationServiceGrpcClient client = new NotificationServiceGrpcClient(notificationServiceUrl);
            // NotificationResponse response = client.sendNotification(command);
            // success = response.getSuccess();
            // failureReason = response.getFailureReason();
            
            String notificationId = UUID.randomUUID().toString();
            
            NotificationSentReply reply = new NotificationSentReply(
                    command.sagaId(),
                    command.transactionId(),
                    notificationId,
                    success,
                    failureReason
            );
            
            kafkaTemplate.send(fundTransferNotificationReplyTopic, command.sagaId(), reply);
            LOGGER.info("Sent notification reply: sagaId={}, success={}", reply.sagaId(), reply.success());
            
        } catch (Exception e) {
            LOGGER.error("Error processing fund transfer notification: {}", e.getMessage(), e);
            
            NotificationSentReply errorReply = new NotificationSentReply(
                    command.sagaId(),
                    command.transactionId(),
                    null,
                    false,
                    "Error processing notification: " + e.getMessage()
            );
            
            kafkaTemplate.send(fundTransferNotificationReplyTopic, command.sagaId(), errorReply);
        }
    }
    
    @KafkaListener(topics = "${app.kafka.topics.transaction-notification-command}", 
                   groupId = "${spring.kafka.consumer.group-id:notification-listener-group}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleTransactionNotification(@Payload SendNotificationCommand command) {
        LOGGER.info("Received transaction notification command: sagaId={}, transactionId={}, messageType={}",
                command.sagaId(), command.transactionId(), command.messageType());
        
        try {
            // Here you would call the notification service via gRPC to send the notification
            // For this example, we'll simulate a successful notification
            boolean success = true;
            String failureReason = null;
            
            String notificationId = UUID.randomUUID().toString();
            
            NotificationSentReply reply = new NotificationSentReply(
                    command.sagaId(),
                    command.transactionId(),
                    notificationId,
                    success,
                    failureReason
            );
            
            kafkaTemplate.send(fundTransferNotificationReplyTopic, command.sagaId(), reply);
            LOGGER.info("Sent transaction notification reply: sagaId={}, success={}", reply.sagaId(), reply.success());
            
        } catch (Exception e) {
            LOGGER.error("Error processing transaction notification: {}", e.getMessage(), e);
            
            NotificationSentReply errorReply = new NotificationSentReply(
                    command.sagaId(),
                    command.transactionId(),
                    null,
                    false,
                    "Error processing notification: " + e.getMessage()
            );
            
            kafkaTemplate.send(fundTransferNotificationReplyTopic, command.sagaId(), errorReply);
        }
    }
} 