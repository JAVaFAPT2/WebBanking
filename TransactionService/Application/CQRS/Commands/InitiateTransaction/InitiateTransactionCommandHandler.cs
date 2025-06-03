using System.Threading;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Logging;
using TransactionService.Domain.Entities;
using TransactionService.Domain.Interfaces;

namespace TransactionService.Application.CQRS.Commands.InitiateTransaction;

public class InitiateTransactionCommandHandler : IRequestHandler<InitiateTransactionCommand, InitiateTransactionResponse>
{
    private readonly ITransactionRepository _transactionRepository;
    private readonly ILogger<InitiateTransactionCommandHandler> _logger;
    // private readonly IEventProducer _eventProducer; // To be added for Kafka integration

    public InitiateTransactionCommandHandler(
        ITransactionRepository transactionRepository,
        ILogger<InitiateTransactionCommandHandler> logger
        // IEventProducer eventProducer
        )
    {
        _transactionRepository = transactionRepository;
        _logger = logger;
        // _eventProducer = eventProducer;
    }

    public async Task<InitiateTransactionResponse> Handle(InitiateTransactionCommand request, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Initiating transaction: {@Command}", request);

        var transaction = Transaction.Create(
            request.AccountFromId,
            request.AccountToId,
            request.Type,
            request.Amount,
            request.Description,
            request.InitiatedBy,
            request.CorrelationId
        );

        await _transactionRepository.AddAsync(transaction, cancellationToken);

        // After saving, you would typically publish the TransactionCreatedEvent (and other domain events)
        // This can be handled by a MediatR behavior that dispatches domain events after the command handler completes
        // or by explicitly publishing here if preferred, though a behavior is cleaner.

        // Example of publishing an event to Kafka (details will be in Infrastructure)
        // await _eventProducer.ProduceAsync("transaction-events", transaction.DomainEvents.OfType<TransactionCreatedEvent>().FirstOrDefault());

        _logger.LogInformation("Transaction initiated with ID: {TransactionId}", transaction.Id);
        
        return new InitiateTransactionResponse(transaction.Id, transaction.Status);
    }
} 