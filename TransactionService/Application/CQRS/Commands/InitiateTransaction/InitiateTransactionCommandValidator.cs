using FluentValidation;
using TransactionService.Domain.Enums;
using TransactionService.Domain.ValueObjects;

namespace TransactionService.Application.CQRS.Commands.InitiateTransaction;

public class InitiateTransactionCommandValidator : AbstractValidator<InitiateTransactionCommand>
{
    public InitiateTransactionCommandValidator()
    {
        RuleFor(x => x.Type)
            .IsInEnum().WithMessage("Invalid transaction type.");

        RuleFor(x => x.Amount)
            .NotEmpty().WithMessage("Amount cannot be empty.")
            .Must(BeValidMoney).WithMessage("Amount must be greater than zero and have a valid currency.");

        RuleFor(x => x.InitiatedBy)
            .NotEmpty().WithMessage("InitiatedBy cannot be empty.")
            .MaximumLength(100).WithMessage("InitiatedBy cannot exceed 100 characters.");

        RuleFor(x => x.Description)
            .MaximumLength(500).WithMessage("Description cannot exceed 500 characters.");

        // Conditional validation based on transaction type
        When(x => x.Type == TransactionType.Debit || x.Type == TransactionType.Transfer, () =>
        {
            RuleFor(x => x.AccountFromId)
                .NotEmpty().WithMessage("AccountFromId is required for Debit or Transfer transactions.");
        });

        When(x => x.Type == TransactionType.Credit || x.Type == TransactionType.Transfer, () =>
        {
            RuleFor(x => x.AccountToId)
                .NotEmpty().WithMessage("AccountToId is required for Credit or Transfer transactions.");
        });

        When(x => x.Type == TransactionType.Transfer, () => {
            RuleFor(x => x.AccountFromId)
                .NotEqual(x => x.AccountToId).When(x=> x.AccountFromId.HasValue && x.AccountToId.HasValue)
                .WithMessage("AccountFromId and AccountToId cannot be the same for a transfer.");
        });

    }

    private bool BeValidMoney(Money money)
    {
        return money != null && money.Amount > 0 && !string.IsNullOrWhiteSpace(money.Currency);
    }
} 