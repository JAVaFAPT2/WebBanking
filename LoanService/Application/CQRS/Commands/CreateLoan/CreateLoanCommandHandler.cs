using Domain.Configuration;
using Domain.Interface;
using Domain.Models;
using MediatR;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace Application.CQRS.Commands.CreateLoan;

public class CreateLoanCommandHandler : IRequestHandler<CreateLoanCommand, Guid>
{
    private readonly ILoanRepository _loanRepository;
    private readonly ILogger<CreateLoanCommandHandler> _logger;
    private readonly LoanServiceSettings _settings;

    public CreateLoanCommandHandler(
        ILoanRepository loanRepository,
        ILogger<CreateLoanCommandHandler> logger,
        IOptions<LoanServiceSettings> settings)
    {
        _loanRepository = loanRepository;
        _logger = logger;
        _settings = settings.Value;
    }

    public async Task<Guid> Handle(CreateLoanCommand request, CancellationToken cancellationToken)
    {
        _logger.LogInformation(
            "Creating loan for account {AccountId} of type {LoanType} for amount {Amount} {Currency}",
            request.AccountId, request.LoanType, request.Amount.Amount, request.Amount.Currency);

        ValidateRequest(request);

        var loan = Loan.Create(
            request.AccountId,
            request.CardId,
            request.LoanType,
            request.Amount,
            request.InterestRate,
            request.TermMonths);

        await _loanRepository.AddAsync(loan, cancellationToken);

        _logger.LogInformation(
            "Created loan {LoanId} for account {AccountId}",
            loan.Id, request.AccountId);

        return loan.Id;
    }

    private void ValidateRequest(CreateLoanCommand request)
    {
        if (request.Amount.Amount > _settings.MaxLoanAmount)
            throw new ArgumentException($"Loan amount cannot exceed {_settings.MaxLoanAmount}");

        if (request.Amount.Amount < _settings.MinLoanAmount)
            throw new ArgumentException($"Loan amount cannot be less than {_settings.MinLoanAmount}");

        if (request.TermMonths > _settings.MaxTermMonths)
            throw new ArgumentException($"Loan term cannot exceed {_settings.MaxTermMonths} months");

        if (request.TermMonths < _settings.MinTermMonths)
            throw new ArgumentException($"Loan term cannot be less than {_settings.MinTermMonths} months");

        if (request.InterestRate <= 0)
            throw new ArgumentException("Interest rate must be greater than zero");
    }
} 