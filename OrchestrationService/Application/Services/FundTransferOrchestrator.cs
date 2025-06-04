using System;
using System.Threading;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Logging;
using OrchestrationService.Application.Models.Commands;
using OrchestrationService.Application.Models.Events;
using OrchestrationService.Domain.Events;
using OrchestrationService.Domain.Interfaces;
using OrchestrationService.Domain.Models;

namespace OrchestrationService.Application.Services;

/// <summary>
/// Orchestrates the process of transferring funds between accounts
/// using the saga pattern
/// </summary>
public class FundTransferOrchestrator
{
    private readonly ISagaRepository<FundTransferSaga> _sagaRepository;
    private readonly IMessageBroker _messageBroker;
    private readonly IMediator _mediator;
    private readonly ILogger<FundTransferOrchestrator> _logger;

    /// <summary>
    /// Constructor
    /// </summary>
    public FundTransferOrchestrator(
        ISagaRepository<FundTransferSaga> sagaRepository,
        IMessageBroker messageBroker,
        IMediator mediator,
        ILogger<FundTransferOrchestrator> logger)
    {
        _sagaRepository = sagaRepository ?? throw new ArgumentNullException(nameof(sagaRepository));
        _messageBroker = messageBroker ?? throw new ArgumentNullException(nameof(messageBroker));
        _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    /// <summary>
    /// Start a new fund transfer saga
    /// </summary>
    public async Task<string> StartFundTransferAsync(
        string userId,
        string sourceAccountId,
        string destinationAccountId,
        decimal amount,
        string currency,
        string reference,
        CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Starting fund transfer saga for user {UserId} from account {SourceAccountId} to {DestinationAccountId} for {Amount} {Currency}",
            userId, sourceAccountId, destinationAccountId, amount, currency);

        // Create the saga
        var saga = new FundTransferSaga(
            userId,
            sourceAccountId,
            destinationAccountId,
            amount,
            currency,
            reference);

        // Save the saga
        await _sagaRepository.SaveAsync(saga, cancellationToken);

        // Start the saga
        saga.Start();
        await _sagaRepository.UpdateAsync(saga, cancellationToken);

        // Publish domain events
        foreach (var domainEvent in saga.DomainEvents)
        {
            await _mediator.Publish(domainEvent, cancellationToken);
        }
        saga.ClearDomainEvents();

        // Publish command to debit source account
        await _messageBroker.PublishAsync(
            "account-commands",
            saga.TransactionId,
            new DebitAccountCommand(
                saga.TransactionId,
                saga.Id.ToString(),
                saga.SourceAccountId,
                saga.Amount,
                saga.Currency,
                saga.Reference),
            cancellationToken);

        return saga.TransactionId;
    }

    /// <summary>
    /// Handle source account debited event
    /// </summary>
    public async Task HandleSourceAccountDebitedAsync(
        string transactionId,
        string accountId,
        decimal amount,
        string currency,
        CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Handling source account debited event for transaction {TransactionId}", transactionId);

        // Get the saga
        var saga = await _sagaRepository.GetByTransactionIdAsync(transactionId, cancellationToken);
        if (saga == null)
        {
            _logger.LogWarning("Saga not found for transaction {TransactionId}", transactionId);
            return;
        }

        // Update the saga
        saga.MarkSourceAccountDebited();
        await _sagaRepository.UpdateAsync(saga, cancellationToken);

        // Publish domain events
        foreach (var domainEvent in saga.DomainEvents)
        {
            await _mediator.Publish(domainEvent, cancellationToken);
        }
        saga.ClearDomainEvents();

        // Publish command to credit destination account
        await _messageBroker.PublishAsync(
            "account-commands",
            saga.TransactionId,
            new CreditAccountCommand(
                saga.TransactionId,
                saga.Id.ToString(),
                saga.DestinationAccountId,
                saga.Amount,
                saga.Currency,
                saga.Reference),
            cancellationToken);
    }

