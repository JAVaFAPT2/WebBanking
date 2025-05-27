using Domain.Models;
using Domain.ValueObjects;
using Microsoft.EntityFrameworkCore;

namespace Infrastructure.Persistence;

public class AccountDbContext : DbContext
{
    public AccountDbContext(DbContextOptions<AccountDbContext> options) : base(options)
    {
    }

    public DbSet<Account> Accounts { get; set; } = null!;

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.ApplyConfigurationsFromAssembly(typeof(AccountDbContext).Assembly);
        
        modelBuilder.Entity<Account>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.AccountNumber).HasConversion(
                v => v.Value,
                v => new AccountNumber(v));
            entity.OwnsOne(e => e.Balance, b =>
            {
                b.Property(m => m.Amount).HasColumnName("BalanceAmount");
                b.Property(m => m.Currency).HasColumnName("BalanceCurrency").HasMaxLength(3);
            });
            entity.Property(e => e.Type).HasConversion<string>();
            entity.Property(e => e.Status).HasConversion<string>();
            entity.Property(e => e.Currency).IsRequired().HasMaxLength(3);
            entity.Property(e => e.CreatedAt).IsRequired();
            entity.Property(e => e.LastModifiedAt);
        });
    }
} 