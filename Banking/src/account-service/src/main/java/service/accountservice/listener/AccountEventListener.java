package service.accountservice.listener;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.accountservice.entity.AccountEntity;


/**
 * Event listener for Account entity events.
 */
public class AccountEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AccountEventListener.class);

    @PrePersist
    public void prePersist(AccountEntity account) {
        // Automatically set the creation timestamp if not set
        if (account.getCreatedAt() == null) {
            account.setCreatedAt(java.time.LocalDateTime.now());
        }

        // Log the creation of the account
        logger.info("Account created with ID: {}", account.getId());
    }

    @PostPersist
    public void postPersist(AccountEntity account) {
        // Logic after the account is persisted
        logger.info("Account persisted with ID: {}", account.getId());

        // Example: Send a message to a message broker (e.g., Kafka)
        // sendMessageToBroker(account);
    }

    @PreUpdate
    public void preUpdate(AccountEntity account) {
        // Logic before the account is updated
        logger.info("Account with ID {} is about to be updated", account.getId());

        // Example: Update the timestamp when the account is updated
        account.setUpdatedAt(java.time.LocalDateTime.now());
    }

    @PostUpdate
    public void postUpdate(AccountEntity account) {
        // Logic after the account is updated
        logger.info("Account with ID {} has been updated", account.getId());

        // Example: Trigger an external service call
        // triggerExternalService(account);
    }

    @PreRemove
    public void preRemove(AccountEntity account) {
        // Logic before the account is removed
        logger.info("Account with ID {} is about to be removed", account.getId());

        // Example: Archive the account before deletion
        // archiveAccount(account);
    }

    @PostRemove
    public void postRemove(AccountEntity account) {
        // Logic after the account is removed
        logger.info("Account with ID {} has been removed", account.getId());
    }

    // Example method to send a message to a message broker
    private void sendMessageToBroker(AccountEntity account) {
        // Implementation for sending a message to a message broker
        logger.info("Sending account information to message broker: {}", account);
    }

    // Example method to trigger an external service
    private void triggerExternalService(AccountEntity account) {
        // Implementation for triggering an external service
        logger.info("Triggering external service for account: {}", account.getId());
    }

    // Example method to archive an account
    private void archiveAccount(AccountEntity account) {
        // Implementation for archiving an account
        logger.info("Archiving account: {}", account.getId());
    }
}