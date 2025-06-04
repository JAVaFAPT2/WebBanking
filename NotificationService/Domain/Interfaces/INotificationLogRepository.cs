using NotificationService.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace NotificationService.Domain.Interfaces;

public interface INotificationLogRepository
{
    Task<NotificationLog?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);
    Task AddAsync(NotificationLog notificationLog, CancellationToken cancellationToken = default);
    Task UpdateAsync(NotificationLog notificationLog, CancellationToken cancellationToken = default);
    Task<IEnumerable<NotificationLog>> GetByRecipientAsync(string recipient, CancellationToken cancellationToken = default);
    Task<IEnumerable<NotificationLog>> GetByCorrelationIdAsync(string correlationId, CancellationToken cancellationToken = default);
    // Add other query methods as needed, e.g., GetByStatus, GetByChannelAndDateRange, etc.
} 