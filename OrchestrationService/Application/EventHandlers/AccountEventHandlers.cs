using System;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using OrchestrationService.Application.Models.Events;
using OrchestrationService.Application.Services;

namespace OrchestrationService.Application.EventHandlers;

/// <summary>
/// Handles events from the Account service
/// </summary>
public class AccountEventHandlers
{
    private readonly FundTransferOrchestrator _fundTransferOrchestrator;
    private readonly ILogger<AccountEventHandlers> _logger;

    /// <summary>
    /// Constructor
    /// </summary>
    public AccountEventHandlers(
        FundTransferOrchestrator fundTransferOrchestrator,
        ILogger<AccountEventHandlers> logger)
    {
        _fundTransferOrchestrator = fundTransferOrchestrator ?? throw new ArgumentNullException(nameof(fundTransferOrchestrator));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    /// <summary>
    /// Handle account debited events
    /// </summary>
    public async Task HandleAccountDebitedEventAsync(AccountDebitedEvent @event, string key, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Received AccountDebitedEvent for transaction {TransactionId}", @event.TransactionId);
        
        await _fundTransferOrchestrator.HandleSourceAccountDebitedAsync(
            @event.TransactionId,
            @event.AccountId,
            @event.Amount,
            @event.Currency,
            cancellationToken);
    }

    /// <summary>
    /// Handle account credited events
    /// </summary>
    public async Task HandleAccountCreditedEventAsync(AccountCreditedEvent @event, string key, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Received AccountCreditedEvent for transaction {TransactionId}", @event.TransactionId);
        
        await _fundTransferOrchestrator.HandleDestinationAccountCreditedAsync(
            @event.TransactionId,
            @event.AccountId,
            @event.Amount,
            @event.Currency,
            cancellationToken);
    }

    /// <summary>
    /// Handle account debit failed events
    /// </summary>
    public Task HandleAccountDebitFailedEventAsync(AccountDebitFailedEvent @event, string key, CancellationToken cancellationToken)
    {
        _logger.LogWarning("Received AccountDebitFailedEvent for transaction {TransactionId}: {Reason}",
            @event.TransactionId, @event.Reason);
        
        // No need to handle this in the fund transfer saga because the process
        // didn't even get started properly - no source account debit happened
        
        return Task.CompletedTask;
    }

    /// <summary>
    /// Handle account credit failed events
    /// </summary>
    public async Task HandleAccountCreditFailedEventAsync(AccountCreditFailedEvent @event, string key, CancellationToken cancellationToken)
    {
        _logger.LogWarning("Received AccountCreditFailedEvent for transaction {TransactionId}: {Reason}",
            @event.TransactionId, @event.Reason);
        
        await _fundTransferOrchestrator.HandleDestinationAccountCreditFailedAsync(
            @event.TransactionId,
            @event.AccountId,
            @event.Reason,
            cancellationToken);
    }

    /// <summary>
    /// Handle account debit compensated events
    /// </summary>
    public async Task HandleAccountDebitCompensatedEventAsync(AccountDebitCompensatedEvent @event, string key, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Received AccountDebitCompensatedEvent for transaction {TransactionId}", @event.TransactionId);
        
        await _fundTransferOrchestrator.HandleSourceAccountCompensatedAsync(
            @event.TransactionId,
            @event.AccountId,
            @event.Amount,
            @event.Currency,
            cancellationToken);
    }
} 