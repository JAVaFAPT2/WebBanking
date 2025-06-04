using System;
using OrchestrationService.Domain.Events;

namespace OrchestrationService.Domain.Models;

/// <summary>
/// Saga for orchestrating fund transfers between accounts
/// </summary>
public class FundTransferSaga : Saga
{
    /// <summary>
    /// Unique transaction identifier
    /// </summary>
    public string TransactionId { get; private set; }
    
    /// <summary>
    /// User who initiated the transfer
    /// </summary>
    public string UserId { get; private set; }
    
    /// <summary>
    /// Source account ID
    /// </summary>
    public string SourceAccountId { get; private set; }
    
    /// <summary>
    /// Destination account ID
    /// </summary>
    public string DestinationAccountId { get; private set; }
    
    /// <summary>
    /// Amount to transfer
    /// </summary>
    public decimal Amount { get; private set; }
    
    /// <summary>
    /// Currency of the transaction
    /// </summary>
    public string Currency { get; private set; }
    
    /// <summary>
    /// Reference/description for the transaction
    /// </summary>
    public string Reference { get; private set; }

    /// <summary>
    /// Whether funds have been debited from the source account
    /// </summary>
    public bool SourceAccountDebited { get; private set; }
    
    /// <summary>
    /// Whether funds have been credited to the destination account
    /// </summary>
    public bool DestinationAccountCredited { get; private set; }
    
    /// <summary>
    /// Whether notification has been sent
    /// </summary>
    public bool NotificationSent { get; private set; }

    // For ORM
    private FundTransferSaga() { }

    /// <summary>
    /// Create a new fund transfer saga
    /// </summary>
    public FundTransferSaga(
        string userId,
        string sourceAccountId,
        string destinationAccountId,
        decimal amount,
        string currency,
        string reference)
    {
        TransactionId = Guid.NewGuid().ToString();
        UserId = userId ?? throw new ArgumentNullException(nameof(userId));
        SourceAccountId = sourceAccountId ?? throw new ArgumentNullException(nameof(sourceAccountId));
        DestinationAccountId = destinationAccountId ?? throw new ArgumentNullException(nameof(destinationAccountId));
        
        if (amount <= 0)
            throw new ArgumentException("Amount must be greater than zero", nameof(amount));
        
        Amount = amount;
        Currency = currency ?? throw new ArgumentNullException(nameof(currency));
        Reference = reference ?? string.Empty;
        
        TotalSteps = 3; // Debit, Credit, Notify
        
        // Raise domain event
        AddDomainEvent(new FundTransferSagaCreatedEvent(
            Id,
            TransactionId,
            UserId,
            SourceAccountId,
            DestinationAccountId,
            Amount,
            Currency,
            Reference));
    }

    /// <summary>
    /// Mark the source account as debited
    /// </summary>
    public void MarkSourceAccountDebited()
    {
        if (State != SagaState.Running)
            throw new InvalidOperationException($"Cannot mark source account debited when saga is in {State} state");
        
        SourceAccountDebited = true;
        CurrentStep++;
        LastUpdatedAt = DateTime.UtcNow;
        
        AddDomainEvent(new SourceAccountDebitedEvent(Id, TransactionId, SourceAccountId, Amount, Currency));
    }

    /// <summary>
    /// Mark the destination account as credited
    /// </summary>
    public void MarkDestinationAccountCredited()
    {
        if (State != SagaState.Running)
            throw new InvalidOperationException($"Cannot mark destination account credited when saga is in {State} state");
        
        if (!SourceAccountDebited)
            throw new InvalidOperationException("Cannot credit destination account before debiting source account");
        
        DestinationAccountCredited = true;
        CurrentStep++;
        LastUpdatedAt = DateTime.UtcNow;
        
        AddDomainEvent(new DestinationAccountCreditedEvent(Id, TransactionId, DestinationAccountId, Amount, Currency));
    }

    /// <summary>
    /// Mark notification as sent
    /// </summary>
    public void MarkNotificationSent()
    {
        if (State != SagaState.Running)
            throw new InvalidOperationException($"Cannot mark notification sent when saga is in {State} state");
        
        NotificationSent = true;
        CurrentStep++;
        LastUpdatedAt = DateTime.UtcNow;
        
        AddDomainEvent(new TransferNotificationSentEvent(Id, TransactionId, UserId));
        
        // If all steps completed, mark saga as complete
        if (CurrentStep >= TotalSteps)
        {
            Complete();
            AddDomainEvent(new FundTransferCompletedEvent(Id, TransactionId));
        }
    }

    /// <summary>
    /// Mark destination account credit as failed and start compensation
    /// </summary>
    public void FailDestinationAccountCredit(string errorMessage)
    {
        if (State != SagaState.Running)
            throw new InvalidOperationException($"Cannot fail destination credit when saga is in {State} state");
        
        Fail(errorMessage);
        AddDomainEvent(new DestinationAccountCreditFailedEvent(Id, TransactionId, DestinationAccountId, errorMessage));
    }

    /// <summary>
    /// Mark source account compensation (re-crediting) as completed
    /// </summary>
    public void MarkSourceAccountCompensated()
    {
        if (State != SagaState.Compensating)
            throw new InvalidOperationException($"Cannot mark source account compensated when saga is in {State} state");
        
        SourceAccountDebited = false;
        MarkCompensationCompleted();
        
        AddDomainEvent(new FundTransferCompensatedEvent(Id, TransactionId, ErrorMessage));
    }
} 