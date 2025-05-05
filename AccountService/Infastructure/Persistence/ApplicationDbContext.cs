using Domain.Entity;
using Microsoft.EntityFrameworkCore;

namespace Infrastructure.Persistence
{
    public class ApplicationDbContext(DbContextOptions<ApplicationDbContext> options) : DbContext(options)
    {
        public DbSet<Account> Accounts { get; set; } 
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Account>(entity =>
            {
                entity.ToTable("Accounts");
                entity.HasKey(e => e.AccountId);

                entity.Property(e => e.AccountId)
                    .HasColumnType("uniqueidentifier");

                entity.Property(e => e.UserId)
                    .IsRequired()
                    .HasColumnType("uniqueidentifier");

                entity.Property(e => e.AccountNumber)
                    .IsRequired()
                    .HasMaxLength(50)
                    .HasColumnType("nvarchar(50)");

                entity.Property(e => e.Balance)
                    .IsRequired()
                    .HasColumnType("decimal(18,2)");

                entity.Property(e => e.AccountType)
                    .IsRequired()
                    .HasMaxLength(50)
                    .HasColumnType("nvarchar(50)");

                entity.Property(e => e.CreatedAt)
                    .IsRequired()
                    .HasColumnType("datetime2");

                entity.HasIndex(e => e.AccountNumber)
                    .IsUnique();
            });
        }
    }
}
