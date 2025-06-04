using System;
using System.Threading;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Logging;
using NotificationService.Application.CQRS.Commands.SendEmail;
using NotificationService.Application.CQRS.Commands.SendSms;
using NotificationService.Domain.Interfaces;

namespace NotificationService.Application.EventHandlers
{
    // This handler will process events from TransactionService
    public class TransactionEventHandler : 
        INotificationHandler<TransactionCreatedEvent>,
        INotificationHandler<TransactionCompletedEvent>
    {
        private readonly IMediator _mediator;
        private readonly ILogger<TransactionEventHandler> _logger;

        public TransactionEventHandler(
            IMediator mediator,
            ILogger<TransactionEventHandler> logger)
        {
            _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

        public async Task Handle(TransactionCreatedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling TransactionCreatedEvent: {TransactionId}", notification.TransactionId);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "Transaction Initiated",
                    $"<p>Your transaction of {notification.Amount:C} has been initiated. Transaction ID: {notification.TransactionId}</p>",
                    CorrelationId: notification.TransactionId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Transaction of {notification.Amount:C} initiated. Ref: {notification.TransactionId}",
                        CorrelationId: notification.TransactionId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending TransactionCreated notification for {TransactionId}", notification.TransactionId);
                // Don't rethrow - we don't want to fail the transaction if notification fails
            }
        }

        public async Task Handle(TransactionCompletedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling TransactionCompletedEvent: {TransactionId}", notification.TransactionId);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "Transaction Completed",
                    $"<p>Your transaction of {notification.Amount:C} has been completed successfully. Transaction ID: {notification.TransactionId}</p>",
                    CorrelationId: notification.TransactionId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Transaction of {notification.Amount:C} completed successfully. Ref: {notification.TransactionId}",
                        CorrelationId: notification.TransactionId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending TransactionCompleted notification for {TransactionId}", notification.TransactionId);
                // Don't rethrow - we don't want transaction completion to fail if notification fails
            }
        }
    }

    // Event objects from TransactionService
    public record TransactionCreatedEvent(
        Guid TransactionId,
        Guid AccountId,
        decimal Amount,
        string UserEmail,
        string UserPhone) : INotification;

    public record TransactionCompletedEvent(
        Guid TransactionId,
        Guid AccountId,
        decimal Amount,
        string UserEmail,
        string UserPhone) : INotification;
} 