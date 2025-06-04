using Domain.Models;
using MediatR;

namespace Application.CQRS.Queries.GetLoan;

public record GetLoanQuery(Guid LoanId) : IRequest<Loan?>; 