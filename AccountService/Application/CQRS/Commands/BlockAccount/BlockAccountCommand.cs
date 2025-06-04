using Domain.Interface;
using Domain.Models;
using FluentValidation;
using MediatR;

namespace Application.CQRS.Commands.BlockAccount;

public record BlockAccountCommand(Guid AccountId, bool Block) : IRequest<bool>;

public class BlockAccountCommandHandler : IRequestHandler<BlockAccountCommand, bool>
{
    private readonly IAccountRepository _accountRepository;

    public BlockAccountCommandHandler(IAccountRepository accountRepository)
    {
        _accountRepository = accountRepository;
    }

    public async Task<bool> Handle(BlockAccountCommand request, CancellationToken cancellationToken)
    {
        var account = await _accountRepository.GetByIdAsync(request.AccountId);
        if (account == null)
            return false;

        if (request.Block)
            account.Block();
        else
            account.Unblock();
        
        await _accountRepository.UpdateAsync(account);
        await _accountRepository.SaveChangesAsync();
        
        return true;
    }
}

public class BlockAccountCommandValidator : AbstractValidator<BlockAccountCommand>
{
    public BlockAccountCommandValidator()
    {
        RuleFor(x => x.AccountId)
            .NotEmpty()
            .WithMessage("Account ID is required");
    }
} 