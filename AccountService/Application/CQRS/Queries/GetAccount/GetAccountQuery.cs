using AutoMapper;
using Domain.Interface;
using MediatR;
using Application.CQRS.DTO;
using System;

namespace Application.CQRS.Queries.GetAccount;

public record GetAccountQuery(Guid AccountId) : IRequest<AccountDto>;

public class GetAccountQueryHandler : IRequestHandler<GetAccountQuery, AccountDto>
{
    private readonly IAccountRepository _accountRepository;

    public GetAccountQueryHandler(IAccountRepository accountRepository)
    {
        _accountRepository = accountRepository;
    }

    public async Task<AccountDto> Handle(GetAccountQuery request, CancellationToken cancellationToken)
    {
        var account = await _accountRepository.GetByIdAsync(request.AccountId);
        if (account == null) return null;

        return new AccountDto(
            account.Id,
            account.AccountNumber.Value,
            account.UserId,
            account.Type.ToString(),
            account.Balance.Amount,
            account.Status.ToString(),
            account.Balance.Currency,
            account.CreatedAt,
            account.LastModifiedAt
        );
    }
}

public class AccountMappingProfile : Profile
{
    public AccountMappingProfile()
    {
        CreateMap<Domain.Models.Account, AccountDto>()
            .ForMember(dest => dest.AccountNumber, opt => opt.MapFrom(src => src.AccountNumber.Value))
            .ForMember(dest => dest.Type, opt => opt.MapFrom(src => src.Type.ToString()))
            .ForMember(dest => dest.Status, opt => opt.MapFrom(src => src.Status.ToString()))
            .ForMember(dest => dest.Balance, opt => opt.MapFrom(src => src.Balance.Amount));
    }
} 