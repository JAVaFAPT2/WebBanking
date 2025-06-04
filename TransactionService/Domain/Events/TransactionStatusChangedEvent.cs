using System;
using MediatR;
using TransactionService.Domain.Enums;

namespace TransactionService.Domain.Events;

public record TransactionStatusChangedEvent(
    Guid TransactionId,
    TransactionStatus NewStatus,
    DateTime UpdatedAt,
    string? Reason // Optional, e.g., for failure reason
) : INotification; 