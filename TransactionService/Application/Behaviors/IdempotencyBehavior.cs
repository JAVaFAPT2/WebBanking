using MediatR;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using System;
using System.Threading;
using System.Threading.Tasks;
using TransactionService.Domain.Entities; // For IdempotencyKey
using TransactionService.Infrastructure.Persistence; // For TransactionDbContext

namespace TransactionService.Application.Behaviors;

public class IdempotencyBehavior<TRequest, TResponse> : IPipelineBehavior<TRequest, TResponse>
    where TRequest : IRequest<TResponse>, IIdempotentCommand // Constrain to IIdempotentCommand
{
    private readonly TransactionDbContext _dbContext;
    private readonly ILogger<IdempotencyBehavior<TRequest, TResponse>> _logger;

    public IdempotencyBehavior(TransactionDbContext dbContext, ILogger<IdempotencyBehavior<TRequest, TResponse>> logger)
    {
        _dbContext = dbContext ?? throw new ArgumentNullException(nameof(dbContext));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    public async Task<TResponse> Handle(TRequest request, RequestHandlerDelegate<TResponse> next, CancellationToken cancellationToken)
    {
        var commandName = typeof(TRequest).Name;
        var requestId = request.RequestId;

        _logger.LogInformation("----- Checking for idempotency for command: {CommandName}; Request ID: {RequestId}", commandName, requestId);

        var idempotencyKey = await _dbContext.IdempotencyKeys
            .FirstOrDefaultAsync(ik => ik.RequestId == requestId && ik.CommandName == commandName, cancellationToken);

        if (idempotencyKey != null)
        {
            _logger.LogWarning("Duplicate request detected for command: {CommandName}; Request ID: {RequestId}. Request already processed at {CreatedAt}.", 
                commandName, requestId, idempotencyKey.CreatedAt);
            
            // For commands that return a specific result (like InitiateTransactionResponse),
            // you might want to fetch and return the original result. 
            // For now, we'll throw a custom exception. Consider a specific response type or a more sophisticated handling.
            // This behavior depends on the desired outcome for duplicate requests.
            // If TResponse is an object that can signify a duplicate (e.g. with a status code), that would be better.
            // For simplicity, let's assume for now that null can indicate a duplicate or an issue if TResponse is nullable.
            // A more robust solution would be a custom exception and an exception handling middleware.

            if (typeof(TResponse) == typeof(Application.CQRS.Commands.InitiateTransaction.InitiateTransactionResponse))
            {
                 // Attempt to find the original transaction if this is an InitiateTransactionCommand
                 // This is a simplified example; a more robust solution might store the serialized response with the IdempotencyKey
                 // or have a dedicated way to retrieve the original result.
                var originalTransaction = await _dbContext.Transactions
                                            .FirstOrDefaultAsync(t => t.CorrelationId == requestId, cancellationToken);
                if(originalTransaction != null)
                {
                    _logger.LogInformation("Returning original transaction details for duplicate request {RequestId}. TransactionId: {TransactionId}", requestId, originalTransaction.Id);
                    // This part is tricky because TResponse is generic.
                    // We are casting to dynamic. A better way would be to have a specific interface for responses of idempotent commands.
                    dynamic result = new Application.CQRS.Commands.InitiateTransaction.InitiateTransactionResponse(
                        originalTransaction.Id,
                        originalTransaction.Status
                    );
                    return (TResponse)result;
                }
                else
                {
                     _logger.LogWarning("Duplicate request {RequestId} found, but original transaction not found by CorrelationId.", requestId);
                     // Fallback or throw if original transaction not found
                }
            }
            
            // Default behavior for other idempotent commands if original result cannot be determined/returned
            // Or throw a specific exception like `DuplicateRequestException` to be handled by a middleware.
            // For now, returning default(TResponse) might not be ideal for all cases.
            _logger.LogWarning("Duplicate request {RequestId} for {CommandName}. Returning default response.", requestId, commandName);
            return default; // Or throw new DuplicateRequestException(requestId, commandName);
        }

        _logger.LogInformation("No duplicate detected for command: {CommandName}; Request ID: {RequestId}. Proceeding with handler.", commandName, requestId);

        // Add idempotency key before calling the next handler, it will be saved with the actual command changes
        var newIdempotencyKey = new IdempotencyKey(requestId, commandName);
        await _dbContext.IdempotencyKeys.AddAsync(newIdempotencyKey, cancellationToken);
        // Note: SaveChangesAsync is NOT called here. It will be called by the command handler or a unit of work behavior later.

        return await next();
    }
} 