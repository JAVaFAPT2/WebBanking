using Domain.Interface;
using Domain.Models;
using MediatR;
using Microsoft.Extensions.Logging;

namespace Application.CQRS.Queries.GetAccountLoans;

public class GetAccountLoansQueryHandler : IRequestHandler<GetAccountLoansQuery, IEnumerable<Loan>>
{
    private readonly ILoanRepository _loanRepository;
    private readonly ILogger<GetAccountLoansQueryHandler> _logger;

    public GetAccountLoansQueryHandler(
        ILoanRepository loanRepository,
        ILogger<GetAccountLoansQueryHandler> logger)
    {
        _loanRepository = loanRepository;
        _logger = logger;
    }

    public async Task<IEnumerable<Loan>> Handle(GetAccountLoansQuery request, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Retrieving loans for account {AccountId}", request.AccountId);

        var loans = await _loanRepository.GetByAccountIdAsync(request.AccountId, cancellationToken);

        _logger.LogInformation(
            "Retrieved {Count} loans for account {AccountId}",
            loans.Count(), request.AccountId);

        return loans;
    }
} 