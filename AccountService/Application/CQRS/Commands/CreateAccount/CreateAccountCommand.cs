using Domain.Interface;
using Domain.Models;
using Domain.ValueObjects;
using FluentValidation;
using MediatR;

namespace Application.CQRS.Commands.CreateAccount;

public record CreateAccountCommand(
    Guid UserId,
    AccountType Type,
    string Currency) : IRequest<Guid>;

public class CreateAccountCommandHandler : IRequestHandler<CreateAccountCommand, Guid>
{
    private readonly IAccountRepository _accountRepository;

    public CreateAccountCommandHandler(IAccountRepository accountRepository)
    {
        _accountRepository = accountRepository;
    }

    public async Task<Guid> Handle(CreateAccountCommand request, CancellationToken cancellationToken)
    {
        var accountNumber = AccountNumber.Generate();
        var account = new Account(accountNumber, request.UserId, request.Type, request.Currency);
        
        await _accountRepository.AddAsync(account);
        await _accountRepository.SaveChangesAsync();
        
        return account.Id;
    }
}

public class CreateAccountCommandValidator : AbstractValidator<CreateAccountCommand>
{
    public CreateAccountCommandValidator()
    {
        RuleFor(x => x.UserId)
            .NotEmpty()
            .WithMessage("User ID is required");

        RuleFor(x => x.Type)
            .IsInEnum()
            .WithMessage("Invalid account type");

        RuleFor(x => x.Currency)
            .NotEmpty()
            .WithMessage("Currency is required")
            .Length(3)
            .WithMessage("Currency must be 3 characters");
    }
} 