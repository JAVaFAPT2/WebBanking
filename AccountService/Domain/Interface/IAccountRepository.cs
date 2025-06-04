using Domain.Models;

namespace Domain.Interface;

public interface IAccountRepository
{
    Task<Account?> GetByIdAsync(Guid id);
    Task<Account?> GetByAccountNumberAsync(string accountNumber);
    Task<IEnumerable<Account>> GetByUserIdAsync(Guid userId);
    Task<Account> AddAsync(Account account);
    Task UpdateAsync(Account account);
    Task DeleteAsync(Guid id);
    Task<bool> ExistsAsync(string accountNumber);
    Task SaveChangesAsync();
} 