using Domain.Interface;
using Domain.Models;
using Domain.ValueObjects;
using FluentValidation;
using MediatR;

namespace Application.CQRS.Commands.UpdateBalance;

public record UpdateBalanceCommand(
    Guid AccountId,
    decimal Amount,
    TransactionType Type) : IRequest<bool>;

public class UpdateBalanceCommandHandler : IRequestHandler<UpdateBalanceCommand, bool>
{
    private readonly IAccountRepository _accountRepository;

    public UpdateBalanceCommandHandler(IAccountRepository accountRepository)
    {
        _accountRepository = accountRepository;
    }

    public async Task<bool> Handle(UpdateBalanceCommand request, CancellationToken cancellationToken)
    {
        var account = await _accountRepository.GetByIdAsync(request.AccountId);
        if (account == null)
            return false;

        var money = new Money(request.Amount, account.Currency);
        account.UpdateBalance(money, request.Type);
        
        await _accountRepository.UpdateAsync(account);
        await _accountRepository.SaveChangesAsync();
        
        return true;
    }
}

public class UpdateBalanceCommandValidator : AbstractValidator<UpdateBalanceCommand>
{
    public UpdateBalanceCommandValidator()
    {
        RuleFor(x => x.AccountId)
            .NotEmpty()
            .WithMessage("Account ID is required");

        RuleFor(x => x.Amount)
            .GreaterThan(0)
            .WithMessage("Amount must be greater than 0");

        RuleFor(x => x.Type)
            .IsInEnum()
            .WithMessage("Invalid transaction type");
    }
} 