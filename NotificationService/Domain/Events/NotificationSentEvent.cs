using MediatR;
using NotificationService.Domain.Enums;
using System;

namespace NotificationService.Domain.Events;

public record NotificationSentEvent(
    Guid NotificationLogId,
    string Recipient,
    NotificationChannel Channel,
    string? Subject,
    DateTime SentAt,
    string? ProviderMessageId,
    string? CorrelationId
) : INotification; 