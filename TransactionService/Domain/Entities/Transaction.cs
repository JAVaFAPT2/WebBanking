using System;
using TransactionService.Domain.Common;
using TransactionService.Domain.Enums;
using TransactionService.Domain.Events;
using TransactionService.Domain.ValueObjects;

namespace TransactionService.Domain.Entities;

public class Transaction : AggregateRoot
{
    public Guid Id { get; private set; }
    public Guid CorrelationId { get; private set; } // For tracking related transactions (e.g., saga)
    public Guid? AccountFromId { get; private set; }
    public Guid? AccountToId { get; private set; }
    public TransactionType Type { get; private set; }
    public TransactionStatus Status { get; private set; }
    public Money Amount { get; private set; }
    public DateTime InitiatedAt { get; private set; }
    public DateTime LastUpdatedAt { get; private set; }
    public string? Description { get; private set; }
    public string? FailureReason { get; private set; }
    public string? ReferenceNumber { get; private set; } // e.g., payment gateway reference
    public string? InitiatedBy { get; private set; } // User ID or system component

    private Transaction() { } // For EF Core

    public static Transaction Create(
        Guid? accountFromId,
        Guid? accountToId,
        TransactionType type,
        Money amount,
        string? description,
        string? initiatedBy,
        Guid? correlationId = null)
    {
        if (amount.Amount <= 0)
            throw new ArgumentException("Transaction amount must be positive.", nameof(amount));

        var transaction = new Transaction
        {
            Id = Guid.NewGuid(),
            CorrelationId = correlationId ?? Guid.NewGuid(),
            AccountFromId = accountFromId,
            AccountToId = accountToId,
            Type = type,
            Status = TransactionStatus.Pending,
            Amount = amount,
            InitiatedAt = DateTime.UtcNow,
            LastUpdatedAt = DateTime.UtcNow,
            Description = description,
            InitiatedBy = initiatedBy
        };

        transaction.AddDomainEvent(new TransactionCreatedEvent(transaction.Id, transaction.Type, transaction.Amount, transaction.InitiatedAt));
        return transaction;
    }

    public void SetStatus(TransactionStatus newStatus, string? reason = null)
    {
        if (Status == newStatus) return;

        Status = newStatus;
        LastUpdatedAt = DateTime.UtcNow;
        if (newStatus == TransactionStatus.Failed)
        {
            FailureReason = reason;
        }
        AddDomainEvent(new TransactionStatusChangedEvent(Id, newStatus, LastUpdatedAt, reason));
    }

    public void SetReferenceNumber(string reference)
    {
        ReferenceNumber = reference;
        LastUpdatedAt = DateTime.UtcNow;
    }
} 