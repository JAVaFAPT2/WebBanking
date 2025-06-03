using System.Threading;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Logging;
using TransactionService.Domain.Interfaces;
using TransactionService.Domain.Entities; // Required for Transaction entity
using TransactionService.Domain.Enums;   // Required for TransactionStatus enum

namespace TransactionService.Application.CQRS.Commands.UpdateTransactionStatus;

public class UpdateTransactionStatusCommandHandler : IRequestHandler<UpdateTransactionStatusCommand, bool>
{
    private readonly ITransactionRepository _transactionRepository;
    private readonly ILogger<UpdateTransactionStatusCommandHandler> _logger;
    // Potentially IEventProducer if status changes should also be published

    public UpdateTransactionStatusCommandHandler(
        ITransactionRepository transactionRepository,
        ILogger<UpdateTransactionStatusCommandHandler> logger)
    {
        _transactionRepository = transactionRepository;
        _logger = logger;
    }

    public async Task<bool> Handle(UpdateTransactionStatusCommand request, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Updating transaction status for ID: {TransactionId} to {NewStatus}", request.TransactionId, request.NewStatus);

        var transaction = await _transactionRepository.GetByIdAsync(request.TransactionId, cancellationToken);

        if (transaction == null)
        {
            _logger.LogWarning("Transaction with ID: {TransactionId} not found for status update.", request.TransactionId);
            return false;
        }

        // You might want to add logic here to check if the status transition is valid
        // For example, a transaction that Succeeded should not go back to Pending.
        // This could be part of the Transaction entity's domain logic as well.

        transaction.SetStatus(request.NewStatus, request.Reason);

        if (!string.IsNullOrWhiteSpace(request.ReferenceNumber))
        {
            transaction.SetReferenceNumber(request.ReferenceNumber);
        }

        await _transactionRepository.UpdateAsync(transaction, cancellationToken);
        
        // Domain events (like TransactionStatusChangedEvent) are raised within transaction.SetStatus()
        // and should be dispatched by a MediatR behavior after the transaction is successfully saved.

        _logger.LogInformation("Transaction status updated for ID: {TransactionId}", request.TransactionId);
        return true;
    }
} 