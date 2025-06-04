using MediatR;
using NotificationService.Application.CQRS.DTO;
using NotificationService.Domain.Configuration;
using NotificationService.Domain.Entities;
using NotificationService.Domain.Enums;
using NotificationService.Domain.Events;
using NotificationService.Domain.Interfaces;
using System;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace NotificationService.Application.CQRS.Commands.SendEmail;

public class SendEmailCommandHandler : IRequestHandler<SendEmailCommand, SendNotificationResponseDto>
{
    private readonly IEmailProvider _emailProvider;
    private readonly INotificationLogRepository _notificationLogRepository;
    private readonly IPublisher _mediator;
    private readonly ILogger<SendEmailCommandHandler> _logger;
    private readonly NotificationServiceSettings _settings;

    public SendEmailCommandHandler(
        IEmailProvider emailProvider,
        INotificationLogRepository notificationLogRepository,
        IPublisher mediator, // Use IPublisher for publishing events
        ILogger<SendEmailCommandHandler> logger,
        IOptions<NotificationServiceSettings> settings)
    {
        _emailProvider = emailProvider ?? throw new ArgumentNullException(nameof(emailProvider));
        _notificationLogRepository = notificationLogRepository ?? throw new ArgumentNullException(nameof(notificationLogRepository));
        _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        _settings = settings?.Value ?? throw new ArgumentNullException(nameof(settings));
    }

    public async Task<SendNotificationResponseDto> Handle(SendEmailCommand request, CancellationToken cancellationToken)
    {
        var notificationLog = new NotificationLog(
            recipient: request.ToEmail,
            channel: NotificationChannel.Email,
            body: request.HtmlBody,
            subject: request.Subject,
            correlationId: request.CorrelationId
        );

        try
        {
            await _notificationLogRepository.AddAsync(notificationLog, cancellationToken);
            _logger.LogInformation("Attempting to send email to {Recipient} with Subject: {Subject}. LogId: {LogId}", request.ToEmail, request.Subject, notificationLog.Id);

            await _emailProvider.SendEmailAsync(
                toEmail: request.ToEmail,
                subject: request.Subject,
                htmlBody: request.HtmlBody,
                fromEmail: request.FromEmail ?? _settings.DefaultFromEmail,
                fromName: request.FromName ?? _settings.DefaultFromName,
                cancellationToken: cancellationToken
            );

            notificationLog.MarkAsSent(); // Assuming ProviderMessageId might come from a more complex provider response, not shown here
            await _notificationLogRepository.UpdateAsync(notificationLog, cancellationToken);

            await _mediator.Publish(new NotificationSentEvent(
                notificationLog.Id,
                notificationLog.Recipient,
                notificationLog.Channel,
                notificationLog.Subject,
                notificationLog.SentAt!.Value, // SentAt is non-null after MarkAsSent
                notificationLog.ProviderMessageId,
                notificationLog.CorrelationId
            ), cancellationToken);
            
            _logger.LogInformation("Email sent successfully to {Recipient}. LogId: {LogId}", request.ToEmail, notificationLog.Id);
            return new SendNotificationResponseDto 
            { 
                NotificationLogId = notificationLog.Id, 
                Success = true, 
                Message = "Email sent successfully.",
                ProviderMessageId = notificationLog.ProviderMessageId
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to send email to {Recipient}. LogId: {LogId}. Error: {ErrorMessage}", request.ToEmail, notificationLog.Id, ex.Message);
            notificationLog.MarkAsFailed(ex.Message);
            await _notificationLogRepository.UpdateAsync(notificationLog, cancellationToken).ConfigureAwait(false); //ConfigureAwait(false) in catch if not UI thread.

            await _mediator.Publish(new NotificationFailedEvent(
                notificationLog.Id,
                notificationLog.Recipient,
                notificationLog.Channel,
                notificationLog.Subject,
                ex.Message,
                notificationLog.FailedAt!.Value, // FailedAt is non-null after MarkAsFailed
                notificationLog.CorrelationId
            ), cancellationToken).ConfigureAwait(false);
            
            return new SendNotificationResponseDto 
            { 
                NotificationLogId = notificationLog.Id, 
                Success = false, 
                Message = $"Failed to send email: {ex.Message}"
            };
        }
    }
} 