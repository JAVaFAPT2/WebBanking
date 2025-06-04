namespace NotificationService.Domain.Enums;

public enum NotificationStatus
{
    Pending,
    Sent,
    Failed,
    Delivered, // If provider supports delivery receipts
    Read // If provider supports read receipts
} 