using MediatR;
using NotificationService.Application.CQRS.DTO;
using System;

namespace NotificationService.Application.CQRS.Queries.GetNotificationLogById;

public record GetNotificationLogByIdQuery(Guid NotificationLogId) : IRequest<NotificationLogDto?>; 