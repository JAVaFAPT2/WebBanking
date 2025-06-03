using FundTransferService.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace FundTransferService.Infrastructure.Persistence.Configuration;

public class FundTransferEntityTypeConfiguration : IEntityTypeConfiguration<FundTransfer>
{
    public void Configure(EntityTypeBuilder<FundTransfer> builder)
    {
        builder.ToTable("FundTransfers");

        builder.HasKey(ft => ft.Id);

        builder.Property(ft => ft.FromAccountId)
            .IsRequired();

        builder.Property(ft => ft.ToAccountId)
            .IsRequired();

        builder.OwnsOne(ft => ft.Amount, amountBuilder =>
        {
            amountBuilder.Property(m => m.Amount)
                .HasColumnName("Amount")
                .HasColumnType("decimal(18,2)")
                .IsRequired();
            amountBuilder.Property(m => m.Currency)
                .HasColumnName("Currency")
                .HasMaxLength(3)
                .IsRequired();
        });

        builder.Property(ft => ft.TransferDate)
            .IsRequired();

        builder.Property(ft => ft.Status)
            .IsRequired()
            .HasConversion<string>() // Store enum as string
            .HasMaxLength(20);

        builder.Property(ft => ft.ReferenceNumber)
            .HasMaxLength(50);
        
        builder.Property(ft => ft.FailureReason)
            .HasMaxLength(255);

        builder.Property(ft => ft.CreatedAt)
            .IsRequired();

        builder.Property(ft => ft.LastModifiedAt)
            .IsRequired();

        // Ignoring DomainEvents from persistence
        builder.Ignore(ft => ft.DomainEvents);
    }
} 