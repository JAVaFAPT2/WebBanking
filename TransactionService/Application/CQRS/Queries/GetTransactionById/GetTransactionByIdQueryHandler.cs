using System.Threading;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Logging;
using TransactionService.Domain.Interfaces;

namespace TransactionService.Application.CQRS.Queries.GetTransactionById;

public class GetTransactionByIdQueryHandler : IRequestHandler<GetTransactionByIdQuery, TransactionDetailsDto?>
{
    private readonly ITransactionRepository _transactionRepository;
    private readonly ILogger<GetTransactionByIdQueryHandler> _logger;

    public GetTransactionByIdQueryHandler(ITransactionRepository transactionRepository, ILogger<GetTransactionByIdQueryHandler> logger)
    {
        _transactionRepository = transactionRepository;
        _logger = logger;
    }

    public async Task<TransactionDetailsDto?> Handle(GetTransactionByIdQuery request, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Fetching transaction by ID: {TransactionId}", request.TransactionId);
        var transaction = await _transactionRepository.GetByIdAsync(request.TransactionId, cancellationToken);

        if (transaction == null)
        {
            _logger.LogWarning("Transaction with ID: {TransactionId} not found.", request.TransactionId);
            return null;
        }

        return new TransactionDetailsDto(
            transaction.Id,
            transaction.CorrelationId,
            transaction.AccountFromId,
            transaction.AccountToId,
            transaction.Type,
            transaction.Status,
            transaction.Amount,
            transaction.InitiatedAt,
            transaction.LastUpdatedAt,
            transaction.Description,
            transaction.FailureReason,
            transaction.ReferenceNumber,
            transaction.InitiatedBy
        );
    }
} 