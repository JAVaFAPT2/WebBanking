using Domain.Models;
using MediatR;
using Domain.ValueObjects;

namespace Domain.Events;

// Events published by CardService
public record CardCreatedEvent(
    Guid CardId,
    string CardNumber,
    Guid AccountId,
    CardType Type) : INotification;

public record CardIssuedEvent(
    Guid CardId,
    Guid AccountId,
    string CardNumber,
    string CardholderName,
    CardType CardType,
    DateTime ExpiryDate
) : INotification;

public record CardActivatedEvent(
    Guid CardId,
    Guid AccountId,
    DateTime ActivationDate
) : INotification;

public record CardBlockedEvent(
    Guid CardId,
    Guid AccountId,
    string Reason,
    DateTime BlockedDate
) : INotification;

public record CardUnblockedEvent(
    Guid CardId,
    string CardNumber) : INotification;

public record CardLimitUpdatedEvent(
    Guid CardId,
    string CardNumber,
    Money OldLimit,
    Money NewLimit) : INotification;

// Events consumed from other services
public record AccountCreatedEvent(
    Guid AccountId,
    string AccountNumber,
    Guid UserId,
    string AccountType) : INotification;

public record AccountClosedEvent(
    Guid AccountId,
    string AccountNumber) : INotification;

public record AccountBlockedEvent(
    Guid AccountId,
    string AccountNumber) : INotification;

public record TransactionAuthorizedEvent(
    Guid CardId,
    Guid AccountId,
    string MerchantName,
    Money Amount,
    Money AvailableBalance,
    DateTime TransactionDate
) : INotification;

public record TransactionDeclinedEvent(
    Guid CardId,
    Guid AccountId,
    string MerchantName,
    Money Amount,
    string DeclineReason,
    DateTime TransactionDate
) : INotification; 