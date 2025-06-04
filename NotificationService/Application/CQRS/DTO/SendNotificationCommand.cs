using System;
using System.Collections.Generic;
using MediatR;

namespace NotificationService.Application.CQRS.DTO
{
    public record SendNotificationCommand(
        string SagaId,
        string TransactionId, // ID of the overall transaction
        string UserId, // The user to notify
        string RecipientAddress, // e.g., email address, phone number, device token
        string MessageType, // e.g., "EMAIL", "SMS", "PUSH"
        string Subject, // Title for email/push
        string Body, // Content of the notification
        Dictionary<string, string> TemplateModel // For personalized notifications
    ) : IRequest<NotificationSentReply>;
} 