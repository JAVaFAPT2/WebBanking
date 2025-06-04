namespace OrchestrationService.Application.Models.Events;

/// <summary>
/// Event indicating a notification was successfully sent
/// </summary>
/// <param name="TransactionId">Unique transaction identifier</param>
/// <param name="SagaId">ID of the orchestrating saga</param>
/// <param name="NotificationId">Unique ID of the sent notification</param>
/// <param name="UserId">User who received the notification</param>
/// <param name="SentAt">When the notification was sent</param>
public record NotificationSentEvent(
    string TransactionId,
    string SagaId,
    string NotificationId,
    string UserId,
    string SentAt);

/// <summary>
/// Event indicating a notification failed to send
/// </summary>
/// <param name="TransactionId">Unique transaction identifier</param>
/// <param name="SagaId">ID of the orchestrating saga</param>
/// <param name="UserId">User who should have received the notification</param>
/// <param name="Reason">Reason for the failure</param>
public record NotificationFailedEvent(
    string TransactionId,
    string SagaId,
    string UserId,
    string Reason); 