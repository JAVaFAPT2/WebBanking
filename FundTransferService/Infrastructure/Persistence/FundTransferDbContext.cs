using FundTransferService.Domain.Entities;
using FundTransferService.Infrastructure.Persistence.Configuration;
using Microsoft.EntityFrameworkCore;
using System.Reflection;

namespace FundTransferService.Infrastructure.Persistence;

public class FundTransferDbContext : DbContext
{
    public DbSet<FundTransfer> FundTransfers { get; set; }

    public FundTransferDbContext(DbContextOptions<FundTransferDbContext> options)
        : base(options)
    {
    }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.ApplyConfigurationsFromAssembly(Assembly.GetExecutingAssembly());
        // Or more specifically:
        // modelBuilder.ApplyConfiguration(new FundTransferEntityTypeConfiguration());

        base.OnModelCreating(modelBuilder);
    }
} 