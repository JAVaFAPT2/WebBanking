using MediatR;
using NotificationService.Application.CQRS.DTO;
using NotificationService.Domain.Configuration;
using NotificationService.Domain.Entities;
using NotificationService.Domain.Enums;
using NotificationService.Domain.Events;
using NotificationService.Domain.Interfaces;
using System;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace NotificationService.Application.CQRS.Commands.SendPush;

public class SendPushNotificationCommandHandler : IRequestHandler<SendPushNotificationCommand, SendNotificationResponseDto>
{
    private readonly IPushProvider _pushProvider;
    private readonly INotificationLogRepository _notificationLogRepository;
    private readonly IPublisher _mediator;
    private readonly ILogger<SendPushNotificationCommandHandler> _logger;
    private readonly NotificationServiceSettings _settings; // Though not directly used in this basic version

    public SendPushNotificationCommandHandler(
        IPushProvider pushProvider,
        INotificationLogRepository notificationLogRepository,
        IPublisher mediator,
        ILogger<SendPushNotificationCommandHandler> logger,
        IOptions<NotificationServiceSettings> settings)
    {
        _pushProvider = pushProvider ?? throw new ArgumentNullException(nameof(pushProvider));
        _notificationLogRepository = notificationLogRepository ?? throw new ArgumentNullException(nameof(notificationLogRepository));
        _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        _settings = settings?.Value ?? throw new ArgumentNullException(nameof(settings));
    }

    public async Task<SendNotificationResponseDto> Handle(SendPushNotificationCommand request, CancellationToken cancellationToken)
    {
        bool isMulticast = request.DeviceTokens != null && request.DeviceTokens.Any();
        string recipientSummary = isMulticast 
            ? $"Multicast to {request.DeviceTokens!.Count()} devices" 
            : request.DeviceToken!;

        var notificationLog = new NotificationLog(
            recipient: recipientSummary, // For multicast, this is a summary.
            channel: NotificationChannel.Push,
            body: request.Body, // Title is part of the push content, not separate in the basic log body
            subject: request.Title, // Using Subject for Title in push notifications
            correlationId: request.CorrelationId
        );

        try
        {
            await _notificationLogRepository.AddAsync(notificationLog, cancellationToken);
            _logger.LogInformation("Attempting to send push notification. Title: {Title}, To: {RecipientSummary}. LogId: {LogId}", 
                request.Title, recipientSummary, notificationLog.Id);

            if (isMulticast)
            {
                await _pushProvider.SendMulticastPushNotificationAsync(
                    deviceTokens: request.DeviceTokens!,
                    title: request.Title,
                    body: request.Body,
                    data: request.DataPayload,
                    cancellationToken: cancellationToken
                );
            }
            else
            {
                await _pushProvider.SendPushNotificationAsync(
                    deviceToken: request.DeviceToken!,
                    title: request.Title,
                    body: request.Body,
                    data: request.DataPayload,
                    cancellationToken: cancellationToken
                );
            }

            notificationLog.MarkAsSent(); // ProviderMessageId for push might be a batch ID or not applicable here
            await _notificationLogRepository.UpdateAsync(notificationLog, cancellationToken);

            await _mediator.Publish(new NotificationSentEvent(
                notificationLog.Id,
                notificationLog.Recipient,
                notificationLog.Channel,
                notificationLog.Subject, // Title
                notificationLog.SentAt!.Value,
                notificationLog.ProviderMessageId,
                notificationLog.CorrelationId
            ), cancellationToken);

            _logger.LogInformation("Push notification sent successfully. Title: {Title}, To: {RecipientSummary}. LogId: {LogId}", 
                request.Title, recipientSummary, notificationLog.Id);
            return new SendNotificationResponseDto
            {
                NotificationLogId = notificationLog.Id,
                Success = true,
                Message = "Push notification sent successfully.",
                ProviderMessageId = notificationLog.ProviderMessageId
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to send push notification. Title: {Title}, To: {RecipientSummary}. LogId: {LogId}. Error: {ErrorMessage}", 
                request.Title, recipientSummary, notificationLog.Id, ex.Message);
            notificationLog.MarkAsFailed(ex.Message);
            await _notificationLogRepository.UpdateAsync(notificationLog, cancellationToken).ConfigureAwait(false);

            await _mediator.Publish(new NotificationFailedEvent(
                notificationLog.Id,
                notificationLog.Recipient,
                notificationLog.Channel,
                notificationLog.Subject, // Title
                ex.Message,
                notificationLog.FailedAt!.Value,
                notificationLog.CorrelationId
            ), cancellationToken).ConfigureAwait(false);

            return new SendNotificationResponseDto
            {
                NotificationLogId = notificationLog.Id,
                Success = false,
                Message = $"Failed to send push notification: {ex.Message}"
            };
        }
    }
} 