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

    public async Task<Account?> GetByAccountNumberAsync(string accountNumber)
    {
        return await _context.Accounts
            .FirstOrDefaultAsync(a => a.AccountNumber.Value == accountNumber);
    }

    public async Task<IEnumerable<Account>> GetByUserIdAsync(Guid userId)
    {
        return await _context.Accounts
            .Where(a => a.UserId == userId)
            .ToListAsync();
    }

    public async Task<Account> AddAsync(Account account)
    {
        await _context.Accounts.AddAsync(account);
        await _context.SaveChangesAsync();
        return account;
    }

    public async Task UpdateAsync(Account account)
    {
        _context.Accounts.Update(account);
        await _context.SaveChangesAsync();
    }

    public async Task DeleteAsync(Guid id)
    {
        var account = await GetByIdAsync(id);
        if (account != null)
        {
            _context.Accounts.Remove(account);
            await _context.SaveChangesAsync();
        }
    }

    public async Task<bool> ExistsAsync(string accountNumber)
    {
        return await _context.Accounts.AnyAsync(a => a.AccountNumber.Value == accountNumber);
    }

    public async Task SaveChangesAsync()
    {
        await _context.SaveChangesAsync();
    }
} 