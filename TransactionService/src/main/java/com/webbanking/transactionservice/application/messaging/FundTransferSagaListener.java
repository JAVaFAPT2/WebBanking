package com.webbanking.transactionservice.application.messaging;

import com.webbanking.transactionservice.application.cqrs.dto.RecordTransactionCommand;
import com.webbanking.transactionservice.application.cqrs.dto.TransactionRecordedReply;
// Assuming an interface for transaction operations. This would be implemented elsewhere.
// import com.webbanking.transactionservice.application.ports.in.TransactionUseCases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FundTransferSagaListener {

    private static final Logger logger = LoggerFactory.getLogger(FundTransferSagaListener.class);

    // Placeholder for actual transaction service/use cases
    // @Autowired
    // private TransactionUseCases transactionUseCases;

    @Autowired
    private KafkaTemplate<String, TransactionRecordedReply> kafkaTemplate;

    @Value("${app.kafka.topics.transaction-record-command}")
    private String transactionRecordCommandTopic;

    @Value("${app.kafka.topics.transaction-recorded-reply}")
    private String transactionRecordedReplyTopic;

    @KafkaListener(topics = "${app.kafka.topics.transaction-record-command}", groupId = "${spring.kafka.consumer.group-id:transaction-service-group}")
    public void handleRecordTransactionCommand(RecordTransactionCommand command) {
        logger.info("Received RecordTransactionCommand for sagaId: {}, transactionId: {}, type: {}",
                command.sagaId(), command.transactionId(), command.transactionType());

        boolean success = false;
        String failureReason = null;

        try {
            // --- Placeholder for actual transaction recording logic ---
            // This is where you would call your domain service or use case to persist the transaction.
            // For example:
            // transactionUseCases.recordTransaction(command);
            // For this placeholder, we'll simulate success.
            logger.info("Simulating successful transaction recording for sagaId: {}, transactionId: {}", command.sagaId(), command.transactionId());
            success = true;
            // --- End of Placeholder ---

            if (success) {
                logger.info("Transaction recording successful for sagaId: {}, transactionId: {}.",
                        command.sagaId(), command.transactionId());
            } else {
                // This path would be taken if the placeholder logic had a failure condition.
                failureReason = "Placeholder: Simulated transaction recording failure in TransactionService.";
                logger.error("Transaction recording failed for sagaId: {}, transactionId: {}. Reason: {}",
                        command.sagaId(), command.transactionId(), failureReason);
            }

        } catch (Exception e) {
            logger.error("Exception during transaction recording for sagaId: {}, transactionId: {}. Error: {}",
                    command.sagaId(), command.transactionId(), e.getMessage(), e);
            success = false;
            failureReason = "Exception during transaction recording: " + e.getMessage();
        }

        TransactionRecordedReply reply = new TransactionRecordedReply(
                command.sagaId(),
                command.transactionId(),
                success,
                failureReason
        );

        kafkaTemplate.send(transactionRecordedReplyTopic, command.sagaId(), reply);
        logger.info("Sent TransactionRecordedReply for sagaId: {}. Success: {}", command.sagaId(), success);
    }
} 