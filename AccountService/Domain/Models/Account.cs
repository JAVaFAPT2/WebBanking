using Domain.Common;
using Domain.Events;
using Domain.ValueObjects;
using Domain.Interface;

namespace Domain.Models;

public class Account : AggregateRoot
{
    public AccountNumber AccountNumber { get; private set; }
    public Guid UserId { get; private set; }
    public AccountType Type { get; private set; }
    public Money Balance { get; private set; }
    public AccountStatus Status { get; private set; }
    public DateTime CreatedAt { get; private set; }
    public DateTime? LastModifiedAt { get; private set; }
    public string Currency { get; private set; }

    private Account() { } // For EF Core

    public Account(AccountNumber accountNumber, Guid userId, AccountType type, string currency)
    {
        Id = Guid.NewGuid();
        AccountNumber = accountNumber;
        UserId = userId;
        Type = type;
        Currency = currency;
        Balance = Money.Zero(currency);
        Status = AccountStatus.Active;
        CreatedAt = DateTime.UtcNow;
        
        AddDomainEvent(new AccountCreatedEvent(Id, AccountNumber, UserId, Type, Currency));
    }

    public void UpdateBalance(Money amount, TransactionType transactionType)
    {
        if (Status != AccountStatus.Active)
            throw new DomainException("Cannot update balance for inactive account");

        var oldBalance = Balance;
        Balance = transactionType == TransactionType.Credit 
            ? Balance.Add(amount) 
            : Balance.Subtract(amount);

        if (Balance.Amount < 0)
            throw new DomainException("Insufficient funds");

        LastModifiedAt = DateTime.UtcNow;
        
        AddDomainEvent(new BalanceUpdatedEvent(Id, AccountNumber, oldBalance, Balance, transactionType));
    }

    public void Close()
    {
        if (Status == AccountStatus.Closed)
            throw new DomainException("Account is already closed");

        if (Balance.Amount != 0)
            throw new DomainException("Cannot close account with non-zero balance");

        Status = AccountStatus.Closed;
        LastModifiedAt = DateTime.UtcNow;
        
        AddDomainEvent(new AccountClosedEvent(Id, AccountNumber));
    }

    public void Block()
    {
        if (Status == AccountStatus.Blocked)
            throw new DomainException("Account is already blocked");

        Status = AccountStatus.Blocked;
        LastModifiedAt = DateTime.UtcNow;
        
        AddDomainEvent(new AccountBlockedEvent(Id, AccountNumber));
    }

    public void Unblock()
    {
        if (Status != AccountStatus.Blocked)
            throw new DomainException("Account is not blocked");

        Status = AccountStatus.Active;
        LastModifiedAt = DateTime.UtcNow;
        
        AddDomainEvent(new AccountUnblockedEvent(Id, AccountNumber));
    }
}

public enum AccountType
{
    Savings,
    Checking,
    FixedDeposit,
    Business
}

public enum AccountStatus
{
    Active,
    Blocked,
    Closed
}

public enum TransactionType
{
    Credit,
    Debit
} 