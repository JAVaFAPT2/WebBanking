using Domain.Interface;
using MediatR;
using Microsoft.Extensions.Logging;

namespace Application.CQRS.Commands.MakeLoanPayment;

public class MakeLoanPaymentCommandHandler : IRequestHandler<MakeLoanPaymentCommand>
{
    private readonly ILoanRepository _loanRepository;
    private readonly ILogger<MakeLoanPaymentCommandHandler> _logger;

    public MakeLoanPaymentCommandHandler(
        ILoanRepository loanRepository,
        ILogger<MakeLoanPaymentCommandHandler> logger)
    {
        _loanRepository = loanRepository;
        _logger = logger;
    }

    public async Task Handle(MakeLoanPaymentCommand request, CancellationToken cancellationToken)
    {
        _logger.LogInformation(
            "Processing payment for loan {LoanId}: {Amount} {Currency}",
            request.LoanId, request.Payment.Amount, request.Payment.Currency);

        var loan = await _loanRepository.GetByIdAsync(request.LoanId, cancellationToken)
            ?? throw new ArgumentException($"Loan {request.LoanId} not found");

        loan.MakePayment(request.Payment);
        await _loanRepository.UpdateAsync(loan, cancellationToken);

        _logger.LogInformation(
            "Payment processed for loan {LoanId}. Remaining amount: {Amount} {Currency}",
            request.LoanId, loan.RemainingAmount.Amount, loan.RemainingAmount.Currency);
    }
} 