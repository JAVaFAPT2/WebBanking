using MediatR;
using Microsoft.EntityFrameworkCore;
using TransactionService.Domain.Common; // Required for AggregateRoot
using TransactionService.Domain.Entities;
using TransactionService.Domain.ValueObjects;
using System.Linq; // Required for LINQ queries like .Any()
using System.Reflection;
using System.Threading; // Required for CancellationToken
using System.Threading.Tasks; // Required for Task

namespace TransactionService.Infrastructure.Persistence;

public class TransactionDbContext : DbContext
{
    private readonly IMediator _mediator;
    public DbSet<Transaction> Transactions { get; set; }
    public DbSet<IdempotencyKey> IdempotencyKeys { get; set; } // Added DbSet for IdempotencyKey

    // Modified constructor to accept IMediator
    public TransactionDbContext(DbContextOptions<TransactionDbContext> options, IMediator mediator) : base(options)
    {
        _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
    }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);
        modelBuilder.ApplyConfigurationsFromAssembly(Assembly.GetExecutingAssembly());

        // Configure Money value object as an owned entity type for Transaction.Amount
        modelBuilder.Entity<Transaction>(b =>
        {
            b.OwnsOne(t => t.Amount, a =>
            {
                a.Property(m => m.Amount).HasColumnName("Amount").HasColumnType("decimal(18,2)");
                a.Property(m => m.Currency).HasColumnName("Currency").HasMaxLength(3);
            });
        });
        
        // Add more configurations if needed, e.g., for indexes
        modelBuilder.Entity<Transaction>()
            .HasIndex(t => t.CorrelationId);
        modelBuilder.Entity<Transaction>()
            .HasIndex(t => t.AccountFromId)
            .IsUnique(false); // Account can have many transactions
        modelBuilder.Entity<Transaction>()
            .HasIndex(t => t.AccountToId)
            .IsUnique(false); // Account can have many transactions
        modelBuilder.Entity<Transaction>()
            .HasIndex(t => t.InitiatedAt);

        // Configure IdempotencyKey entity
        modelBuilder.Entity<IdempotencyKey>(b =>
        {
            b.HasKey(ik => ik.RequestId);
            b.Property(ik => ik.CommandName).IsRequired().HasMaxLength(255); // Example length
            b.Property(ik => ik.CreatedAt).IsRequired();
        });
    }

    public override async Task<int> SaveChangesAsync(CancellationToken cancellationToken = default)
    {
        // Dispatch Domain Events before saving changes
        await DispatchDomainEventsAsync();
        return await base.SaveChangesAsync(cancellationToken);
    }

    private async Task DispatchDomainEventsAsync()
    {
        var domainEventEntities = ChangeTracker.Entries<AggregateRoot>()
            .Select(entry => entry.Entity)
            .Where(entity => entity.DomainEvents.Any())
            .ToList();

        foreach (var entity in domainEventEntities)
        {
            var events = entity.DomainEvents.ToList();
            entity.ClearDomainEvents(); // Clear events before dispatching to prevent re-dispatching if handler causes SaveChanges
            
            foreach (var domainEvent in events)
            {
                await _mediator.Publish(domainEvent); 
            }
        }
    }
} 