using Domain.Models;
using Domain.ValueObjects;
using MediatR;

namespace Application.CQRS.Commands.CreateLoan;

public record CreateLoanCommand(
    Guid AccountId,
    Guid? CardId,
    LoanType LoanType,
    Money Amount,
    decimal InterestRate,
    int TermMonths) : IRequest<Guid>; 