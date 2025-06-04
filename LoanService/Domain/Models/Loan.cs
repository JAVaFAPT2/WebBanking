using Domain.Common;
using Domain.Events;
using Domain.ValueObjects;

namespace Domain.Models;

public class Loan : AggregateRoot
{
    public Guid Id { get; private set; }
    public Guid AccountId { get; private set; }
    public Guid? CardId { get; private set; }
    public LoanType Type { get; private set; }
    public LoanStatus Status { get; private set; }
    public Money Amount { get; private set; } = default!;
    public Money RemainingAmount { get; private set; } = default!;
    public decimal InterestRate { get; private set; }
    public int TermMonths { get; private set; }
    public Money MonthlyPayment { get; private set; } = default!;
    public DateTime StartDate { get; private set; }
    public DateTime EndDate { get; private set; }
    public DateTime CreatedAt { get; private set; }
    public DateTime? LastPaymentDate { get; private set; }
    public string? RejectionReason { get; private set; }

    private Loan() { }

    public static Loan Create(
        Guid accountId,
        Guid? cardId,
        LoanType type,
        Money amount,
        decimal interestRate,
        int termMonths)
    {
        var loan = new Loan
        {
            Id = Guid.NewGuid(),
            AccountId = accountId,
            CardId = cardId,
            Type = type,
            Status = LoanStatus.Pending,
            Amount = amount,
            RemainingAmount = amount,
            InterestRate = interestRate,
            TermMonths = termMonths,
            StartDate = DateTime.UtcNow,
            EndDate = DateTime.UtcNow.AddMonths(termMonths),
            CreatedAt = DateTime.UtcNow
        };

        // Calculate monthly payment using the loan amortization formula
        var monthlyRate = (double)(interestRate / 12 / 100);
        var denominator = Math.Pow(1 + monthlyRate, termMonths) - 1;
        var monthlyPaymentAmount = (double)amount.Amount * monthlyRate * Math.Pow(1 + monthlyRate, termMonths) / denominator;
        loan.MonthlyPayment = new Money((decimal)Math.Round(monthlyPaymentAmount, 2), amount.Currency);

        loan.AddDomainEvent(new LoanCreatedEvent(loan.Id, accountId, amount));
        return loan;
    }

    public void Approve()
    {
        if (Status != LoanStatus.Pending)
            throw new InvalidOperationException($"Cannot approve loan in {Status} status");

        Status = LoanStatus.Active;
        AddDomainEvent(new LoanApprovedEvent(Id));
    }

    public void Reject(string reason)
    {
        if (Status != LoanStatus.Pending)
            throw new InvalidOperationException($"Cannot reject loan in {Status} status");

        Status = LoanStatus.Rejected;
        RejectionReason = reason;
    }

    public void MakePayment(Money payment)
    {
        if (Status != LoanStatus.Active)
            throw new InvalidOperationException($"Cannot make payment on loan in {Status} status");

        if (payment.Currency != Amount.Currency)
            throw new InvalidOperationException($"Payment currency {payment.Currency} does not match loan currency {Amount.Currency}");

        if (payment.Amount <= 0)
            throw new ArgumentException("Payment amount must be greater than zero");

        if (payment.Amount > RemainingAmount.Amount)
            throw new ArgumentException("Payment amount cannot be greater than remaining amount");

        RemainingAmount = RemainingAmount.Subtract(payment);
        LastPaymentDate = DateTime.UtcNow;

        AddDomainEvent(new LoanPaymentMadeEvent(Id, payment, RemainingAmount));

        if (RemainingAmount.Amount == 0)
        {
            Status = LoanStatus.PaidOff;
            AddDomainEvent(new LoanPaidOffEvent(Id));
        }
    }
} 