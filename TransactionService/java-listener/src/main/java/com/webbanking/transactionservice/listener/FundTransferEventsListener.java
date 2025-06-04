package com.webbanking.transactionservice.listener;

import com.webbanking.transactionservice.dto.RecordTransactionCommand;
import com.webbanking.transactionservice.dto.TransactionRecordedReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FundTransferEventsListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FundTransferEventsListener.class);

    private final KafkaTemplate<String, TransactionRecordedReply> kafkaTemplate;
    private static final String TRANSACTION_REPLY_TOPIC = "fundtransfer.transaction.recorded.reply"; // Ensure this matches orchestrator

    @Autowired
    public FundTransferEventsListener(KafkaTemplate<String, TransactionRecordedReply> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "fundtransfer.transaction.record.command", groupId = "transaction-listener-group",
                   containerFactory = "kafkaListenerContainerFactory") // Assuming a container factory is configured
    public void handleRecordTransactionCommand(@Payload RecordTransactionCommand command) {
        LOGGER.info("Received RecordTransactionCommand: {}", command);

        boolean success = true; // Simulate success
        String failureReason = null;
        LocalDateTime recordedAt = LocalDateTime.now();

        // Simulate transaction recording logic (replace with actual logic or call to .NET service later)
        if (command.amount() == null || command.amount().signum() <= 0) {
            success = false;
            failureReason = "Invalid transaction amount.";
            LOGGER.warn("Failed to record transaction {}: {}", command.transactionId(), failureReason);
        } else {
            LOGGER.info("Transaction {} for saga {} recorded successfully at {}. Details: Source: {}, Dest: {}, Amount: {} {}, Type: {}, Status: {}, Desc: {}", 
                command.transactionId(), 
                command.sagaId(), 
                recordedAt,
                command.sourceAccountId(), 
                command.destinationAccountId(), 
                command.amount(), 
                command.currency(),
                command.transactionType(),
                command.status(),
                command.description());
        }

        TransactionRecordedReply reply = new TransactionRecordedReply(
                command.sagaId(),
                command.transactionId(),
                success,
                failureReason,
                success ? recordedAt : null
        );

        LOGGER.info("Sending TransactionRecordedReply: {}", reply);
        kafkaTemplate.send(TRANSACTION_REPLY_TOPIC, command.sagaId(), reply); // Use sagaId as key
    }
} 