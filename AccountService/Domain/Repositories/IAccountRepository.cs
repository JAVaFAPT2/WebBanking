using Domain.Entity;

namespace Domain.Repositories;

public interface IAccountRepository
{
    Task<Account> GetByIdAsync(Guid accountId);
    Task<List<Account>> GetByUserIdAsync(Guid userId);
    Task<List<Account>> GetAllAsync();
    Task AddAsync(Account account);
    Task UpdateAsync(Account account);
    Task DeleteAsync(Guid accountId);
}