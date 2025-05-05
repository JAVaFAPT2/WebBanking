using Application.DTo;
using MediatR;

namespace Application.Queries;

public record GetAccountByIdQuery(Guid AccountId) : IRequest<AccountDto>;