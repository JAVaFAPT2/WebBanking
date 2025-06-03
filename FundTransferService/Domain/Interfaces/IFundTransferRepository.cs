using FundTransferService.Domain.Entities;

namespace FundTransferService.Domain.Interfaces;

public interface IFundTransferRepository
{
    Task<FundTransfer?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);
    Task<IEnumerable<FundTransfer>> GetByAccountIdAsync(Guid accountId, CancellationToken cancellationToken = default);
    Task<IEnumerable<FundTransfer>> GetByStatusAsync(FundTransferStatus status, CancellationToken cancellationToken = default);
    Task<FundTransfer> AddAsync(FundTransfer fundTransfer, CancellationToken cancellationToken = default);
    Task UpdateAsync(FundTransfer fundTransfer, CancellationToken cancellationToken = default);
    Task<bool> ExistsAsync(Guid id, CancellationToken cancellationToken = default);
} 