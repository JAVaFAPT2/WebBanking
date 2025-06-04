package com.webbanking.accountservice.application.messaging;

import com.webbanking.accountservice.application.cqrs.dto.CompensateDebitCommand;
import com.webbanking.accountservice.application.cqrs.dto.CompensateDebitReply;
import com.webbanking.accountservice.application.cqrs.dto.DebitAccountCommand;
import com.webbanking.accountservice.application.cqrs.dto.DebitAccountReply;
import com.webbanking.accountservice.application.cqrs.dto.CreditAccountCommand;
import com.webbanking.accountservice.application.cqrs.dto.CreditAccountReply;
// Assuming an interface for account operations. This would be implemented elsewhere.
// import com.webbanking.accountservice.application.ports.in.AccountUseCases; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID; // For generating a new transaction ID for the compensation

@Service
public class FundTransferSagaListener {

    private static final Logger logger = LoggerFactory.getLogger(FundTransferSagaListener.class);

    // Placeholder for actual account service/use cases
    // @Autowired
    // private AccountUseCases accountUseCases; 

    @Autowired
    private KafkaTemplate<String, CompensateDebitReply> kafkaTemplate;

    @Value("${app.kafka.topics.compensate-debit-command}")
    private String compensateDebitCommandTopic;

    @Value("${app.kafka.topics.compensate-debit-reply}")
    private String compensateDebitReplyTopic;

    // Topics for regular debit
    @Value("${app.kafka.topics.debit-account-command}")
    private String debitAccountCommandTopic;

    @Value("${app.kafka.topics.account-debit-reply}")
    private String accountDebitReplyTopic;

    // Topics for regular credit
    @Value("${app.kafka.topics.credit-account-command}")
    private String creditAccountCommandTopic;

    @Value("${app.kafka.topics.account-credit-reply}")
    private String accountCreditReplyTopic;

    @KafkaListener(topics = "${app.kafka.topics.debit-account-command}", groupId = "${spring.kafka.consumer.group-id:account-service-group}")
    public void handleDebitAccountCommand(DebitAccountCommand command) {
        logger.info("Received DebitAccountCommand for sagaId: {}, sourceAccountId: {}, amount: {}, transactionId: {}",
                command.sagaId(), command.sourceAccountId(), command.amount(), command.transactionId());

        boolean success = false;
        String failureReason = null;

        try {
            // --- Placeholder for actual debit logic ---
            // Example: Check balance, perform debit, save transaction state.
            // For this placeholder, we'll simulate success.
            // A real implementation would interact with a domain service/repository.
            if (command.amount().doubleValue() > 10000) { // Simulate a failure condition for large amounts
                 success = false;
                 failureReason = "Debit amount exceeds policy limit for simulation.";
                 logger.warn("Simulated debit failure for sagaId: {}, accountId: {}: {}", command.sagaId(), command.sourceAccountId(), failureReason);
            } else {
                logger.info("Simulating successful debit for sagaId: {}, accountId: {} with amount: {}", command.sagaId(), command.sourceAccountId(), command.amount());
                success = true;
            }
            // --- End of Placeholder ---
        } catch (Exception e) {
            logger.error("Exception during debit for sagaId: {}, accountId: {}. Error: {}",
                    command.sagaId(), command.sourceAccountId(), e.getMessage(), e);
            success = false;
            failureReason = "Exception during debit: " + e.getMessage();
        }

        DebitAccountReply reply = new DebitAccountReply(
                command.sagaId(),
                command.transactionId(),
                success,
                failureReason
        );

        kafkaTemplate.send(accountDebitReplyTopic, command.sagaId(), reply);
        logger.info("Sent DebitAccountReply for sagaId: {}. Success: {}", command.sagaId(), success);
    }

    @KafkaListener(topics = "${app.kafka.topics.credit-account-command}", groupId = "${spring.kafka.consumer.group-id:account-service-group}")
    public void handleCreditAccountCommand(CreditAccountCommand command) {
        logger.info("Received CreditAccountCommand for sagaId: {}, destinationAccountId: {}, amount: {}, transactionId: {}",
                command.sagaId(), command.destinationAccountId(), command.amount(), command.transactionId());

        boolean success = false;
        String failureReason = null;

        try {
            // --- Placeholder for actual credit logic ---
            // Example: Perform credit, save transaction state.
            // For this placeholder, we'll simulate success.
            logger.info("Simulating successful credit for sagaId: {}, accountId: {} with amount: {}", command.sagaId(), command.destinationAccountId(), command.amount());
            success = true;
            // --- End of Placeholder ---
        } catch (Exception e) {
            logger.error("Exception during credit for sagaId: {}, accountId: {}. Error: {}",
                    command.sagaId(), command.destinationAccountId(), e.getMessage(), e);
            success = false;
            failureReason = "Exception during credit: " + e.getMessage();
        }

        CreditAccountReply reply = new CreditAccountReply(
                command.sagaId(),
                command.transactionId(),
                success,
                failureReason
        );

        kafkaTemplate.send(accountCreditReplyTopic, command.sagaId(), reply);
        logger.info("Sent CreditAccountReply for sagaId: {}. Success: {}", command.sagaId(), success);
    }

    @KafkaListener(topics = "${app.kafka.topics.compensate-debit-command}", groupId = "${spring.kafka.consumer.group-id:account-service-group}")
    public void handleCompensateDebitCommand(CompensateDebitCommand command) {
        logger.info("Received CompensateDebitCommand for sagaId: {}, originalDebitTxId: {}, accountId: {}, amount: {}",
                command.sagaId(), command.originalDebitTransactionId(), command.accountId(), command.amount());

        String compensationTransactionId = UUID.randomUUID().toString(); // Generate a new ID for this compensating transaction
        boolean success = false;
        String failureReason = null;

        try {
            // --- Placeholder for actual compensation logic ---
            // This is where you would call your domain service or use case to credit the account.
            // For example:
            // accountUseCases.creditAccount(command.accountId(), command.amount(), "COMPENSATION", command.originalDebitTransactionId());
            // For this placeholder, we'll simulate success.
            logger.info("Simulating successful compensation for accountId: {} with amount: {}", command.accountId(), command.amount());
            success = true;
            // --- End of Placeholder ---

            if (success) {
                logger.info("Debit compensation successful for sagaId: {}, accountId: {}. Compensation TxId: {}",
                        command.sagaId(), command.accountId(), compensationTransactionId);
            } else {
                // This path would be taken if the placeholder logic had a failure condition.
                failureReason = "Placeholder: Simulated compensation failure in AccountService.";
                logger.error("Debit compensation failed for sagaId: {}, accountId: {}. Reason: {}. Compensation TxId: {}",
                        command.sagaId(), command.accountId(), failureReason, compensationTransactionId);
            }

        } catch (Exception e) {
            logger.error("Exception during debit compensation for sagaId: {}, accountId: {}. Error: {}",
                    command.sagaId(), command.accountId(), e.getMessage(), e);
            success = false;
            failureReason = "Exception during compensation: " + e.getMessage();
        }

        CompensateDebitReply reply = new CompensateDebitReply(
                command.sagaId(),
                compensationTransactionId,
                success,
                failureReason
        );

        kafkaTemplate.send(compensateDebitReplyTopic, command.sagaId(), reply);
        logger.info("Sent CompensateDebitReply for sagaId: {}. Success: {}", command.sagaId(), success);
    }
} 