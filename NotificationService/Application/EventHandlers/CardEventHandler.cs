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
    // This handler will process events from CardService
    public class CardEventHandler : 
        INotificationHandler<CardIssuedEvent>,
        INotificationHandler<CardActivatedEvent>,
        INotificationHandler<CardBlockedEvent>,
        INotificationHandler<CardTransactionAuthorizedEvent>,
        INotificationHandler<CardTransactionDeclinedEvent>
    {
        private readonly IMediator _mediator;
        private readonly ILogger<CardEventHandler> _logger;

        public CardEventHandler(
            IMediator mediator,
            ILogger<CardEventHandler> logger)
        {
            _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

        public async Task Handle(CardIssuedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling CardIssuedEvent: {CardId}", notification.CardId);
            
            try
            {
                // Mask all but last 4 digits of card number
                string maskedCardNumber = $"XXXX-XXXX-XXXX-{notification.CardNumber.Substring(notification.CardNumber.Length - 4)}";
                
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "Your New Card Has Been Issued",
                    $"<p>Your new {notification.CardType} card (ending in {notification.CardNumber.Substring(notification.CardNumber.Length - 4)}) " +
                    $"has been issued and will be delivered to your registered address shortly.</p>" +
                    $"<p>Card Details:</p>" +
                    $"<ul>" +
                    $"<li>Card Type: {notification.CardType}</li>" +
                    $"<li>Card Number: {maskedCardNumber}</li>" +
                    $"<li>Expiry Date: {notification.ExpiryDate:MM/yy}</li>" +
                    $"</ul>" +
                    $"<p>Please activate your card as soon as you receive it.</p>",
                    CorrelationId: notification.CardId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Your new {notification.CardType} card (ending in {notification.CardNumber.Substring(notification.CardNumber.Length - 4)}) has been issued. Activate upon receipt.",
                        CorrelationId: notification.CardId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending CardIssued notification for {CardId}", notification.CardId);
            }
        }

        public async Task Handle(CardActivatedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling CardActivatedEvent: {CardId}", notification.CardId);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "Card Successfully Activated",
                    $"<p>Your card ending in {notification.CardNumber.Substring(notification.CardNumber.Length - 4)} has been successfully activated.</p>" +
                    $"<p>You can now use your card for transactions.</p>",
                    CorrelationId: notification.CardId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Your card ending in {notification.CardNumber.Substring(notification.CardNumber.Length - 4)} has been activated successfully.",
                        CorrelationId: notification.CardId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending CardActivated notification for {CardId}", notification.CardId);
            }
        }

        public async Task Handle(CardBlockedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling CardBlockedEvent: {CardId}", notification.CardId);
            
            try
            {
                // Send email notification as high priority
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "ALERT: Your Card Has Been Blocked",
                    $"<p>Your card ending in {notification.CardNumber.Substring(notification.CardNumber.Length - 4)} has been blocked.</p>" +
                    $"<p>Reason: {notification.Reason}</p>" +
                    $"<p>If you did not request this action, please contact our customer support immediately.</p>",
                    CorrelationId: notification.CardId.ToString()
                ), cancellationToken);
                
                // Send SMS notification as high priority
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"ALERT: Your card ending in {notification.CardNumber.Substring(notification.CardNumber.Length - 4)} has been blocked. Reason: {notification.Reason}",
                        CorrelationId: notification.CardId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending CardBlocked notification for {CardId}", notification.CardId);
            }
        }

        public async Task Handle(CardTransactionAuthorizedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling CardTransactionAuthorizedEvent: {CardId}, {MerchantName}, {Amount}", 
                notification.CardId, notification.MerchantName, notification.Amount);
            
            try
            {
                // For high-value transactions, send both email and SMS
                bool isHighValueTransaction = notification.Amount >= 1000;
                
                // Send email for all transactions
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    $"Transaction of {notification.Amount:C} at {notification.MerchantName}",
                    $"<p>A transaction of {notification.Amount:C} at {notification.MerchantName} was authorized on your card ending in " +
                    $"{notification.CardNumber.Substring(notification.CardNumber.Length - 4)} on {notification.TransactionDate:g}.</p>" +
                    $"<p>If you don't recognize this transaction, please contact us immediately.</p>",
                    CorrelationId: notification.CardId.ToString()
                ), cancellationToken);
                
                // Send SMS only for high-value transactions
                if (isHighValueTransaction && !string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Transaction of {notification.Amount:C} at {notification.MerchantName} was authorized on your card ending in {notification.CardNumber.Substring(notification.CardNumber.Length - 4)}",
                        CorrelationId: notification.CardId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending CardTransactionAuthorized notification for {CardId}", notification.CardId);
            }
        }

        public async Task Handle(CardTransactionDeclinedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling CardTransactionDeclinedEvent: {CardId}, {MerchantName}, {Amount}", 
                notification.CardId, notification.MerchantName, notification.Amount);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    $"Transaction Declined at {notification.MerchantName}",
                    $"<p>A transaction of {notification.Amount:C} at {notification.MerchantName} was declined on your card ending in " +
                    $"{notification.CardNumber.Substring(notification.CardNumber.Length - 4)} on {notification.TransactionDate:g}.</p>" +
                    $"<p>Reason: {notification.DeclineReason}</p>" +
                    $"<p>If you need assistance, please contact our customer support.</p>",
                    CorrelationId: notification.CardId.ToString()
                ), cancellationToken);
                
                // SMS notification for declined transactions
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Transaction of {notification.Amount:C} at {notification.MerchantName} was declined. Reason: {notification.DeclineReason}",
                        CorrelationId: notification.CardId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending CardTransactionDeclined notification for {CardId}", notification.CardId);
            }
        }
    }

    // Event objects from CardService
    public record CardIssuedEvent(
        Guid CardId,
        string CardNumber,
        Guid AccountId,
        string UserEmail,
        string UserPhone,
        string CardholderName,
        string CardType,
        DateTime ExpiryDate) : INotification;

    public record CardActivatedEvent(
        Guid CardId,
        string CardNumber,
        Guid AccountId,
        string UserEmail,
        string UserPhone,
        DateTime ActivationDate) : INotification;

    public record CardBlockedEvent(
        Guid CardId,
        string CardNumber,
        Guid AccountId,
        string UserEmail,
        string UserPhone,
        string Reason,
        DateTime BlockedDate) : INotification;

    public record CardTransactionAuthorizedEvent(
        Guid CardId,
        string CardNumber,
        Guid AccountId,
        string UserEmail,
        string UserPhone,
        string MerchantName,
        decimal Amount,
        decimal AvailableBalance,
        DateTime TransactionDate) : INotification;

    public record CardTransactionDeclinedEvent(
        Guid CardId,
        string CardNumber,
        Guid AccountId,
        string UserEmail,
        string UserPhone,
        string MerchantName,
        decimal Amount,
        string DeclineReason,
        DateTime TransactionDate) : INotification;
}