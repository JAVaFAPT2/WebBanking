using Domain.Interface;
using Domain.Models;
using Domain.ValueObjects;
using Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;

namespace Infrastructure.Repositories;

public class AccountRepository : IAccountRepository
{
    private readonly AccountDbContext _context;

    public AccountRepository(AccountDbContext context)
    {
        _context = context;
    }

    public async Task<Account?> GetByIdAsync(Guid id)
    {
        return await _context.Accounts.FindAsync(id);
    }

    public async Task<Account?> GetByAccountNumberAsync(AccountNumber accountNumber)
    {
        return await _context.Accounts
            .FirstOrDefaultAsync(a => a.AccountNumber == accountNumber);
    }

    public async Task<IEnumerable<Account>> GetByUserIdAsync(Guid userId)
    {
        return await _context.Accounts
            .Where(a => a.UserId == userId)
            .ToListAsync();
    }

    public async Task<Account> AddAsync(Account account)
    {
        await _dbContext.Accounts.AddAsync(account);
        await _dbContext.SaveChangesAsync();
        return account;
    }

    public Task UpdateAsync(Account account)
    {
        _context.Accounts.Update(account);
        return Task.CompletedTask;
    }

    public Task DeleteAsync(Account account)
    {
        _context.Accounts.Remove(account);
        return Task.CompletedTask;
    }

    public async Task<bool> ExistsAsync(Guid id)
    {
        return await _context.Accounts.AnyAsync(a => a.Id == id);
    }

    public async Task<int> SaveChangesAsync()
    {
        return await _context.SaveChangesAsync();
    }
} 