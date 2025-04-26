using Domain.models;
using Microsoft.EntityFrameworkCore;


namespace Infrastructure.Persistence.DBContext
{
    public class ApplicationDbContext(DbContextOptions<ApplicationDbContext> options) : DbContext(options)
    {
        public DbSet<User> Users { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<User>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Name)
                      .IsRequired()
                      .HasMaxLength(100);
                entity.Property(e => e.Email)
                      .IsRequired()
                      .HasMaxLength(100);
                entity.Property(e => e.PasswordHash)
                      .IsRequired();
                entity.Property(e => e.CreatedAt)
                      .IsRequired();
            });
        }
    }
}
