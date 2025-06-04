using MediatR;
using Microsoft.Extensions.Logging;
using NotificationService.Application.CQRS.DTO;
using NotificationService.Domain.Interfaces;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace NotificationService.Application.CQRS.Queries.GetNotificationLogById;

public class GetNotificationLogByIdQueryHandler : IRequestHandler<GetNotificationLogByIdQuery, NotificationLogDto?>
{
    private readonly INotificationLogRepository _notificationLogRepository;
    private readonly ILogger<GetNotificationLogByIdQueryHandler> _logger;

    public GetNotificationLogByIdQueryHandler(
        INotificationLogRepository notificationLogRepository, 
        ILogger<GetNotificationLogByIdQueryHandler> logger)
    {
        _notificationLogRepository = notificationLogRepository ?? throw new ArgumentNullException(nameof(notificationLogRepository));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    public async Task<NotificationLogDto?> Handle(GetNotificationLogByIdQuery request, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Fetching notification log by Id: {NotificationLogId}", request.NotificationLogId);
        var notificationLog = await _notificationLogRepository.GetByIdAsync(request.NotificationLogId, cancellationToken);

        if (notificationLog == null)
        {
            _logger.LogWarning("Notification log with Id: {NotificationLogId} not found.", request.NotificationLogId);
            return null;
        }

        // Manual mapping. Consider AutoMapper for more complex scenarios.
        return new NotificationLogDto
        {
            Id = notificationLog.Id,
            CorrelationId = notificationLog.CorrelationId,
            Recipient = notificationLog.Recipient,
            Channel = notificationLog.Channel,
            Subject = notificationLog.Subject,
            Body = notificationLog.Body,
            Status = notificationLog.Status,
            CreatedAt = notificationLog.CreatedAt,
            SentAt = notificationLog.SentAt,
            FailedAt = notificationLog.FailedAt,
            ProviderMessageId = notificationLog.ProviderMessageId,
            ErrorMessage = notificationLog.ErrorMessage
        };
    }
} 