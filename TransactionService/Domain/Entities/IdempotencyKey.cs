using System;

namespace TransactionService.Domain.Entities;

public class IdempotencyKey
{
    public Guid RequestId { get; private set; } // The unique key for the request
    public string CommandName { get; private set; } // Name of the command being processed
    public DateTime CreatedAt { get; private set; } // When the request was first processed
    // public Guid? TransactionId { get; private set; } // Optional: Store the ID of the created transaction if applicable

    private IdempotencyKey() { } // For EF Core

    public IdempotencyKey(Guid requestId, string commandName)
    {
        RequestId = requestId;
        CommandName = commandName;
        CreatedAt = DateTime.UtcNow;
    }

    // public void SetTransactionId(Guid transactionId)
    // {
    //     TransactionId = transactionId;
    // }
} 