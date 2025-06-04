using Domain.Common;

namespace Domain.ValueObjects;

public class AccountNumber : ValueObject
{
    public string Value { get; private set; }

    private AccountNumber() { } // For EF Core

    public AccountNumber(string value)
    {
        if (string.IsNullOrWhiteSpace(value))
            throw new DomainException("Account number cannot be empty");

        if (value.Length != 10)
            throw new DomainException("Account number must be 10 digits");

        if (!value.All(char.IsDigit))
            throw new DomainException("Account number must contain only digits");

        Value = value;
    }

    public static AccountNumber Generate()
    {
        var random = new Random();
        var accountNumber = string.Join("", Enumerable.Range(0, 10)
            .Select(_ => random.Next(0, 10)));
        return new AccountNumber(accountNumber);
    }

    protected override IEnumerable<object> GetEqualityComponents()
    {
        yield return Value;
    }

    public override string ToString() => Value;
} 