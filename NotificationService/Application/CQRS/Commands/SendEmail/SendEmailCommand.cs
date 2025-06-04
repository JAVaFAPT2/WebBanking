using MediatR;
using NotificationService.Application.CQRS.DTO;

namespace NotificationService.Application.CQRS.Commands.SendEmail;

public record SendEmailCommand(
    string ToEmail,
    string Subject,
    string HtmlBody,
    string? FromEmail = null,
    string? FromName = null,
    string? CorrelationId = null
) : IRequest<SendNotificationResponseDto>; 