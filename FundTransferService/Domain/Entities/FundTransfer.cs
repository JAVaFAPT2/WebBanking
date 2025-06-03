using FundTransferService.Domain.Common;
using FundTransferService.Domain.ValueObjects;
using FundTransferService.Domain.Events;

namespace FundTransferService.Domain.Entities;

public enum FundTransferStatus
{
    Pending,
    Processing,
    Completed,
    Failed,
    Cancelled
}

public class FundTransfer : AggregateRoot
{
    public Guid Id { get; private set; }
    public Guid FromAccountId { get; private set; }
    public Guid ToAccountId { get; private set; }
    public Money Amount { get; private set; }
    public DateTime TransferDate { get; private set; }
    public FundTransferStatus Status { get; private set; }
    public string? ReferenceNumber { get; private set; }
    public string? FailureReason { get; private set; }
    public DateTime CreatedAt { get; private set; }
    public DateTime LastModifiedAt { get; private set; }

    private FundTransfer() { } // For EF Core

    public static FundTransfer Create(
        Guid fromAccountId,
        Guid toAccountId,
        Money amount,
        string? referenceNumber = null)
    {
        if (fromAccountId == Guid.Empty) throw new ArgumentException("FromAccountId cannot be empty.", nameof(fromAccountId));
        if (toAccountId == Guid.Empty) throw new ArgumentException("ToAccountId cannot be empty.", nameof(toAccountId));
        if (amount == null || amount.Amount <= 0) throw new ArgumentException("Amount must be positive.", nameof(amount));
        if (fromAccountId == toAccountId) throw new InvalidOperationException("Cannot transfer funds to the same account.");

        var transfer = new FundTransfer
        {
            Id = Guid.NewGuid(),
            FromAccountId = fromAccountId,
            ToAccountId = toAccountId,
            Amount = amount,
            TransferDate = DateTime.UtcNow, // Or a scheduled date if needed
            Status = FundTransferStatus.Pending,
            ReferenceNumber = referenceNumber ?? Guid.NewGuid().ToString("N").Substring(0,12), // Auto-generate if not provided
            CreatedAt = DateTime.UtcNow,
            LastModifiedAt = DateTime.UtcNow
        };

        transfer.AddDomainEvent(new FundTransferCreatedEvent(transfer.Id, transfer.FromAccountId, transfer.ToAccountId, transfer.Amount, transfer.Status));
        return transfer;
    }

    public void MarkAsProcessing()
    {
        if (Status != FundTransferStatus.Pending)
            throw new InvalidOperationException($"Cannot mark transfer as processing from status {Status}.");
        
        Status = FundTransferStatus.Processing;
        LastModifiedAt = DateTime.UtcNow;
        AddDomainEvent(new FundTransferStatusChangedEvent(Id, Status, FromAccountId, ToAccountId, Amount));
    }

    public void MarkAsCompleted()
    {
        if (Status != FundTransferStatus.Processing)
            throw new InvalidOperationException($"Cannot complete transfer from status {Status}.");

        Status = FundTransferStatus.Completed;
        TransferDate = DateTime.UtcNow; // Actual completion date
        LastModifiedAt = DateTime.UtcNow;
        AddDomainEvent(new FundTransferStatusChangedEvent(Id, Status, FromAccountId, ToAccountId, Amount));
    }

    public void MarkAsFailed(string reason)
    {
        if (Status == FundTransferStatus.Completed || Status == FundTransferStatus.Cancelled)
            throw new InvalidOperationException($"Cannot fail transfer from status {Status}.");

        Status = FundTransferStatus.Failed;
        FailureReason = reason;
        LastModifiedAt = DateTime.UtcNow;
        AddDomainEvent(new FundTransferStatusChangedEvent(Id, Status, FromAccountId, ToAccountId, Amount, reason));
    }

    public void Cancel(string reason = "Cancelled by user")
    {
        if (Status != FundTransferStatus.Pending)
             throw new InvalidOperationException($"Cannot cancel transfer from status {Status}.");

        Status = FundTransferStatus.Cancelled;
        FailureReason = reason;
        LastModifiedAt = DateTime.UtcNow;
        AddDomainEvent(new FundTransferStatusChangedEvent(Id, Status, FromAccountId, ToAccountId, Amount, reason));
    }
} 