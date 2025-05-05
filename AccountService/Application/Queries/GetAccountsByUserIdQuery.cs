using Application.DTo;
using MediatR;

namespace Application.Queries
{
    public record GetAccountsByUserIdQuery(Guid UserId) : IRequest<List<AccountDto>>;
}
