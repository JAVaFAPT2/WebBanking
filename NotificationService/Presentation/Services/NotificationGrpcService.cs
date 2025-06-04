using Grpc.Core;
using MediatR;
using Microsoft.Extensions.Logging;
using NotificationService.Application.CQRS.Commands.SendEmail;
using NotificationService.Application.CQRS.Commands.SendPush;
using NotificationService.Application.CQRS.Commands.SendSms;
using NotificationService.Application.CQRS.DTO;
using NotificationService.Presentation.Protos;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using FluentValidation;

namespace NotificationService.Presentation.Services;

public class NotificationGrpcService : Notification.NotificationBase
{
    private readonly IMediator _mediator;
    private readonly ILogger<NotificationGrpcService> _logger;

    public NotificationGrpcService(IMediator mediator, ILogger<NotificationGrpcService> logger)
    {
        _mediator = mediator ?? throw new ArgumentNullException(nameof(mediator));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    public override async Task<SendNotificationResponse> SendNotification(SendNotificationRequest request, ServerCallContext context)
    {
        _logger.LogInformation("gRPC SendNotification request received for Recipient: {Recipient}, Type: {Type}", request.Recipient, request.Type);

        SendNotificationResponseDto? commandResponse = null;
        string? correlationId = context.RequestHeaders.FirstOrDefault(h => h.Key == "x-correlation-id")?.Value;

        try
        {
            switch (request.Type)
            {
                case Protos.NotificationType.Email:
                    var emailCommand = new SendEmailCommand(
                        ToEmail: request.Recipient,
                        Subject: request.Subject,
                        HtmlBody: request.Body,
                        CorrelationId: correlationId 
                        // FromEmail and FromName can be added if provided in proto or use defaults in handler
                    );
                    commandResponse = await _mediator.Send(emailCommand, context.CancellationToken);
                    break;

                case Protos.NotificationType.Sms:
                    var smsCommand = new SendSmsCommand(
                        ToNumber: request.Recipient,
                        Message: request.Body,
                        CorrelationId: correlationId
                        // FromNumber can be added if provided in proto or use defaults in handler
                    );
                    commandResponse = await _mediator.Send(smsCommand, context.CancellationToken);
                    break;

                case Protos.NotificationType.Push:
                    // Assuming recipient is a single device token for this simplified mapping
                    // For more complex scenarios (topics, multiple tokens from one gRPC request), this would need adjustment.
                    var pushCommand = new SendPushNotificationCommand(
                        DeviceToken: request.Recipient, 
                        DeviceTokens: null, // Or map from a repeated field if proto supports it
                        Title: request.Subject, // Using Subject as Title for Push
                        Body: request.Body,
                        DataPayload: request.DataPayload.ToDictionary(kvp => kvp.Key, kvp => kvp.Value), // Handle DataPayload
                        CorrelationId: correlationId
                    );
                    commandResponse = await _mediator.Send(pushCommand, context.CancellationToken);
                    break;
                
                default:
                    _logger.LogWarning("Unsupported notification type: {NotificationType}", request.Type);
                    throw new RpcException(new Status(StatusCode.InvalidArgument, $"Unsupported notification type: {request.Type}"));
            }

            if (commandResponse == null)
            {
                _logger.LogError("Command response was null for Recipient: {Recipient}, Type: {Type}", request.Recipient, request.Type);
                throw new RpcException(new Status(StatusCode.Internal, "Failed to process notification command."));
            }

            return new SendNotificationResponse
            {
                Success = commandResponse.Success,
                MessageId = commandResponse.ProviderMessageId ?? commandResponse.NotificationLogId.ToString(),
                ErrorMessage = commandResponse.Success ? string.Empty : commandResponse.Message ?? "An unknown error occurred."
            };
        }
        catch (ValidationException valEx)
        {
            _logger.LogWarning(valEx, "Validation error processing SendNotification request for Recipient: {Recipient}, Type: {Type}. Errors: {Errors}", 
                request.Recipient, request.Type, string.Join("; ", valEx.Errors.Select(e => e.ErrorMessage)));
            throw new RpcException(new Status(StatusCode.InvalidArgument, "Validation failed: " + string.Join("; ", valEx.Errors.Select(e => e.ErrorMessage))));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error processing SendNotification request for Recipient: {Recipient}, Type: {Type}", request.Recipient, request.Type);
            throw new RpcException(new Status(StatusCode.Internal, $"An internal error occurred: {ex.Message}"));
        }
    }
} 