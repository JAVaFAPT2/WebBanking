using System;
using System.Threading;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Logging;
using NotificationService.Application.CQRS.Commands.SendEmail;
using NotificationService.Application.CQRS.Commands.SendSms;

namespace NotificationService.Application.EventHandlers
{
    // This handler will process events from LoanService
    public class LoanEventHandler : 
        INotificationHandler<LoanCreatedEvent>,
        INotificationHandler<LoanApprovedEvent>,
        INotificationHandler<LoanRejectedEvent>,
        INotificationHandler<LoanPaymentMadeEvent>,
        INotificationHandler<LoanPaidOffEvent>
    {
        private readonly IMediator _mediator;
        private readonly ILogger<LoanEventHandler> _logger;

        public LoanEventHandler(
            IMediator mediator,
            ILogger<LoanEventHandler> logger)
        {
            _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

        public async Task Handle(LoanCreatedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling LoanCreatedEvent: {LoanId}", notification.LoanId);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "Loan Application Received",
                    $"<p>We have received your loan application for {notification.Amount:C}.</p>" +
                    $"<p>Loan Details:</p>" +
                    $"<ul>" +
                    $"<li>Loan Type: {notification.LoanType}</li>" +
                    $"<li>Amount: {notification.Amount:C}</li>" +
                    $"<li>Term: {notification.TermMonths} months</li>" +
                    $"<li>Interest Rate: {notification.InterestRate:P2}</li>" +
                    $"</ul>" +
                    $"<p>Your application is currently under review. We will notify you once a decision has been made.</p>",
                    CorrelationId: notification.LoanId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Your loan application for {notification.Amount:C} has been received and is under review. Ref: {notification.LoanId}",
                        CorrelationId: notification.LoanId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending LoanCreated notification for {LoanId}", notification.LoanId);
            }
        }

        public async Task Handle(LoanApprovedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling LoanApprovedEvent: {LoanId}", notification.LoanId);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "Loan Approved",
                    $"<p>Congratulations! Your loan application for {notification.Amount:C} has been approved.</p>" +
                    $"<p>Loan Details:</p>" +
                    $"<ul>" +
                    $"<li>Loan ID: {notification.LoanId}</li>" +
                    $"<li>Amount: {notification.Amount:C}</li>" +
                    $"<li>Term: {notification.TermMonths} months</li>" +
                    $"<li>Interest Rate: {notification.InterestRate:P2}</li>" +
                    $"<li>Monthly Payment: {notification.MonthlyPayment:C}</li>" +
                    $"<li>Start Date: {notification.StartDate:d}</li>" +
                    $"<li>End Date: {notification.EndDate:d}</li>" +
                    $"</ul>" +
                    $"<p>The funds will be disbursed to your account within 1-2 business days.</p>",
                    CorrelationId: notification.LoanId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Your loan application for {notification.Amount:C} has been approved. Funds will be disbursed within 1-2 business days.",
                        CorrelationId: notification.LoanId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending LoanApproved notification for {LoanId}", notification.LoanId);
            }
        }

        public async Task Handle(LoanRejectedEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling LoanRejectedEvent: {LoanId}", notification.LoanId);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "Loan Application Status",
                    $"<p>We regret to inform you that your loan application for {notification.Amount:C} could not be approved at this time.</p>" +
                    $"<p>Reason: {notification.RejectionReason}</p>" +
                    $"<p>If you have any questions or would like to discuss alternative options, please contact our customer support.</p>",
                    CorrelationId: notification.LoanId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Your loan application for {notification.Amount:C} could not be approved at this time. Please check your email for details.",
                        CorrelationId: notification.LoanId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending LoanRejected notification for {LoanId}", notification.LoanId);
            }
        }

        public async Task Handle(LoanPaymentMadeEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling LoanPaymentMadeEvent: {LoanId}, {Amount}", notification.LoanId, notification.PaymentAmount);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "Loan Payment Confirmation",
                    $"<p>We have received your payment of {notification.PaymentAmount:C} for your loan.</p>" +
                    $"<p>Loan Details:</p>" +
                    $"<ul>" +
                    $"<li>Loan ID: {notification.LoanId}</li>" +
                    $"<li>Payment Date: {notification.PaymentDate:d}</li>" +
                    $"<li>Payment Amount: {notification.PaymentAmount:C}</li>" +
                    $"<li>Remaining Balance: {notification.RemainingAmount:C}</li>" +
                    $"</ul>" +
                    $"<p>Thank you for your payment.</p>",
                    CorrelationId: notification.LoanId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Payment of {notification.PaymentAmount:C} received for your loan. Remaining balance: {notification.RemainingAmount:C}",
                        CorrelationId: notification.LoanId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending LoanPaymentMade notification for {LoanId}", notification.LoanId);
            }
        }

        public async Task Handle(LoanPaidOffEvent notification, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Handling LoanPaidOffEvent: {LoanId}", notification.LoanId);
            
            try
            {
                // Send email notification
                await _mediator.Send(new SendEmailCommand(
                    notification.UserEmail,
                    "Congratulations! Your Loan is Paid Off",
                    $"<p>Congratulations! You have successfully paid off your loan.</p>" +
                    $"<p>Loan Details:</p>" +
                    $"<ul>" +
                    $"<li>Loan ID: {notification.LoanId}</li>" +
                    $"<li>Original Amount: {notification.OriginalAmount:C}</li>" +
                    $"<li>Paid Off Date: {notification.PaidOffDate:d}</li>" +
                    $"</ul>" +
                    $"<p>Thank you for choosing our bank for your financial needs. We look forward to serving you again in the future.</p>",
                    CorrelationId: notification.LoanId.ToString()
                ), cancellationToken);
                
                // Send SMS notification if phone number is available
                if (!string.IsNullOrEmpty(notification.UserPhone))
                {
                    await _mediator.Send(new SendSmsCommand(
                        notification.UserPhone,
                        $"Congratulations! Your loan of {notification.OriginalAmount:C} has been fully paid off.",
                        CorrelationId: notification.LoanId.ToString()
                    ), cancellationToken);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending LoanPaidOff notification for {LoanId}", notification.LoanId);
            }
        }
    }

    // Event objects from LoanService
    public record LoanCreatedEvent(
        Guid LoanId,
        Guid AccountId,
        string UserEmail,
        string UserPhone,
        string LoanType,
        decimal Amount,
        decimal InterestRate,
        int TermMonths) : INotification;

    public record LoanApprovedEvent(
        Guid LoanId,
        Guid AccountId,
        string UserEmail,
        string UserPhone,
        decimal Amount,
        decimal InterestRate,
        int TermMonths,
        decimal MonthlyPayment,
        DateTime StartDate,
        DateTime EndDate) : INotification;

    public record LoanRejectedEvent(
        Guid LoanId,
        Guid AccountId,
        string UserEmail,
        string UserPhone,
        decimal Amount,
        string RejectionReason) : INotification;

    public record LoanPaymentMadeEvent(
        Guid LoanId,
        Guid AccountId,
        string UserEmail,
        string UserPhone,
        decimal PaymentAmount,
        decimal RemainingAmount,
        DateTime PaymentDate) : INotification;

    public record LoanPaidOffEvent(
        Guid LoanId,
        Guid AccountId,
        string UserEmail,
        string UserPhone,
        decimal OriginalAmount,
        DateTime PaidOffDate) : INotification;
} 