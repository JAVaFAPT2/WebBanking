using Domain.Common;
using Domain.Models;
using Domain.ValueObjects;
using MediatR;

namespace Domain.Events;

public record AccountCreatedEvent(
    Guid AccountId,
    AccountNumber AccountNumber,
    Guid UserId,
    AccountType Type,
    string Currency) : INotification;

public record AccountClosedEvent(
    Guid AccountId,
    AccountNumber AccountNumber) : INotification;

public record AccountBlockedEvent(
    Guid AccountId,
    AccountNumber AccountNumber) : INotification;

public record AccountUnblockedEvent(
    Guid AccountId,
    AccountNumber AccountNumber) : INotification;

public record BalanceUpdatedEvent(
    Guid AccountId,
    AccountNumber AccountNumber,
    Money OldBalance,
    Money NewBalance,
    TransactionType TransactionType) : INotification; 