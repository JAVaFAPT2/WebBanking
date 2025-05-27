using Domain.ValueObjects;
using MediatR;

namespace Application.CQRS.Commands.MakeLoanPayment;

public record MakeLoanPaymentCommand(Guid LoanId, Money Payment) : IRequest; 