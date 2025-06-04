using MediatR;
using Microsoft.Extensions.Logging;
using System.Threading.Tasks;
using TransactionService.Application.CQRS.Commands.UpdateTransactionStatus;
using TransactionService.Application.IntegrationEvents.Events;

namespace TransactionService.Application.IntegrationEvents.Handlers;

public class PaymentGatewayCallbackEventHandler : IIntegrationEventHandler<PaymentGatewayCallbackEvent>
{
    private readonly IMediator _mediator;
    private readonly ILogger<PaymentGatewayCallbackEventHandler> _logger;

    public PaymentGatewayCallbackEventHandler(IMediator mediator, ILogger<PaymentGatewayCallbackEventHandler> logger)
    {
        _mediator = mediator;
        _logger = logger;
    }

    public async Task Handle(PaymentGatewayCallbackEvent @event)
    {
        _logger.LogInformation("Handling PaymentGatewayCallbackEvent for TransactionId: {TransactionId}, NewStatus: {NewStatus}", 
            @event.TransactionId, @event.NewStatus);

        var command = new UpdateTransactionStatusCommand(
            @event.TransactionId,
            @event.NewStatus, // Assuming direct mapping from event status to domain status
            @event.FailureReason,
            @event.PaymentGatewayReferenceId
        );

        try
        {
            var result = await _mediator.Send(command);
            if (result)
            {
                _logger.LogInformation("Successfully processed PaymentGatewayCallbackEvent and updated transaction status for TransactionId: {TransactionId}", @event.TransactionId);
            }
            else
            {
                _logger.LogWarning("Failed to update transaction status based on PaymentGatewayCallbackEvent for TransactionId: {TransactionId}. Transaction might not exist or update failed.", @event.TransactionId);
            }
        }
        catch (System.Exception ex)
        {
            _logger.LogError(ex, "Error processing PaymentGatewayCallbackEvent for TransactionId: {TransactionId}", @event.TransactionId);
            // Depending on the error, you might want to requeue or move to a dead-letter queue (DLQ)
            // For now, we just log and swallow to prevent the consumer host from crashing.
            // Proper DLQ/retry logic would be in the KafkaConsumerService itself or an intermediary.
        }
    }
} 