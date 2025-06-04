namespace OrchestrationService.Application.Models.Commands;

/// <summary>
/// Types of notifications that can be sent
/// </summary>
public enum NotificationType
{
    /// <summary>
    /// Email notification
    /// </summary>
    Email,
    
    /// <summary>
    /// SMS text message
    /// </summary>
    Sms,
    
    /// <summary>
    /// Push notification to mobile device
    /// </summary>
    Push,
    
    /// <summary>
    /// In-app notification
    /// </summary>
    InApp
}

/// <summary>
/// Command to send a notification
/// </summary>
/// <param name="TransactionId">Unique transaction identifier</param>
/// <param name="SagaId">ID of the orchestrating saga</param>
/// <param name="UserId">User to notify</param>
/// <param name="Type">Type of notification</param>
/// <param name="Subject">Notification subject</param>
/// <param name="Content">Notification content</param>
public record SendNotificationCommand(
    string TransactionId,
    string SagaId,
    string UserId,
    NotificationType Type,
    string Subject,
    string Content); 