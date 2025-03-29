package service.accountservice.command.internal;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import service.accountservice.command.api.CreateAccountCommand;
import service.accountservice.command.api.UpdateAccountCommand;
import service.accountservice.command.internal.model.AccountWriteModel;
import service.accountservice.repository.AccountWriteRepository;
import service.shared.event.AccountEvent;
import service.shared.exception.BankingException;
import service.shared.models.Account;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

public class AccountCommandHandler {

    private final AccountWriteRepository accountWriteRepository;
    private final Validator validator;


    public AccountCommandHandler(AccountWriteRepository accountWriteRepository, Validator validator) {
        this.accountWriteRepository = accountWriteRepository;
        this.validator = validator;
    }

    @CommandHandler
    public void handleCreateAccountCommand(CreateAccountCommand command) {
        // Validate command
        Set<ConstraintViolation<CreateAccountCommand>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            throw new BankingException("Command validation failed: " + violations.toString());
        }

        // Check if account already exists (assumes repository provides a findByAccountNumber method)
        if (accountWriteRepository.findByAccountNumber(command.getAccountNumber()).isPresent()) {
            throw new BankingException("Account already exists");
        }

        // Create new AccountWriteModel instance
        AccountWriteModel account = new AccountWriteModel(
                command.getId(),
                command.getAccountNumber(),
                command.getAccountType(),
                command.getBalance(),
                command.getCurrency(),      // Assumes CreateAccountCommand includes currency field
                command.getStatus(),        // Assumes status is provided
                command.getOwnerType(),     // Assumes ownerType is provided
                command.getUserId(),        // Assumes userId is provided
                null                        // Transaction IDs; initially null or an empty set as per your design
        );

        // Save to repository
        accountWriteRepository.save(account);

        // Build a domain Account object to include in the event
        Account accountObject = new Account();
        accountObject.setId(account.getId());
        accountObject.setAccountNumber(account.getAccountNumber());
        accountObject.setAccountType(account.getAccountType());
        accountObject.setBalance(account.getBalance());
        accountObject.setCurrency(account.getCurrency());
        accountObject.setStatus(account.getStatus());
        accountObject.setOwnerType(account.getOwnerType());
        // Set additional fields if necessary

        // Create and apply an AccountEvent for creation
        AccountEvent event = new AccountEvent(
                account.getId(),
                "ACCOUNT_CREATED",
                accountObject,
                LocalDateTime.now(),
                Map.of("commandType", "CreateAccountCommand")
        );
        apply(event);
    }

    @CommandHandler
    public void handleUpdateAccountCommand(UpdateAccountCommand command) {
        // Validate command
        Set<ConstraintViolation<UpdateAccountCommand>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            throw new BankingException("Command validation failed: " + violations.toString());
        }

        // Retrieve existing account or throw an exception if not found
        AccountWriteModel account = accountWriteRepository.findById(command.getAccountId())
                .orElseThrow(() -> new BankingException("Account not found"));

        // Update account details if provided
        if (command.getAccountNumber() != null) {
            account.setAccountNumber(command.getAccountNumber());
        }
        if (command.getAccountType() != null) {
            account.setAccountType(command.getAccountType());
        }
        if (command.getBalance() != null) {
            account.setBalance(command.getBalance());
        }
        if (command.getCurrency() != null) {
            account.setCurrency(command.getCurrency());
        }
        if (command.getStatus() != null) {
            account.setStatus(command.getStatus());
        }

        if (command.getUserId() != null) {
            account.setUserId(command.getUserId());
        }

        // Save the updated account
        accountWriteRepository.save(account);

        // Build a domain Account object with updated information
        Account accountObject = new Account();
        accountObject.setId(account.getId());
        accountObject.setAccountNumber(account.getAccountNumber());
        accountObject.setAccountType(account.getAccountType());
        accountObject.setBalance(account.getBalance());
        accountObject.setCurrency(account.getCurrency());
        accountObject.setStatus(account.getStatus());
        accountObject.setOwnerType(account.getOwnerType());
        // Set additional fields if needed

        // Create and apply an AccountEvent for update
        AccountEvent event = new AccountEvent(
                account.getId(),
                "ACCOUNT_UPDATED",
                accountObject,
                LocalDateTime.now(),
                Map.of("commandType", "UpdateAccountCommand")
        );
        apply(event);
    }

    @EventSourcingHandler
    public void onAccountCreatedEvent(AccountEvent event) {
        if ("ACCOUNT_CREATED".equals(event.getEventType())) {
            Account account = event.getAccount();
            AccountWriteModel accountWriteModel = new AccountWriteModel(
                    account.getId(),
                    account.getAccountNumber(),
                    account.getAccountType(),
                    account.getBalance(),
                    account.getCurrency(),
                    account.getStatus(),
                    account.getOwnerType(),
                    account.getUser() != null ? account.getUser().getId() : null,
                    null // Initialize transactionIds as needed (e.g., new HashSet<>())
            );
            accountWriteRepository.save(accountWriteModel);
        }
    }

    @EventSourcingHandler
    public void onAccountUpdatedEvent(AccountEvent event) {
        if ("ACCOUNT_UPDATED".equals(event.getEventType())) {
            Account account = event.getAccount();
            AccountWriteModel accountWriteModel = new AccountWriteModel(
                    account.getId(),
                    account.getAccountNumber(),
                    account.getAccountType(),
                    account.getBalance(),
                    account.getCurrency(),
                    account.getStatus(),
                    account.getOwnerType(),
                    account.getUser() != null ? account.getUser().getId() : null,
                    null // Initialize transactionIds as needed
            );
            accountWriteRepository.save(accountWriteModel);
        }
    }
}