    /// <summary>
    /// Handle destination account credited event
    /// </summary>
    public async Task HandleDestinationAccountCreditedAsync(
        string transactionId,
        string accountId,
        decimal amount,
        string currency,
        CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Handling destination account credited event for transaction {TransactionId}", transactionId);

        // Get the saga
        var saga = await _sagaRepository.GetByTransactionIdAsync(transactionId, cancellationToken);
        if (saga == null)
        {
            _logger.LogWarning("Saga not found for transaction {TransactionId}", transactionId);
            return;
        }

        // Update the saga
        saga.MarkDestinationAccountCredited();
        await _sagaRepository.UpdateAsync(saga, cancellationToken);

        // Publish domain events
        foreach (var domainEvent in saga.DomainEvents)
        {
            await _mediator.Publish(domainEvent, cancellationToken);
        }
        saga.ClearDomainEvents();

        // Publish command to send notification
        await _messageBroker.PublishAsync(
            "notification-commands",
            saga.TransactionId,
            new SendNotificationCommand(
                saga.TransactionId,
                saga.Id.ToString(),
                saga.UserId,
                NotificationType.Email, // This could be configurable or user-preference based
                $"Fund transfer of {saga.Amount} {saga.Currency} completed",
                $"Your fund transfer from account {saga.SourceAccountId} to account {saga.DestinationAccountId} " +
                $"for {saga.Amount} {saga.Currency} has been completed successfully."),
            cancellationToken);
    }

    /// <summary>
    /// Handle notification sent event
    /// </summary>
    public async Task HandleNotificationSentAsync(
        string transactionId,
        string notificationId,
        CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Handling notification sent event for transaction {TransactionId}", transactionId);

        // Get the saga
        var saga = await _sagaRepository.GetByTransactionIdAsync(transactionId, cancellationToken);
        if (saga == null)
        {
            _logger.LogWarning("Saga not found for transaction {TransactionId}", transactionId);
            return;
        }

        // Update the saga
        saga.MarkNotificationSent();
        await _sagaRepository.UpdateAsync(saga, cancellationToken);

        // Publish domain events
        foreach (var domainEvent in saga.DomainEvents)
        {
            await _mediator.Publish(domainEvent, cancellationToken);
        }
        saga.ClearDomainEvents();
    }

    /// <summary>
    /// Handle destination account credit failed event
    /// </summary>
    public async Task HandleDestinationAccountCreditFailedAsync(
        string transactionId,
        string accountId,
        string errorMessage,
        CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Handling destination account credit failed event for transaction {TransactionId}: {ErrorMessage}",
            transactionId, errorMessage);

        // Get the saga
        var saga = await _sagaRepository.GetByTransactionIdAsync(transactionId, cancellationToken);
        if (saga == null)
        {
            _logger.LogWarning("Saga not found for transaction {TransactionId}", transactionId);
            return;
        }

        // Update the saga
        saga.FailDestinationAccountCredit(errorMessage);
        await _sagaRepository.UpdateAsync(saga, cancellationToken);

        // Publish domain events
        foreach (var domainEvent in saga.DomainEvents)
        {
            await _mediator.Publish(domainEvent, cancellationToken);
        }
        saga.ClearDomainEvents();

        // Publish command to compensate (re-credit) source account
        await _messageBroker.PublishAsync(
            "account-commands",
            saga.TransactionId,
            new CompensateAccountDebitCommand(
                saga.TransactionId,
                saga.Id.ToString(),
                saga.SourceAccountId,
                saga.Amount,
                saga.Currency,
                $"Reversal of transfer to {saga.DestinationAccountId}: {errorMessage}"),
            cancellationToken);
    }

    /// <summary>
    /// Handle source account compensated event
    /// </summary>
    public async Task HandleSourceAccountCompensatedAsync(
        string transactionId,
        string accountId,
        decimal amount,
        string currency,
        CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Handling source account compensated event for transaction {TransactionId}", transactionId);

        // Get the saga
        var saga = await _sagaRepository.GetByTransactionIdAsync(transactionId, cancellationToken);
        if (saga == null)
        {
            _logger.LogWarning("Saga not found for transaction {TransactionId}", transactionId);
            return;
        }

        // Update the saga
        saga.MarkSourceAccountCompensated();
        await _sagaRepository.UpdateAsync(saga, cancellationToken);

        // Publish domain events
        foreach (var domainEvent in saga.DomainEvents)
        {
            await _mediator.Publish(domainEvent, cancellationToken);
        }
        saga.ClearDomainEvents();

        // Send failure notification to user
        await _messageBroker.PublishAsync(
            "notification-commands",
            saga.TransactionId,
            new SendNotificationCommand(
                saga.TransactionId,
                saga.Id.ToString(),
                saga.UserId,
                NotificationType.Email,
                $"Fund transfer of {saga.Amount} {saga.Currency} failed",
                $"Your fund transfer from account {saga.SourceAccountId} to account {saga.DestinationAccountId} " +
                $"for {saga.Amount} {saga.Currency} has failed: {saga.ErrorMessage}. " +
                $"Your source account has been re-credited."),
            cancellationToken);
    }
} 