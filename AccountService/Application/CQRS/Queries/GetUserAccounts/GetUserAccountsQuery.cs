using AutoMapper;
using Domain.Interface;
using FluentValidation;
using MediatR;
using Application.CQRS.Queries.GetAccount;
using Application.CQRS.DTO;
using System;
using System.Collections.Generic;

namespace Application.CQRS.Queries.GetUserAccounts;

public record GetUserAccountsQuery(Guid UserId) : IRequest<IEnumerable<AccountDto>>;

public class GetUserAccountsQueryHandler : IRequestHandler<GetUserAccountsQuery, IEnumerable<AccountDto>>
{
    private readonly IAccountRepository _accountRepository;

    public GetUserAccountsQueryHandler(IAccountRepository accountRepository)
    {
        _accountRepository = accountRepository;
    }

    public async Task<IEnumerable<AccountDto>> Handle(GetUserAccountsQuery request, CancellationToken cancellationToken)
    {
        var accounts = await _accountRepository.GetByUserIdAsync(request.UserId);
        return accounts.Select(account => new AccountDto(
            account.Id,
            account.AccountNumber.Value,
            account.UserId,
            account.Type.ToString(),
            account.Balance.Amount,
            account.Status.ToString(),
            account.Balance.Currency,
            account.CreatedAt,
            account.LastModifiedAt
        ));
    }
}

public class GetUserAccountsQueryValidator : FluentValidation.AbstractValidator<GetUserAccountsQuery>
{
    public GetUserAccountsQueryValidator()
    {
        RuleFor(x => x.UserId)
            .NotEmpty()
            .WithMessage("User ID is required");
    }
} 