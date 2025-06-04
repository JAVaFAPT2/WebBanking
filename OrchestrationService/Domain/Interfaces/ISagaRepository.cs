using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using OrchestrationService.Domain.Models;

namespace OrchestrationService.Domain.Interfaces;

/// <summary>
/// Repository interface for persisting and retrieving sagas
/// </summary>
/// <typeparam name="T">Type of saga</typeparam>
public interface ISagaRepository<T> where T : Saga
{
    /// <summary>
    /// Get a saga by its ID
    /// </summary>
    Task<T?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);
    
    /// <summary>
    /// Save a new saga
    /// </summary>
    Task<T> SaveAsync(T saga, CancellationToken cancellationToken = default);
    
    /// <summary>
    /// Update an existing saga
    /// </summary>
    Task UpdateAsync(T saga, CancellationToken cancellationToken = default);
    
    /// <summary>
    /// Get all sagas for a user
    /// </summary>
    Task<IEnumerable<T>> GetByUserIdAsync(string userId, CancellationToken cancellationToken = default);
    
    /// <summary>
    /// Get a saga by its transaction ID
    /// </summary>
    Task<T?> GetByTransactionIdAsync(string transactionId, CancellationToken cancellationToken = default);
    
    /// <summary>
    /// Get all sagas in a specific state
    /// </summary>
    Task<IEnumerable<T>> GetByStateAsync(SagaState state, CancellationToken cancellationToken = default);
} 