using MediatR;

namespace Application.CQRS.Commands.ApproveLoan;

public record ApproveLoanCommand(Guid LoanId) : IRequest; 