using Application.CQRS.Commands.ApproveLoan;
using Application.CQRS.Commands.CreateLoan;
using Application.CQRS.Commands.MakeLoanPayment;
using Application.CQRS.Queries.GetAccountLoans;
using Application.CQRS.Queries.GetLoan;
using Domain.Models;
using Domain.ValueObjects;
using Grpc.Core;
using MediatR;
using Presentation.Protos;

namespace Presentation.Services;

public class LoanGrpcService : Protos.LoanService.LoanServiceBase
{
    private readonly IMediator _mediator;
    private readonly ILogger<LoanGrpcService> _logger;

    public LoanGrpcService(IMediator mediator, ILogger<LoanGrpcService> logger)
    {
        _mediator = mediator;
        _logger = logger;
    }

    public override async Task<CreateLoanResponse> CreateLoan(CreateLoanRequest request, ServerCallContext context)
    {
        var correlationId = Guid.NewGuid().ToString();
        _logger.LogInformation(
            "Creating loan request [{CorrelationId}] - Account: {AccountId}, Type: {LoanType}, Amount: {Amount} {Currency}",
            correlationId, request.AccountId, request.LoanType, request.Amount.Amount, request.Amount.Currency);

        try
        {
            var command = new CreateLoanCommand(
                Guid.Parse(request.AccountId),
                request.CardId != null ? Guid.Parse(request.CardId) : null,
                MapLoanType(request.LoanType),
                new Money((decimal)request.Amount.Amount, request.Amount.Currency),
                (decimal)request.InterestRate,
                request.TermMonths
            );

            var loanId = await _mediator.Send(command);
            
            _logger.LogInformation(
                "Loan created successfully [{CorrelationId}] - LoanId: {LoanId}",
                correlationId, loanId);

            return new CreateLoanResponse { LoanId = loanId.ToString() };
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Failed to create loan [{CorrelationId}] - Account: {AccountId}, Error: {ErrorMessage}",
                correlationId, request.AccountId, ex.Message);
            throw new RpcException(new Status(StatusCode.Internal, "Error creating loan"));
        }
    }

    public override async Task<ApproveLoanResponse> ApproveLoan(ApproveLoanRequest request, ServerCallContext context)
    {
        var correlationId = Guid.NewGuid().ToString();
        _logger.LogInformation(
            "Approving loan [{CorrelationId}] - LoanId: {LoanId}",
            correlationId, request.LoanId);

        try
        {
            var command = new ApproveLoanCommand(Guid.Parse(request.LoanId));
            await _mediator.Send(command);

            _logger.LogInformation(
                "Loan approved successfully [{CorrelationId}] - LoanId: {LoanId}",
                correlationId, request.LoanId);

            return new ApproveLoanResponse { Success = true };
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Failed to approve loan [{CorrelationId}] - LoanId: {LoanId}, Error: {ErrorMessage}",
                correlationId, request.LoanId, ex.Message);
            throw new RpcException(new Status(StatusCode.Internal, "Error approving loan"));
        }
    }

    public override async Task<MakeLoanPaymentResponse> MakeLoanPayment(MakeLoanPaymentRequest request, ServerCallContext context)
    {
        var correlationId = Guid.NewGuid().ToString();
        _logger.LogInformation(
            "Processing loan payment [{CorrelationId}] - LoanId: {LoanId}, Amount: {Amount} {Currency}",
            correlationId, request.LoanId, request.PaymentAmount.Amount, request.PaymentAmount.Currency);

        try
        {
            var command = new MakeLoanPaymentCommand(
                Guid.Parse(request.LoanId),
                new Money((decimal)request.PaymentAmount.Amount, request.PaymentAmount.Currency)
            );

            await _mediator.Send(command);

            _logger.LogInformation(
                "Payment processed successfully [{CorrelationId}] - LoanId: {LoanId}",
                correlationId, request.LoanId);

            return new MakeLoanPaymentResponse { Success = true };
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Failed to process payment [{CorrelationId}] - LoanId: {LoanId}, Error: {ErrorMessage}",
                correlationId, request.LoanId, ex.Message);
            throw new RpcException(new Status(StatusCode.Internal, "Error making loan payment"));
        }
    }

    public override async Task<LoanResponse> GetLoan(GetLoanRequest request, ServerCallContext context)
    {
        var correlationId = Guid.NewGuid().ToString();
        _logger.LogInformation(
            "Retrieving loan details [{CorrelationId}] - LoanId: {LoanId}",
            correlationId, request.LoanId);

        try
        {
            var query = new GetLoanQuery(Guid.Parse(request.LoanId));
            var loan = await _mediator.Send(query);

            if (loan == null)
            {
                _logger.LogWarning(
                    "Loan not found [{CorrelationId}] - LoanId: {LoanId}",
                    correlationId, request.LoanId);
                throw new RpcException(new Status(StatusCode.NotFound, "Loan not found"));
            }

            _logger.LogInformation(
                "Loan details retrieved successfully [{CorrelationId}] - LoanId: {LoanId}, Status: {Status}",
                correlationId, request.LoanId, loan.Status);

            return MapLoanToResponse(loan);
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Failed to retrieve loan details [{CorrelationId}] - LoanId: {LoanId}, Error: {ErrorMessage}",
                correlationId, request.LoanId, ex.Message);
            throw new RpcException(new Status(StatusCode.Internal, "Error retrieving loan"));
        }
    }

    public override async Task<GetAccountLoansResponse> GetAccountLoans(GetAccountLoansRequest request, ServerCallContext context)
    {
        var correlationId = Guid.NewGuid().ToString();
        _logger.LogInformation(
            "Retrieving account loans [{CorrelationId}] - AccountId: {AccountId}",
            correlationId, request.AccountId);

        try
        {
            var query = new GetAccountLoansQuery(Guid.Parse(request.AccountId));
            var loans = await _mediator.Send(query);

            _logger.LogInformation(
                "Account loans retrieved successfully [{CorrelationId}] - AccountId: {AccountId}, Count: {Count}",
                correlationId, request.AccountId, loans.Count());

            var response = new GetAccountLoansResponse();
            response.Loans.AddRange(loans.Select(MapLoanToResponse));
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Failed to retrieve account loans [{CorrelationId}] - AccountId: {AccountId}, Error: {ErrorMessage}",
                correlationId, request.AccountId, ex.Message);
            throw new RpcException(new Status(StatusCode.Internal, "Error retrieving account loans"));
        }
    }

    private static LoanResponse MapLoanToResponse(Loan loan)
    {
        return new LoanResponse
        {
            LoanId = loan.Id.ToString(),
            AccountId = loan.AccountId.ToString(),
            CardId = loan.CardId?.ToString(),
            LoanType = MapLoanType(loan.Type),
            Status = MapLoanStatus(loan.Status),
            Amount = new Protos.Money
            {
                Amount = (double)loan.Amount.Amount,
                Currency = loan.Amount.Currency
            },
            RemainingAmount = new Protos.Money
            {
                Amount = (double)loan.RemainingAmount.Amount,
                Currency = loan.RemainingAmount.Currency
            },
            InterestRate = (double)loan.InterestRate,
            TermMonths = loan.TermMonths,
            MonthlyPayment = new Protos.Money
            {
                Amount = (double)loan.MonthlyPayment.Amount,
                Currency = loan.MonthlyPayment.Currency
            },
            StartDate = loan.StartDate.ToString("o"),
            EndDate = loan.EndDate.ToString("o"),
            CreatedAt = loan.CreatedAt.ToString("o"),
            LastPaymentDate = loan.LastPaymentDate?.ToString("o"),
            RejectionReason = loan.RejectionReason
        };
    }

    private static LoanType MapLoanType(Protos.LoanType type) => type switch
    {
        Protos.LoanType.LoanTypePersonal => LoanType.Personal,
        Protos.LoanType.LoanTypeCreditCard => LoanType.CreditCard,
        Protos.LoanType.LoanTypeMortgage => LoanType.Mortgage,
        Protos.LoanType.LoanTypeAuto => LoanType.Auto,
        Protos.LoanType.LoanTypeStudent => LoanType.Student,
        Protos.LoanType.LoanTypeBusiness => LoanType.Business,
        _ => throw new ArgumentOutOfRangeException(nameof(type))
    };

    private static Protos.LoanType MapLoanType(LoanType type) => type switch
    {
        LoanType.Personal => Protos.LoanType.LoanTypePersonal,
        LoanType.CreditCard => Protos.LoanType.LoanTypeCreditCard,
        LoanType.Mortgage => Protos.LoanType.LoanTypeMortgage,
        LoanType.Auto => Protos.LoanType.LoanTypeAuto,
        LoanType.Student => Protos.LoanType.LoanTypeStudent,
        LoanType.Business => Protos.LoanType.LoanTypeBusiness,
        _ => throw new ArgumentOutOfRangeException(nameof(type))
    };

    private static Protos.LoanStatus MapLoanStatus(LoanStatus status) => status switch
    {
        LoanStatus.Pending => Protos.LoanStatus.LoanStatusPending,
        LoanStatus.Approved => Protos.LoanStatus.LoanStatusApproved,
        LoanStatus.Active => Protos.LoanStatus.LoanStatusActive,
        LoanStatus.Rejected => Protos.LoanStatus.LoanStatusRejected,
        LoanStatus.Closed => Protos.LoanStatus.LoanStatusClosed,
        LoanStatus.Default => Protos.LoanStatus.LoanStatusDefault,
        LoanStatus.PaidOff => Protos.LoanStatus.LoanStatusPaidOff,
        _ => throw new ArgumentOutOfRangeException(nameof(status))
    };
} 