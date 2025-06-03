using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Logging;
using TransactionService.Application.CQRS.Queries.GetTransactionById; // For TransactionDetailsDto
using TransactionService.Domain.Interfaces;

namespace TransactionService.Application.CQRS.Queries.GetTransactionsByAccountId;

public class GetTransactionsByAccountIdQueryHandler : IRequestHandler<GetTransactionsByAccountIdQuery, IEnumerable<TransactionDetailsDto>>
{
    private readonly ITransactionRepository _transactionRepository;
    private readonly ILogger<GetTransactionsByAccountIdQueryHandler> _logger;

    public GetTransactionsByAccountIdQueryHandler(ITransactionRepository transactionRepository, ILogger<GetTransactionsByAccountIdQueryHandler> logger)
    {
        _transactionRepository = transactionRepository;
        _logger = logger;
    }

    public async Task<IEnumerable<TransactionDetailsDto>> Handle(GetTransactionsByAccountIdQuery request, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Fetching transactions for Account ID: {AccountId}, Page: {PageNumber}, Size: {PageSize}", request.AccountId, request.PageNumber, request.PageSize);

        var transactions = await _transactionRepository.GetByAccountIdAsync(request.AccountId, request.FromDate, request.ToDate, cancellationToken);

        // Basic in-memory pagination for now. For larger datasets, pagination should ideally be done at the database level.
        var pagedTransactions = transactions
            .OrderByDescending(t => t.InitiatedAt) // Default sort order
            .Skip((request.PageNumber - 1) * request.PageSize)
            .Take(request.PageSize)
            .ToList();

        if (!pagedTransactions.Any())
        {
            _logger.LogInformation("No transactions found for Account ID: {AccountId}", request.AccountId);
            return Enumerable.Empty<TransactionDetailsDto>();
        }

        return pagedTransactions.Select(transaction => new TransactionDetailsDto(
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
        ));
    }
} 