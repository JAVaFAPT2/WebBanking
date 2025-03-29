package service.orchestrationservice.workflow;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.orchestrationservice.client.*;
import service.shared.event.OrchestrationEvent;
import service.shared.event.OrchestrationStatus;
import service.orchestrationservice.model.NotificationRequest;
import service.orchestrationservice.model.TransactionRequest;
import service.orchestrationservice.model.TransferRequest;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class SagaOrchestrator {

    private final UserServiceClient userServiceClient;
    private final AccountServiceClient accountServiceClient;
    private final FundTransferServiceClient fundTransferServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public SagaOrchestrator(UserServiceClient userServiceClient,
                            AccountServiceClient accountServiceClient,
                            FundTransferServiceClient fundTransferServiceClient,
                            TransactionServiceClient transactionServiceClient,
                            NotificationServiceClient notificationServiceClient) {
        this.userServiceClient = userServiceClient;
        this.accountServiceClient = accountServiceClient;
        this.fundTransferServiceClient = fundTransferServiceClient;
        this.transactionServiceClient = transactionServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    public Mono<OrchestrationEvent> orchestrateTransfer(OrchestrationEvent event) {
        return Mono.fromCallable(() -> {
            // Validate user
            var user = userServiceClient.getUserById(event.getUserId()).block(Duration.ofMillis(1000));
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            // Validate accounts
            var fromAccount = accountServiceClient.getAccountById(event.getFromAccountId()).block(Duration.ofMillis(1000));
            if (fromAccount == null) {
                throw new RuntimeException("Source account not found");
            }

            var toAccount = accountServiceClient.getAccountById(event.getToAccountId()).block(Duration.ofMillis(1000));
            if (toAccount == null) {
                throw new RuntimeException("Destination account not found");
            }

            // Check if user owns the source account
            if (!fromAccount.getId().equals(user.getId())) {
                throw new RuntimeException("User does not own the source account");
            }

            // Check sufficient balance in source account
            if (fromAccount.getBalance().compareTo(BigDecimal.valueOf(event.getAmount())) < 0) {
                throw new RuntimeException("Insufficient funds");
            }


            // Initiate fund transfer
            var transfer = fundTransferServiceClient.initiateTransfer(
                    new TransferRequest(
                            event.getFromAccountId(),
                            event.getToAccountId(),
                            event.getAmount()
                    )
            ).block();

            // Record transaction
            var transaction = transactionServiceClient.recordTransaction(
                    new TransactionRequest(
                            event.getUserId(),
                            event.getFromAccountId(),
                            event.getToAccountId(),
                            event.getAmount(),
                            "TRANSFER"
                    )
            ).block();

            // Send notification
            notificationServiceClient.sendNotification(
                    new NotificationRequest(
                            event.getUserId(),
                            "Your transfer of " + event.getAmount() + " was successful"
                    )
            ).block();

            return new OrchestrationEvent(
                    event.getOrchestrationId(),
                    OrchestrationStatus.COMPLETED,
                    "Transfer COMPLETED: "
            );
        }).onErrorResume(e -> {            // Handle compensation logic here
            return Mono.just(new OrchestrationEvent(
                    event.getOrchestrationId(),
                    OrchestrationStatus.FAILED,
                    "Transfer failed: " + e.getMessage()
            ));
        });
    }
}