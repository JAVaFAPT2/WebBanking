using System;
using MediatR;
using TransactionService.Domain.Enums;

namespace TransactionService.Application.CQRS.Commands.UpdateTransactionStatus;

public record UpdateTransactionStatusCommand(
    Guid TransactionId,
    TransactionStatus NewStatus,
    string? Reason = null,
    string? ReferenceNumber = null // Optional: to store external reference like payment gateway's ID
) : IRequest<bool>; 