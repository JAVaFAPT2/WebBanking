using Domain.models;
using Domain.Models;
using Microsoft.EntityFrameworkCore;

namespace Infrastructure.Persistence.DBContext
{
    public class ApplicationDbContext : DbContext
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options) : base(options) { }

        public DbSet<User> Users { get; set; }

        public DbSet<PasswordResetToken> PasswordResetTokens { get; set; }


        public DbSet<KycDocument> KycDocuments { get; set; }
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<User>(entity =>
            {
                entity.ToTable("Users");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnType("uniqueidentifier");
                entity.Property(e => e.Username).IsRequired().HasMaxLength(50).HasColumnType("nvarchar(50)");
                entity.Property(e => e.FirstName).IsRequired().HasMaxLength(50).HasColumnType("nvarchar(50)");
                entity.Property(e => e.LastName).IsRequired().HasMaxLength(50).HasColumnType("nvarchar(50)");
                entity.Property(e => e.Email).IsRequired().HasMaxLength(100).HasColumnType("nvarchar(100)");
                entity.Property(e => e.PhoneNumber).IsRequired().HasMaxLength(20).HasColumnType("nvarchar(20)");
                entity.Property(e => e.PasswordHash).IsRequired().HasMaxLength(128).HasColumnType("nvarchar(128)");
                entity.Property(e => e.DateOfBirth).IsRequired().HasColumnType("date");
                entity.OwnsOne(e => e.Address, address =>
                {
                    address.Property(a => a.Street).IsRequired().HasMaxLength(100).HasColumnType("nvarchar(100)");
                    address.Property(a => a.City).IsRequired().HasMaxLength(50).HasColumnType("nvarchar(50)");
                    address.Property(a => a.State).IsRequired().HasMaxLength(50).HasColumnType("nvarchar(50)");
                    address.Property(a => a.ZipCode).IsRequired().HasMaxLength(20).HasColumnType("nvarchar(20)");
                    address.Property(a => a.Country).IsRequired().HasMaxLength(50).HasColumnType("nvarchar(50)");
                });
                entity.HasMany(u => u.PasswordResetTokens)
                    .WithOne(t => t.User)
                    .HasForeignKey(t => t.UserId);
                entity.Property(e => e.KycStatus).IsRequired().HasColumnType("int");
                entity.Property(e => e.CreatedAt).IsRequired().HasColumnType("datetime2");
                entity.Property(e => e.UpdatedAt).HasColumnType("datetime2");
                entity.Property(e => e.IsActive).IsRequired().HasDefaultValue(true);
                entity.HasIndex(e => e.Email).IsUnique();
            });
            modelBuilder.Entity<PasswordResetToken>(entity =>
            {
                entity.ToTable("PasswordResetTokens");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnType("uniqueidentifier");
                entity.Property(e => e.Token).IsRequired().HasMaxLength(256);
                entity.Property(e => e.Expiry).IsRequired();
                entity.Property(e => e.IsUsed).IsRequired();
                entity.HasOne(e => e.User)
                    .WithMany() // or .WithMany(u => u.PasswordResetTokens) if you add the collection to User
                    .HasForeignKey(e => e.UserId)
                    .OnDelete(DeleteBehavior.Cascade);
            });
        }
    }
}
