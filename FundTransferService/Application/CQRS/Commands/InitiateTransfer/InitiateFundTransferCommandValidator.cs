using FluentValidation;
using FundTransferService.Domain.ValueObjects; // For Money validation if specific rules apply

namespace FundTransferService.Application.CQRS.Commands.InitiateTransfer;

public class InitiateFundTransferCommandValidator : AbstractValidator<InitiateFundTransferCommand>
{
    public InitiateFundTransferCommandValidator()
    {
        RuleFor(x => x.FromAccountId)
            .NotEmpty().WithMessage("FromAccountId is required.")
            .NotEqual(Guid.Empty).WithMessage("FromAccountId cannot be an empty GUID.");

        RuleFor(x => x.ToAccountId)
            .NotEmpty().WithMessage("ToAccountId is required.")
            .NotEqual(Guid.Empty).WithMessage("ToAccountId cannot be an empty GUID.");

        RuleFor(x => x.Amount)
            .NotNull().WithMessage("Amount is required.")
            .Must(amount => amount.Amount > 0).WithMessage("Transfer amount must be positive.")
            .When(x => x.Amount != null);
        
        RuleFor(x => x.FromAccountId)
            .NotEqual(x => x.ToAccountId)
            .WithMessage("Cannot transfer funds to the same account.");

        RuleFor(x => x.ReferenceNumber)
            .MaximumLength(50).WithMessage("ReferenceNumber cannot exceed 50 characters.")
            .When(x => !string.IsNullOrEmpty(x.ReferenceNumber));
    }
} 