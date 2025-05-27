using Domain.Interface;
using MediatR;
using Microsoft.Extensions.Logging;

namespace Application.CQRS.Commands.ApproveLoan;

public class ApproveLoanCommandHandler : IRequestHandler<ApproveLoanCommand>
{
    private readonly ILoanRepository _loanRepository;
    private readonly ILogger<ApproveLoanCommandHandler> _logger;

    public ApproveLoanCommandHandler(
        ILoanRepository loanRepository,
        ILogger<ApproveLoanCommandHandler> logger)
    {
        _loanRepository = loanRepository;
        _logger = logger;
    }

    public async Task Handle(ApproveLoanCommand request, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Approving loan {LoanId}", request.LoanId);

        var loan = await _loanRepository.GetByIdAsync(request.LoanId, cancellationToken)
            ?? throw new ArgumentException($"Loan {request.LoanId} not found");

        loan.Approve();
        await _loanRepository.UpdateAsync(loan, cancellationToken);

        _logger.LogInformation("Loan {LoanId} approved successfully", request.LoanId);
    }
} 