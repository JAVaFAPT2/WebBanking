namespace OrchestrationService.Application.Models.Commands;

/// <summary>
/// Command to debit (withdraw from) an account
/// </summary>
/// <param name="TransactionId">Unique transaction identifier</param>
/// <param name="SagaId">ID of the orchestrating saga</param>
/// <param name="AccountId">Account to debit</param>
/// <param name="Amount">Amount to debit</param>
/// <param name="Currency">Currency of the amount</param>
/// <param name="Description">Transaction description</param>
public record DebitAccountCommand(
    string TransactionId,
    string SagaId,
    string AccountId,
    decimal Amount,
    string Currency,
    string Description);

/// <summary>
/// Command to credit (deposit to) an account
/// </summary>
/// <param name="TransactionId">Unique transaction identifier</param>
/// <param name="SagaId">ID of the orchestrating saga</param>
/// <param name="AccountId">Account to credit</param>
/// <param name="Amount">Amount to credit</param>
/// <param name="Currency">Currency of the amount</param>
/// <param name="Description">Transaction description</param>
public record CreditAccountCommand(
    string TransactionId,
    string SagaId,
    string AccountId,
    decimal Amount,
    string Currency,
    string Description);

/// <summary>
/// Command to compensate (reverse) a debit operation
/// </summary>
/// <param name="TransactionId">Unique transaction identifier</param>
/// <param name="SagaId">ID of the orchestrating saga</param>
/// <param name="AccountId">Account to re-credit</param>
/// <param name="Amount">Amount to re-credit</param>
/// <param name="Currency">Currency of the amount</param>
/// <param name="Description">Transaction description</param>
public record CompensateAccountDebitCommand(
    string TransactionId,
    string SagaId,
    string AccountId,
    decimal Amount,
    string Currency,
    string Description); 