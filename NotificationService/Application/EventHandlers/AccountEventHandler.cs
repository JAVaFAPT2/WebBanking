using System;
using System.Threading;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Logging;
using NotificationService.Application.CQRS.Commands.SendEmail;
using NotificationService.Application.CQRS.Commands.SendSms;

namespace NotificationService.Application.EventHandlers
{
    // This handler will process events from AccountService
    public class AccountEventHandler : 
        INotificationHandler<AccountCreatedEvent>,
        INotificationHandler<AccountBlockedEvent>,
        INotificationHandler<AccountClosedEvent>
    {
        private readonly IMediator _mediator;
        private readonly ILogger<AccountEventHandler> _logger;

        public AccountEventHandler(
            IMediator mediator,
            ILogger<AccountEventHandler> logger)
        {
            _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

        public async Task Handle(AccountCreatedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling AccountCreatedEvent: {AccountId}", notification.AccountId);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "Welcome to Your New Account",
                    $"<p>Thank you for opening an account with us.</p>" +
                    $"<p>Account Details:</p>" +
                    $"<ul>" +
                    $"<li>Account Type: {notification.AccountType}</li>" +
                    $"<li>Account Number: {notification.AccountNumber}</li>" +
                    $"<li>Opening Date: {DateTime.UtcNow:d}</li>" +
                    $"</ul>" +
                    $"<p>You can now access your account through our online banking portal or mobile app.</p>" +
                    $"<p>Thank you for choosing our bank.</p>",
                    CorrelationId: notification.AccountId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Your new {notification.AccountType} account has been opened successfully. Account #: {notification.AccountNumber}",
                        CorrelationId: notification.AccountId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending AccountCreated notification for {AccountId}", notification.AccountId);
            }
        }

        public async Task Handle(AccountBlockedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling AccountBlockedEvent: {AccountId}", notification.AccountId);
            
            try
            {
                // Send email notification as high priority
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "IMPORTANT: Your Account Has Been Blocked",
                    $"<p>Your account ({notification.AccountNumber}) has been blocked.</p>" +
                    $"<p>Reason: {notification.Reason}</p>" +
                    $"<p>If you did not request this action, please contact our customer support immediately.</p>",
                    CorrelationId: notification.AccountId.ToString()
                ), cancellationToken);
                
                // Send SMS notification as high priority
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"ALERT: Your account ({notification.AccountNumber}) has been blocked. Reason: {notification.Reason}. Contact support immediately if this was not requested.",
                        CorrelationId: notification.AccountId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending AccountBlocked notification for {AccountId}", notification.AccountId);
            }
        }

        public async Task Handle(AccountClosedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling AccountClosedEvent: {AccountId}", notification.AccountId);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "Account Closure Confirmation",
                    $"<p>Your account ({notification.AccountNumber}) has been closed as requested.</p>" +
                    $"<p>Closure Date: {DateTime.UtcNow:d}</p>" +
                    $"<p>If you have any questions or if this action was not requested by you, please contact our customer support immediately.</p>" +
                    $"<p>Thank you for choosing our bank. We hope to serve you again in the future.</p>",
                    CorrelationId: notification.AccountId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Your account ({notification.AccountNumber}) has been closed as requested. If this was not requested by you, please contact support immediately.",
                        CorrelationId: notification.AccountId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending AccountClosed notification for {AccountId}", notification.AccountId);
            }
        }
    }

    // Event objects from AccountService
    public record AccountCreatedEvent(
        Guid AccountId,
        Guid UserId,
        string UserEmail,
        string UserPhone,
        string AccountType,
        string AccountNumber) : INotification;

    public record AccountBlockedEvent(
        Guid AccountId,
        Guid UserId,
        string UserEmail,
        string UserPhone,
        string AccountNumber,
        string Reason) : INotification;

    public record AccountClosedEvent(
        Guid AccountId,
        Guid UserId,
        string UserEmail,
        string UserPhone,
        string AccountNumber) : INotification;
} 