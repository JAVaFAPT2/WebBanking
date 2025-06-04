using System;
using MediatR;
using TransactionService.Domain.Enums;
using TransactionService.Domain.ValueObjects;

namespace TransactionService.Application.CQRS.Queries.GetTransactionById;

// DTO to represent the transaction details returned by the query
public record TransactionDetailsDto(
    Guid Id,
    Guid CorrelationId,
    Guid? AccountFromId,
    Guid? AccountToId,
    TransactionType Type,
    TransactionStatus Status,
    Money Amount,
    DateTime InitiatedAt,
    DateTime LastUpdatedAt,
    string? Description,
    string? FailureReason,
    string? ReferenceNumber,
    string? InitiatedBy
);

public record GetTransactionByIdQuery(Guid TransactionId) : IRequest<TransactionDetailsDto?>; 