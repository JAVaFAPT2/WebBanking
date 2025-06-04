using MediatR;
using NotificationService.Application.CQRS.DTO;

namespace NotificationService.Application.CQRS.Commands.SendSms;

public record SendSmsCommand(
    string ToNumber,
    string Message,
    string? FromNumber = null,
    string? CorrelationId = null
) : IRequest<SendNotificationResponseDto>; 