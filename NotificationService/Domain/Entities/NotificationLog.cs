using NotificationService.Domain.Enums;
using System;

namespace NotificationService.Domain.Entities;

public class NotificationLog
{
    public Guid Id { get; private set; }
    public string? CorrelationId { get; private set; } // Optional: for tracking a request across services
    public string Recipient { get; private set; } // Email address, phone number, device token, webhook URL
    public NotificationChannel Channel { get; private set; }
    public string? Subject { get; private set; } // Relevant for Email
    public string Body { get; private set; }
    public NotificationStatus Status { get; private set; }
    public DateTime CreatedAt { get; private set; }
    public DateTime? SentAt { get; private set; }
    public DateTime? FailedAt { get; private set; }
    public string? ProviderMessageId { get; private set; } // ID from the external notification provider
    public string? ErrorMessage { get; private set; }

    // Private constructor for EF Core or other ORMs
    private NotificationLog() { }

    public NotificationLog(
        string recipient,
        NotificationChannel channel,
        string body,
        string? subject = null,
        string? correlationId = null)
    {
        Id = Guid.NewGuid();
        Recipient = recipient ?? throw new ArgumentNullException(nameof(recipient));
        Channel = channel;
        Body = body ?? throw new ArgumentNullException(nameof(body));
        Subject = subject;
        CorrelationId = correlationId;
        Status = NotificationStatus.Pending;
        CreatedAt = DateTime.UtcNow;
    }

    public void MarkAsSent(string? providerMessageId = null)
    {
        Status = NotificationStatus.Sent;
        SentAt = DateTime.UtcNow;
        ProviderMessageId = providerMessageId;
        ErrorMessage = null;
    }

    public void MarkAsFailed(string errorMessage)
    {
        Status = NotificationStatus.Failed;
        FailedAt = DateTime.UtcNow;
        ErrorMessage = errorMessage;
    }

    public void UpdateStatus(NotificationStatus newStatus, string? providerMessageId = null, string? details = null)
    {
        Status = newStatus;
        // Potentially update ProviderMessageId or add more detailed logging based on newStatus
        if (providerMessageId != null) ProviderMessageId = providerMessageId;
        // 'details' could be used for more specific error messages or delivery information
    }
} 