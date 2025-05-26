using AutoMapper;
using Domain.Interface;
using MediatR;

namespace Application.CQRS.Queries.GetAccount;

public record GetAccountQuery(Guid Id) : IRequest<AccountDto?>;

public class GetAccountQueryHandler : IRequestHandler<GetAccountQuery, AccountDto?>
{
    private readonly IAccountRepository _accountRepository;
    private readonly IMapper _mapper;

    public GetAccountQueryHandler(IAccountRepository accountRepository, IMapper mapper)
    {
        _accountRepository = accountRepository;
        _mapper = mapper;
    }

    public async Task<AccountDto?> Handle(GetAccountQuery request, CancellationToken cancellationToken)
    {
        var account = await _accountRepository.GetByIdAsync(request.Id);
        return account == null ? null : _mapper.Map<AccountDto>(account);
    }
}

public class AccountDto
{
    public Guid Id { get; set; }
    public string AccountNumber { get; set; } = null!;
    public Guid UserId { get; set; }
    public string Type { get; set; } = null!;
    public decimal Balance { get; set; }
    public string Status { get; set; } = null!;
    public string Currency { get; set; } = null!;
    public DateTime CreatedAt { get; set; }
    public DateTime? LastModifiedAt { get; set; }
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