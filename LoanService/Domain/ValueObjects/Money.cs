namespace Domain.ValueObjects;

public record Money
{
    public decimal Amount { get; }
    public string Currency { get; }

    public Money(decimal amount, string currency)
    {
        if (amount < 0)
            throw new ArgumentException("Amount cannot be negative", nameof(amount));
        
        if (string.IsNullOrWhiteSpace(currency))
            throw new ArgumentException("Currency cannot be empty", nameof(currency));

        Amount = amount;
        Currency = currency.ToUpperInvariant();
    }

    public static Money Zero(string currency) => new(0, currency);

    public Money Add(Money other)
    {
        if (other.Currency != Currency)
            throw new InvalidOperationException($"Cannot add money with different currencies: {Currency} and {other.Currency}");

        return new Money(Amount + other.Amount, Currency);
    }

    public Money Subtract(Money other)
    {
        if (other.Currency != Currency)
            throw new InvalidOperationException($"Cannot subtract money with different currencies: {Currency} and {other.Currency}");

        return new Money(Amount - other.Amount, Currency);
    }

    public bool IsGreaterThan(Money other)
    {
        if (other.Currency != Currency)
            throw new InvalidOperationException($"Cannot compare money with different currencies: {Currency} and {other.Currency}");

        return Amount > other.Amount;
    }

    public bool IsLessThan(Money other)
    {
        if (other.Currency != Currency)
            throw new InvalidOperationException($"Cannot compare money with different currencies: {Currency} and {other.Currency}");

        return Amount < other.Amount;
    }
} 