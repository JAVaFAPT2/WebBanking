using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using OrchestrationService.Domain.Interfaces;
using OrchestrationService.Domain.Models;

namespace OrchestrationService.Infrastructure.Repositories;

/// <summary>
/// In-memory implementation of the saga repository
/// </summary>
/// <typeparam name="T">Type of saga</typeparam>
public class InMemorySagaRepository<T> : ISagaRepository<T> where T : Saga
{
    private readonly ConcurrentDictionary<Guid, T> _sagas = new();
    private readonly ILogger<InMemorySagaRepository<T>> _logger;

    /// <summary>
    /// Constructor
    /// </summary>
    public InMemorySagaRepository(ILogger<InMemorySagaRepository<T>> logger)
    {
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    /// <inheritdoc />
    public Task<T?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
    {
        _logger.LogDebug("Getting saga by ID {Id}", id);
        
        _sagas.TryGetValue(id, out var saga);
        return Task.FromResult(saga);
    }

    /// <inheritdoc />
    public Task<T> SaveAsync(T saga, CancellationToken cancellationToken = default)
    {
        if (saga == null) throw new ArgumentNullException(nameof(saga));
        
        _logger.LogDebug("Saving saga with ID {Id}", saga.Id);
        
        if (!_sagas.TryAdd(saga.Id, saga))
        {
            throw new InvalidOperationException($"A saga with ID {saga.Id} already exists");
        }
        
        return Task.FromResult(saga);
    }

    /// <inheritdoc />
    public Task UpdateAsync(T saga, CancellationToken cancellationToken = default)
    {
        if (saga == null) throw new ArgumentNullException(nameof(saga));
        
        _logger.LogDebug("Updating saga with ID {Id}", saga.Id);
        
        if (!_sagas.TryGetValue(saga.Id, out _))
        {
            throw new InvalidOperationException($"No saga with ID {saga.Id} exists");
        }
        
        _sagas[saga.Id] = saga;
        
        return Task.CompletedTask;
    }

    /// <inheritdoc />
    public Task<IEnumerable<T>> GetByUserIdAsync(string userId, CancellationToken cancellationToken = default)
    {
        if (string.IsNullOrEmpty(userId)) throw new ArgumentException("User ID cannot be null or empty", nameof(userId));
        
        _logger.LogDebug("Getting sagas for user {UserId}", userId);
        
        // Since this is an in-memory implementation, we need to filter
        // Since we can't access UserId directly (it's in FundTransferSaga),
        // we'll use dynamic to access it
        var result = _sagas.Values
            .Where(s => 
            {
                // Handle potential reflection errors
                try
                {
                    dynamic dynamicSaga = s;
                    return dynamicSaga.UserId == userId;
                }
                catch
                {
                    return false;
                }
            })
            .ToList();
        
        return Task.FromResult<IEnumerable<T>>(result);
    }

    /// <inheritdoc />
    public Task<T?> GetByTransactionIdAsync(string transactionId, CancellationToken cancellationToken = default)
    {
        if (string.IsNullOrEmpty(transactionId)) throw new ArgumentException("Transaction ID cannot be null or empty", nameof(transactionId));
        
        _logger.LogDebug("Getting saga by transaction ID {TransactionId}", transactionId);
        
        // Since this is an in-memory implementation, we need to filter
        // Since we can't access TransactionId directly, we'll use dynamic
        var result = _sagas.Values
            .FirstOrDefault(s => 
            {
                // Handle potential reflection errors
                try
                {
                    dynamic dynamicSaga = s;
                    return dynamicSaga.TransactionId == transactionId;
                }
                catch
                {
                    return false;
                }
            });
        
        return Task.FromResult<T?>(result);
    }

    /// <inheritdoc />
    public Task<IEnumerable<T>> GetByStateAsync(SagaState state, CancellationToken cancellationToken = default)
    {
        _logger.LogDebug("Getting sagas in state {State}", state);
        
        var result = _sagas.Values
            .Where(s => s.State == state)
            .ToList();
        
        return Task.FromResult<IEnumerable<T>>(result);
    }
} 