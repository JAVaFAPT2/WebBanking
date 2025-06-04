using MediatR;
using NotificationService.Application.CQRS.DTO;
using System.Collections.Generic;

namespace NotificationService.Application.CQRS.Commands.SendPush;

public record SendPushNotificationCommand(
    // Send to a single token or a list of tokens. One of these should be populated.
    string? DeviceToken, 
    IEnumerable<string>? DeviceTokens,
    string Title,
    string Body,
    IDictionary<string, string>? DataPayload = null, // Optional custom data
    string? CorrelationId = null 
) : IRequest<SendNotificationResponseDto>;

// Note: The handler will need to decide whether to call 
// IPushProvider.SendPushNotificationAsync or SendMulticastPushNotificationAsync
// based on whether DeviceToken or DeviceTokens is populated.
// Alternatively, create two separate commands (e.g., SendSinglePushCommand, SendMulticastPushCommand)
// For simplicity here, one command is used, and the handler will have branching logic. 