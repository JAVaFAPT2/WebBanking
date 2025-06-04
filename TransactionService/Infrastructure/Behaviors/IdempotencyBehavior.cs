using System;
using System.Threading;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Logging;
using TransactionService.Application.Behaviors; // For IIdempotentCommand
using TransactionService.Domain.Entities;
using TransactionService.Infrastructure.Persistence;

namespace TransactionService.Infrastructure.Behaviors;

public class IdempotencyBehavior<TRequest, TResponse> : IPipelineBehavior<TRequest, TResponse>
    where TRequest : IRequest<TResponse>
{
    private readonly TransactionDbContext _dbContext;
    private readonly ILogger<IdempotencyBehavior<TRequest, TResponse>> _logger;

    public IdempotencyBehavior(TransactionDbContext dbContext, ILogger<IdempotencyBehavior<TRequest, TResponse>> logger)
    {
        _dbContext = dbContext;
        _logger = logger;
    }

    public async Task<TResponse> Handle(TRequest request, RequestHandlerDelegate<TResponse> next, CancellationToken cancellationToken)
    {
        if (request is IIdempotentCommand idempotentCommand)
        {
            var existingKey = await _dbContext.IdempotencyKeys.FindAsync(new object[] { idempotentCommand.RequestId }, cancellationToken);
            if (existingKey != null)
            {
                _logger.LogInformation("Duplicate request detected: {RequestId}", idempotentCommand.RequestId);
                throw new InvalidOperationException($"Duplicate request: {idempotentCommand.RequestId}");
            }
            _dbContext.IdempotencyKeys.Add(new IdempotencyKey(idempotentCommand.RequestId, typeof(TRequest).Name));
            await _dbContext.SaveChangesAsync(cancellationToken);
        }
        return await next();
    }
} 