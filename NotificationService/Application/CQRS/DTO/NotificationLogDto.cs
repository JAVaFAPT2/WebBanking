using NotificationService.Domain.Enums;
using System;

namespace NotificationService.Application.CQRS.DTO;

public class NotificationLogDto
{
    public Guid Id { get; set; }
    public string? CorrelationId { get; set; }
    public string Recipient { get; set; }
    public NotificationChannel Channel { get; set; }
    public string? Subject { get; set; }
    public string Body { get; set; }
    public NotificationStatus Status { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime? SentAt { get; set; }
    public DateTime? FailedAt { get; set; }
    public string? ProviderMessageId { get; set; }
    public string? ErrorMessage { get; set; }
} 