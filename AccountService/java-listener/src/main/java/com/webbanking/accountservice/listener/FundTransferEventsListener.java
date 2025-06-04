package com.webbanking.accountservice.listener;

import com.webbanking.accountservice.dto.AccountDebitReply;
import com.webbanking.accountservice.dto.DebitAccountCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FundTransferEventsListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FundTransferEventsListener.class);

    private final KafkaTemplate<String, AccountDebitReply> kafkaTemplate;
    private static final String DEBIT_REPLY_TOPIC = "fundtransfer.account.debit.reply"; // Ensure this matches orchestrator's expectation

    // In-memory balance for simulation
    private BigDecimal accountBalance = new BigDecimal("1000.00"); 

    @Autowired
    public FundTransferEventsListener(KafkaTemplate<String, AccountDebitReply> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "fundtransfer.account.debit.command", groupId = "account-listener-group",
                   containerFactory = "kafkaListenerContainerFactory") // Assuming a container factory is configured
    public void handleDebitAccountCommand(@Payload DebitAccountCommand command) {
        LOGGER.info("Received DebitAccountCommand: {}", command);

        boolean success = false;
        String failureReason = null;
        BigDecimal newBalance = accountBalance;

        // Simulate debit logic (replace with actual logic or call to .NET service later)
        if (accountBalance.compareTo(command.amount()) >= 0) {
            newBalance = accountBalance.subtract(command.amount());
            accountBalance = newBalance; // Update simulated balance
            success = true;
            LOGGER.info("Account {} debited successfully. New balance: {}", command.accountId(), newBalance);
        } else {
            failureReason = "Insufficient funds.";
            LOGGER.warn("Failed to debit account {}: {}", command.accountId(), failureReason);
        }

        AccountDebitReply reply = new AccountDebitReply(
                command.sagaId(),
                command.globalTransactionId(),
                command.accountId(),
                success,
                failureReason,
                success ? newBalance : accountBalance // send current balance regardless of outcome
        );

        LOGGER.info("Sending AccountDebitReply: {}", reply);
        kafkaTemplate.send(DEBIT_REPLY_TOPIC, command.sagaId(), reply); // Use sagaId as key for partitioning consistency
    }
    
    // TODO: Add listener for CompensateDebitAccountCommand if needed
    // @KafkaListener(topics = "fundtransfer.account.debit.compensate.command", ...)
    // public void handleCompensateDebitCommand(...) { ... }
} 