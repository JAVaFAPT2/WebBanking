using MediatR;
using NotificationService.Domain.Enums;
using System;

namespace NotificationService.Domain.Events;

public record NotificationFailedEvent(
    Guid NotificationLogId,
    string Recipient,
    NotificationChannel Channel,
    string? Subject,
    string ErrorMessage,
    DateTime FailedAt,
    string? CorrelationId
) : INotification; 