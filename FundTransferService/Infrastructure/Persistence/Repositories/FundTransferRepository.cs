using FundTransferService.Domain.Entities;
using FundTransferService.Domain.Interfaces;
using FundTransferService.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace FundTransferService.Infrastructure.Repositories;

public class FundTransferRepository : IFundTransferRepository
{
    private readonly FundTransferDbContext _context;
    private readonly ILogger<FundTransferRepository> _logger;

    public FundTransferRepository(FundTransferDbContext context, ILogger<FundTransferRepository> logger)
    {
        _context = context ?? throw new ArgumentNullException(nameof(context));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    public async Task<FundTransfer?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Fetching fund transfer by ID {TransferId}", id);
        return await _context.FundTransfers.FirstOrDefaultAsync(ft => ft.Id == id, cancellationToken);
    }

    public async Task<IEnumerable<FundTransfer>> GetByAccountIdAsync(Guid accountId, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Fetching fund transfers for account ID {AccountId}", accountId);
        return await _context.FundTransfers
            .Where(ft => ft.FromAccountId == accountId || ft.ToAccountId == accountId)
            .ToListAsync(cancellationToken);
    }

    public async Task<IEnumerable<FundTransfer>> GetByStatusAsync(FundTransferStatus status, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Fetching fund transfers by status {Status}", status);
        return await _context.FundTransfers
            .Where(ft => ft.Status == status)
            .ToListAsync(cancellationToken);
    }

    public async Task<FundTransfer> AddAsync(FundTransfer fundTransfer, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Adding new fund transfer for FromAccount {FromAccountId} to ToAccount {ToAccountId}", fundTransfer.FromAccountId, fundTransfer.ToAccountId);
        _context.FundTransfers.Add(fundTransfer);
        await _context.SaveChangesAsync(cancellationToken);
        return fundTransfer;
    }

    public async Task UpdateAsync(FundTransfer fundTransfer, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Updating fund transfer ID {TransferId}", fundTransfer.Id);
        _context.Entry(fundTransfer).State = EntityState.Modified;
        await _context.SaveChangesAsync(cancellationToken);
    }

    public async Task<bool> ExistsAsync(Guid id, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Checking existence of fund transfer ID {TransferId}", id);
        return await _context.FundTransfers.AnyAsync(ft => ft.Id == id, cancellationToken);
    }
} 