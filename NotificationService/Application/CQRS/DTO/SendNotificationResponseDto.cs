using System;

namespace NotificationService.Application.CQRS.DTO;

public class SendNotificationResponseDto
{
    public Guid NotificationLogId { get; set; }
    public bool Success { get; set; }
    public string? Message { get; set; } // e.g., "Email sent successfully" or error message
    public string? ProviderMessageId { get; set; }
} 