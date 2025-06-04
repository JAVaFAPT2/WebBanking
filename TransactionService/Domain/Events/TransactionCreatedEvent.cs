using System;
using MediatR;
using TransactionService.Domain.Enums;
using TransactionService.Domain.ValueObjects;

namespace TransactionService.Domain.Events;

public record TransactionCreatedEvent(
    Guid TransactionId,
    TransactionType Type,
    Money Amount,
    DateTime InitiatedAt
) : INotification; 