using FundTransferService.Domain.Entities;
using FundTransferService.Domain.ValueObjects;
using MediatR;

namespace FundTransferService.Domain.Events;

public record FundTransferCreatedEvent(
    Guid TransferId,
    Guid FromAccountId,
    Guid ToAccountId,
    Money Amount,
    FundTransferStatus Status
) : INotification;

public record FundTransferStatusChangedEvent(
    Guid TransferId,
    FundTransferStatus NewStatus,
    Guid FromAccountId,
    Guid ToAccountId,
    Money Amount,
    string? Reason = null
) : INotification; 