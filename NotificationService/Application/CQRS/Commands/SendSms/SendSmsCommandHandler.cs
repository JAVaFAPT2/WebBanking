using MediatR;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using NotificationService.Application.CQRS.DTO;
using NotificationService.Domain.Configuration;
using NotificationService.Domain.Entities;
using NotificationService.Domain.Enums;
using NotificationService.Domain.Events;
using NotificationService.Domain.Interfaces;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace NotificationService.Application.CQRS.Commands.SendSms;

public class SendSmsCommandHandler : IRequestHandler<SendSmsCommand, SendNotificationResponseDto>
{
    private readonly ISmsProvider _smsProvider;
    private readonly INotificationLogRepository _notificationLogRepository;
    private readonly IPublisher _mediator;
    private readonly ILogger<SendSmsCommandHandler> _logger;
    private readonly NotificationServiceSettings _settings;

    public SendSmsCommandHandler(
        ISmsProvider smsProvider,
        INotificationLogRepository notificationLogRepository,
        IPublisher mediator,
        ILogger<SendSmsCommandHandler> logger,
        IOptions<NotificationServiceSettings> settings)
    {
        _smsProvider = smsProvider ?? throw new ArgumentNullException(nameof(smsProvider));
        _notificationLogRepository = notificationLogRepository ?? throw new ArgumentNullException(nameof(notificationLogRepository));
        _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        _settings = settings?.Value ?? throw new ArgumentNullException(nameof(settings));
    }

    public async Task<SendNotificationResponseDto> Handle(SendSmsCommand request, CancellationToken cancellationToken)
    {
        var notificationLog = new NotificationLog(
            recipient: request.ToNumber,
            channel: NotificationChannel.Sms,
            body: request.Message,
            correlationId: request.CorrelationId
        );

        try
        {
            await _notificationLogRepository.AddAsync(notificationLog, cancellationToken);
            _logger.LogInformation("Attempting to send SMS to {Recipient}. LogId: {LogId}", request.ToNumber, notificationLog.Id);

            await _smsProvider.SendSmsAsync(
                toNumber: request.ToNumber,
                message: request.Message,
                fromNumber: request.FromNumber ?? _settings.DefaultSmsSenderId,
                cancellationToken: cancellationToken
            );

            notificationLog.MarkAsSent(); // ProviderMessageId could be set here if returned by SendSmsAsync
            await _notificationLogRepository.UpdateAsync(notificationLog, cancellationToken);

            await _mediator.Publish(new NotificationSentEvent(
                notificationLog.Id,
                notificationLog.Recipient,
                notificationLog.Channel,
                notificationLog.Subject, // SMS typically doesn't have a subject
                notificationLog.SentAt!.Value,
                notificationLog.ProviderMessageId,
                notificationLog.CorrelationId
            ), cancellationToken);

            _logger.LogInformation("SMS sent successfully to {Recipient}. LogId: {LogId}", request.ToNumber, notificationLog.Id);
            return new SendNotificationResponseDto
            {
                NotificationLogId = notificationLog.Id,
                Success = true,
                Message = "SMS sent successfully.",
                ProviderMessageId = notificationLog.ProviderMessageId
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to send SMS to {Recipient}. LogId: {LogId}. Error: {ErrorMessage}", request.ToNumber, notificationLog.Id, ex.Message);
            notificationLog.MarkAsFailed(ex.Message);
            await _notificationLogRepository.UpdateAsync(notificationLog, cancellationToken).ConfigureAwait(false);

            await _mediator.Publish(new NotificationFailedEvent(
                notificationLog.Id,
                notificationLog.Recipient,
                notificationLog.Channel,
                notificationLog.Subject,
                ex.Message,
                notificationLog.FailedAt!.Value,
                notificationLog.CorrelationId
            ), cancellationToken).ConfigureAwait(false);

            return new SendNotificationResponseDto
            {
                NotificationLogId = notificationLog.Id,
                Success = false,
                Message = $"Failed to send SMS: {ex.Message}"
            };
        }
    }
} 