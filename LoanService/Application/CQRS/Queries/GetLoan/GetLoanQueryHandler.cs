using Domain.Interface;
using Domain.Models;
using MediatR;
using Microsoft.Extensions.Logging;

namespace Application.CQRS.Queries.GetLoan;

public class GetLoanQueryHandler : IRequestHandler<GetLoanQuery, Loan?>
{
    private readonly ILoanRepository _loanRepository;
    private readonly ILogger<GetLoanQueryHandler> _logger;

    public GetLoanQueryHandler(
        ILoanRepository loanRepository,
        ILogger<GetLoanQueryHandler> logger)
    {
        _loanRepository = loanRepository;
        _logger = logger;
    }

    public async Task<Loan?> Handle(GetLoanQuery request, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Retrieving loan {LoanId}", request.LoanId);

        var loan = await _loanRepository.GetByIdAsync(request.LoanId, cancellationToken);

        if (loan == null)
            _logger.LogWarning("Loan {LoanId} not found", request.LoanId);
        else
            _logger.LogInformation("Retrieved loan {LoanId}", request.LoanId);

        return loan;
    }
} 