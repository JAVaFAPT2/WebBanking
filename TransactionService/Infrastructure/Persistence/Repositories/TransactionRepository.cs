using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using TransactionService.Domain.Entities;
using TransactionService.Domain.Interfaces;

namespace TransactionService.Infrastructure.Persistence.Repositories;

public class TransactionRepository : ITransactionRepository
{
    private readonly TransactionDbContext _context;
    private readonly ILogger<TransactionRepository> _logger;

    public TransactionRepository(TransactionDbContext context, ILogger<TransactionRepository> logger)
    {
        _context = context;
        _logger = logger;
    }

    public async Task<Transaction?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
    {
        _logger.LogDebug("Fetching transaction by ID: {Id}", id);
        return await _context.Transactions.FirstOrDefaultAsync(t => t.Id == id, cancellationToken);
    }

    public async Task<Transaction?> GetByCorrelationIdAsync(Guid correlationId, CancellationToken cancellationToken = default)
    {
        _logger.LogDebug("Fetching transaction by CorrelationID: {CorrelationId}", correlationId);
        return await _context.Transactions.FirstOrDefaultAsync(t => t.CorrelationId == correlationId, cancellationToken);
    }

    public async Task<IEnumerable<Transaction>> GetByAccountIdAsync(Guid accountId, DateTime? fromDate = null, DateTime? toDate = null, CancellationToken cancellationToken = default)
    {
        _logger.LogDebug("Fetching transactions for AccountID: {AccountId}, From: {FromDate}, To: {ToDate}", accountId, fromDate, toDate);
        var query = _context.Transactions
            .Where(t => t.AccountFromId == accountId || t.AccountToId == accountId);

        if (fromDate.HasValue)
        {
            query = query.Where(t => t.InitiatedAt >= fromDate.Value);
        }

        if (toDate.HasValue)
        {
            query = query.Where(t => t.InitiatedAt <= toDate.Value);
        }

        return await query.OrderByDescending(t => t.InitiatedAt).ToListAsync(cancellationToken);
    }

    public async Task<Transaction> AddAsync(Transaction transaction, CancellationToken cancellationToken = default)
    {
        _logger.LogDebug("Adding new transaction: {@Transaction}", transaction);
        await _context.Transactions.AddAsync(transaction, cancellationToken);
        // SaveChangesAsync will be called by a UnitOfWork or a service layer managing the transaction scope.
        // Or, if this repository is the sole writer, it could call SaveChangesAsync here.
        // For domain event dispatch, SaveChangesAsync in DbContext is key.
        await _context.SaveChangesAsync(cancellationToken); // Assuming repository handles UoW for now or SaveChanges is overridden for events
        return transaction;
    }

    public async Task UpdateAsync(Transaction transaction, CancellationToken cancellationToken = default)
    {
        _logger.LogDebug("Updating transaction: {@Transaction}", transaction);
        _context.Transactions.Update(transaction);
        // Similar to AddAsync, SaveChangesAsync handles persistence and event dispatch.
        await _context.SaveChangesAsync(cancellationToken); // Assuming repository handles UoW for now or SaveChanges is overridden for events
    }
} 