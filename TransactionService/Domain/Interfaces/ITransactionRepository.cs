using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using TransactionService.Domain.Entities;

namespace TransactionService.Domain.Interfaces;

public interface ITransactionRepository
{
    Task<Transaction?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);
    Task<Transaction?> GetByCorrelationIdAsync(Guid correlationId, CancellationToken cancellationToken = default);
    Task<IEnumerable<Transaction>> GetByAccountIdAsync(Guid accountId, DateTime? fromDate = null, DateTime? toDate = null, CancellationToken cancellationToken = default);
    Task<Transaction> AddAsync(Transaction transaction, CancellationToken cancellationToken = default);
    Task UpdateAsync(Transaction transaction, CancellationToken cancellationToken = default);
    // Consider adding a method for querying transactions with more filters if needed
    // Task<IEnumerable<Transaction>> FindAsync(TransactionQueryCriteria criteria, CancellationToken cancellationToken = default);
} 