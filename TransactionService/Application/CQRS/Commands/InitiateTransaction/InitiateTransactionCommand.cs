using System;
using MediatR;
using TransactionService.Application.Behaviors; // For IIdempotentCommand
using TransactionService.Domain.Enums;
using TransactionService.Domain.ValueObjects;

namespace TransactionService.Application.CQRS.Commands.InitiateTransaction;

// DTO for the command response - This name was used in IdempotencyBehavior, ensure consistency
// Let's rename this to InitiateTransactionCommandResult to match previous thoughts if that's intended, 
// or update IdempotencyBehavior to use InitiateTransactionResponse.
// For now, sticking with InitiateTransactionResponse as per this file.
public record InitiateTransactionResponse(Guid TransactionId, TransactionStatus InitialStatus);

public record InitiateTransactionCommand : IRequest<InitiateTransactionResponse>, IIdempotentCommand
{
    public Guid? AccountFromId { get; } 
    public Guid? AccountToId { get; }
    public TransactionType Type { get; }
    public Money Amount { get; }
    public string? Description { get; }
    public string? InitiatedBy { get; } // User ID or system component that initiated the transaction
    public Guid CorrelationId { get; } // Made non-nullable for clarity as RequestId

    // IIdempotentCommand implementation
    public Guid RequestId => CorrelationId; // Use CorrelationId as the RequestId for idempotency

    public InitiateTransactionCommand(
        Guid? accountFromId,
        Guid? accountToId,
        TransactionType type,
        Money amount,
        string? description,
        string? initiatedBy,
        Guid? correlationId = null // Client can provide, or we generate
    )
    {
        AccountFromId = accountFromId;
        AccountToId = accountToId;
        Type = type;
        Amount = amount;
        Description = description;
        InitiatedBy = initiatedBy;
        CorrelationId = correlationId ?? Guid.NewGuid(); // Ensure CorrelationId (and thus RequestId) always has a value
    }
} 