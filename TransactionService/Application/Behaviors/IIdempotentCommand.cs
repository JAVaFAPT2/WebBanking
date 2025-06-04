using System;

namespace TransactionService.Application.Behaviors;
 
public interface IIdempotentCommand
{
    Guid RequestId { get; } // The idempotency key for the command
} 