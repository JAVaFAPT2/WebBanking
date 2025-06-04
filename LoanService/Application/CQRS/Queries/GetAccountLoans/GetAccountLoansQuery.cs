using Domain.Models;
using MediatR;

namespace Application.CQRS.Queries.GetAccountLoans;

public record GetAccountLoansQuery(Guid AccountId) : IRequest<IEnumerable<Loan>>; 