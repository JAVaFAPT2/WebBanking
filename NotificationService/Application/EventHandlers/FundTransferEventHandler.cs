using System;
using System.Threading;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Logging;
using NotificationService.Application.CQRS.Commands.SendEmail;
using NotificationService.Application.CQRS.Commands.SendSms;
using NotificationService.Application.CQRS.Commands.SendPush;
using NotificationService.Domain.Interfaces;

namespace NotificationService.Application.EventHandlers
{
    // This handler will process events from FundTransferService
    public class FundTransferEventHandler : 
        INotificationHandler<FundTransferInitiatedEvent>,
        INotificationHandler<FundTransferCompletedEvent>,
        INotificationHandler<FundTransferFailedEvent>
    {
        private readonly IMediator _mediator;
        private readonly ILogger<FundTransferEventHandler> _logger;

        public FundTransferEventHandler(
            IMediator mediator,
            ILogger<FundTransferEventHandler> logger)
        {
            _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

        public async Task Handle(FundTransferInitiatedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling FundTransferInitiatedEvent: {TransferId}", notification.TransferId);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.SenderEmail,
                    "Fund Transfer Initiated",
                    $"<p>Your fund transfer of {notification.Amount:C} to account {notification.ReceiverAccountNumber} has been initiated. " +
                    $"Transfer ID: {notification.TransferId}</p>",
                    CorrelationId: notification.TransferId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.SenderPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.SenderPhone,
                        $"Fund transfer of {notification.Amount:C} to account {notification.ReceiverAccountNumber} initiated. Ref: {notification.TransferId}",
                        CorrelationId: notification.TransferId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending FundTransferInitiated notification for {TransferId}", notification.TransferId);
                // Don't rethrow - we don't want to fail the transfer if notification fails
            }
        }

        public async Task Handle(FundTransferCompletedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling FundTransferCompletedEvent: {TransferId}", notification.TransferId);
            
            try
            {
                // Notify sender
                await _mediator.Send(new SendEmailCommand(
                    notification.SenderEmail,
                    "Fund Transfer Completed",
                    $"<p>Your fund transfer of {notification.Amount:C} to account {notification.ReceiverAccountNumber} has been completed successfully. " +
                    $"Transfer ID: {notification.TransferId}</p>",
                    CorrelationId: notification.TransferId.ToString()
                ), cancellationToken);
                
                if (!string.IsNullOrEmpty(notification.SenderPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.SenderPhone,
                        $"Fund transfer of {notification.Amount:C} to account ending in {notification.ReceiverAccountNumber.Substring(Math.Max(0, notification.ReceiverAccountNumber.Length - 4))} completed.",
                        CorrelationId: notification.TransferId.ToString()
                    ), cancellationToken);
                }
                
                // Notify receiver
                if (!string.IsNullOrEmpty(notification.ReceiverEmail))
                {
                    await _mediator.Send(new SendEmailCommand(
                        notification.ReceiverEmail,
                        "Fund Received",
                        $"<p>You have received {notification.Amount:C} from account {notification.SenderAccountNumber}. " +
                        $"Transfer ID: {notification.TransferId}</p>",
                        CorrelationId: notification.TransferId.ToString()
                    ), cancellationToken);
                }
                
                if (!string.IsNullOrEmpty(notification.ReceiverPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.ReceiverPhone,
                        $"You have received {notification.Amount:C} from account ending in {notification.SenderAccountNumber.Substring(Math.Max(0, notification.SenderAccountNumber.Length - 4))}.",
                        CorrelationId: notification.TransferId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending FundTransferCompleted notification for {TransferId}", notification.TransferId);
            }
        }

        public async Task Handle(FundTransferFailedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling FundTransferFailedEvent: {TransferId}", notification.TransferId);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.SenderEmail,
                    "Fund Transfer Failed",
                    $"<p>Your fund transfer of {notification.Amount:C} to account {notification.ReceiverAccountNumber} has failed. " +
                    $"Reason: {notification.FailureReason}</p>",
                    CorrelationId: notification.TransferId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.SenderPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.SenderPhone,
                        $"Fund transfer of {notification.Amount:C} failed: {notification.FailureReason}",
                        CorrelationId: notification.TransferId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending FundTransferFailed notification for {TransferId}", notification.TransferId);
            }
        }
    }

    // Event objects from FundTransferService
    public record FundTransferInitiatedEvent(
        Guid TransferId,
        Guid SenderAccountId,
        string SenderAccountNumber,
        string SenderEmail,
        string SenderPhone,
        Guid ReceiverAccountId,
        string ReceiverAccountNumber,
        decimal Amount) : INotification;

    public record FundTransferCompletedEvent(
        Guid TransferId,
        Guid SenderAccountId,
        string SenderAccountNumber,
        string SenderEmail,
        string SenderPhone,
        Guid ReceiverAccountId,
        string ReceiverAccountNumber,
        string ReceiverEmail,
        string ReceiverPhone,
        decimal Amount) : INotification;

    public record FundTransferFailedEvent(
        Guid TransferId,
        Guid SenderAccountId,
        string SenderAccountNumber,
        string SenderEmail,
        string SenderPhone,
        Guid ReceiverAccountId,
        string ReceiverAccountNumber,
        decimal Amount,
        string FailureReason) : INotification;
} 