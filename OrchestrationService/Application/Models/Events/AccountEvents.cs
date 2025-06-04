namespace OrchestrationService.Application.Models.Events;

/// <summary>
/// Event indicating an account was successfully debited
/// </summary>
/// <param name="TransactionId">Unique transaction identifier</param>
/// <param name="SagaId">ID of the orchestrating saga</param>
/// <param name="AccountId">Account that was debited</param>
/// <param name="Amount">Amount debited</param>
/// <param name="Currency">Currency of the amount</param>
/// <param name="Balance">New account balance</param>
public record AccountDebitedEvent(
    string TransactionId,
    string SagaId,
    string AccountId,
    decimal Amount,
    string Currency,
    decimal Balance);

/// <summary>
/// Event indicating an account was successfully credited
/// </summary>
/// <param name="TransactionId">Unique transaction identifier</param>
/// <param name="SagaId">ID of the orchestrating saga</param>
/// <param name="AccountId">Account that was credited</param>
/// <param name="Amount">Amount credited</param>
/// <param name="Currency">Currency of the amount</param>
/// <param name="Balance">New account balance</param>
public record AccountCreditedEvent(
    string TransactionId,
    string SagaId,
    string AccountId,
    decimal Amount,
    string Currency,
    decimal Balance);

/// <summary>
/// Event indicating a debit operation failed
/// </summary>
/// <param name="TransactionId">Unique transaction identifier</param>
/// <param name="SagaId">ID of the orchestrating saga</param>
/// <param name="AccountId">Account that failed to be debited</param>
/// <param name="Amount">Amount that failed to be debited</param>
/// <param name="Currency">Currency of the amount</param>
/// <param name="Reason">Reason for the failure</param>
public record AccountDebitFailedEvent(
    string TransactionId,
    string SagaId,
    string AccountId,
    decimal Amount,
    string Currency,
    string Reason);

/// <summary>
/// Event indicating a credit operation failed
/// </summary>
/// <param name="TransactionId">Unique transaction identifier</param>
/// <param name="SagaId">ID of the orchestrating saga</param>
/// <param name="AccountId">Account that failed to be credited</param>
/// <param name="Amount">Amount that failed to be credited</param>
/// <param name="Currency">Currency of the amount</param>
/// <param name="Reason">Reason for the failure</param>
public record AccountCreditFailedEvent(
    string TransactionId,
    string SagaId,
    string AccountId,
    decimal Amount,
    string Currency,
    string Reason);

/// <summary>
/// Event indicating a debit compensation (re-credit) was successful
/// </summary>
/// <param name="TransactionId">Unique transaction identifier</param>
/// <param name="SagaId">ID of the orchestrating saga</param>
/// <param name="AccountId">Account that was re-credited</param>
/// <param name="Amount">Amount re-credited</param>
/// <param name="Currency">Currency of the amount</param>
/// <param name="Balance">New account balance</param>
public record AccountDebitCompensatedEvent(
    string TransactionId,
    string SagaId,
    string AccountId,
    decimal Amount,
    string Currency,
    decimal Balance); 