using AutoMapper;
using Domain.Interface;
using FluentValidation;
using MediatR;
using Application.CQRS.Queries.GetAccount;

namespace Application.CQRS.Queries.GetUserAccounts;

public record GetUserAccountsQuery(Guid UserId) : IRequest<IEnumerable<AccountDto>>;

public class GetUserAccountsQueryHandler : IRequestHandler<GetUserAccountsQuery, IEnumerable<AccountDto>>
{
    private readonly IAccountRepository _accountRepository;
    private readonly IMapper _mapper;

    public GetUserAccountsQueryHandler(IAccountRepository accountRepository, IMapper mapper)
    {
        _accountRepository = accountRepository;
        _mapper = mapper;
    }

    public async Task<IEnumerable<AccountDto>> Handle(GetUserAccountsQuery request, CancellationToken cancellationToken)
    {
        var accounts = await _accountRepository.GetByUserIdAsync(request.UserId);
        return _mapper.Map<IEnumerable<AccountDto>>(accounts);
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