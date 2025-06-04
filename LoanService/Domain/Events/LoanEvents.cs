using Domain.Models;
using Domain.ValueObjects;

namespace Domain.Events;

public record LoanCreatedEvent : IDomainEvent
{
    public Guid LoanId { get; }
    public Guid AccountId { get; }
    public Money Amount { get; }
    public DateTime OccurredOn { get; } = DateTime.UtcNow;

    public LoanCreatedEvent(Guid loanId, Guid accountId, Money amount)
    {
        LoanId = loanId;
        AccountId = accountId;
        Amount = amount;
    }
}

public record LoanApprovedEvent : IDomainEvent
{
    public Guid LoanId { get; }
    public DateTime OccurredOn { get; } = DateTime.UtcNow;

    public LoanApprovedEvent(Guid loanId)
    {
        LoanId = loanId;
    }
}

public record LoanPaymentMadeEvent : IDomainEvent
{
    public Guid LoanId { get; }
    public Money Amount { get; }
    public Money RemainingAmount { get; }
    public DateTime OccurredOn { get; } = DateTime.UtcNow;

    public LoanPaymentMadeEvent(Guid loanId, Money amount, Money remainingAmount)
    {
        LoanId = loanId;
        Amount = amount;
        RemainingAmount = remainingAmount;
    }
}

public record LoanPaidOffEvent : IDomainEvent
{
    public Guid LoanId { get; }
    public DateTime OccurredOn { get; } = DateTime.UtcNow;

    public LoanPaidOffEvent(Guid loanId)
    {
        LoanId = loanId;
    }
} 