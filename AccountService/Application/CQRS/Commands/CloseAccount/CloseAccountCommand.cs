using Domain.Interface;
using Domain.Models;
using FluentValidation;
using MediatR;

namespace Application.CQRS.Commands.CloseAccount;

public record CloseAccountCommand(Guid AccountId) : IRequest<bool>;

public class CloseAccountCommandHandler : IRequestHandler<CloseAccountCommand, bool>
{
    private readonly IAccountRepository _accountRepository;

    public CloseAccountCommandHandler(IAccountRepository accountRepository)
    {
        _accountRepository = accountRepository;
    }

    public async Task<bool> Handle(CloseAccountCommand request, CancellationToken cancellationToken)
    {
        var account = await _accountRepository.GetByIdAsync(request.AccountId);
        if (account == null)
            return false;

        account.Close();
        
        await _accountRepository.UpdateAsync(account);
        await _accountRepository.SaveChangesAsync();
        
        return true;
    }
}

public class CloseAccountCommandValidator : AbstractValidator<CloseAccountCommand>
{
    public CloseAccountCommandValidator()
    {
        RuleFor(x => x.AccountId)
            .NotEmpty()
            .WithMessage("Account ID is required");
    }
} 