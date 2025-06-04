using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;
using Domain.Common;

namespace Domain.ValueObjects;

[Owned]
public class Money : ValueObject
{
    [Required]
    [Column(TypeName = "decimal(18,2)")]
    public decimal Amount { get; private set; }

    [Required]
    [StringLength(3)]
    [Column(TypeName = "nvarchar(3)")]
    public string Currency { get; private set; }

    private Money() { } // For EF Core

    public Money(decimal amount, string currency)
    {
        if (amount < 0)
            throw new DomainException("Amount cannot be negative");

        if (string.IsNullOrWhiteSpace(currency))
            throw new DomainException("Currency cannot be empty");

        if (currency.Length != 3)
            throw new DomainException("Currency code must be exactly 3 characters");

        Amount = amount;
        Currency = currency.ToUpperInvariant();
    }

    public static Money Zero(string currency) => new(0, currency);

    public Money Add(Money other)
    {
        if (Currency != other.Currency)
            throw new DomainException("Cannot add money with different currencies");

        return new Money(Amount + other.Amount, Currency);
    }

    public Money Subtract(Money other)
    {
        if (Currency != other.Currency)
            throw new DomainException("Cannot subtract money with different currencies");

        if (Amount < other.Amount)
            throw new DomainException("Insufficient funds");

        return new Money(Amount - other.Amount, Currency);
    }

    protected override IEnumerable<object> GetEqualityComponents()
    {
        yield return Amount;
        yield return Currency;
    }

    public override string ToString() => $"{Amount} {Currency}";
} 