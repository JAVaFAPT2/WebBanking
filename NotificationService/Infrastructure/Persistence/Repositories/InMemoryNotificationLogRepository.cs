using NotificationService.Domain.Entities;
using NotificationService.Domain.Interfaces;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace NotificationService.Infrastructure.Persistence.Repositories;

public class InMemoryNotificationLogRepository : INotificationLogRepository
{
    private readonly ConcurrentDictionary<Guid, NotificationLog> _logs = new();

    public Task AddAsync(NotificationLog notificationLog, CancellationToken cancellationToken = default)
    {
        _logs[notificationLog.Id] = notificationLog;
        return Task.CompletedTask;
    }

    public Task<NotificationLog?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
    {
        _logs.TryGetValue(id, out var log);
        return Task.FromResult(log);
    }

    public Task<IEnumerable<NotificationLog>> GetByRecipientAsync(string recipient, CancellationToken cancellationToken = default)
    {
        var results = _logs.Values.Where(log => log.Recipient == recipient).ToList();
        return Task.FromResult<IEnumerable<NotificationLog>>(results);
    }
    
    public Task<IEnumerable<NotificationLog>> GetByCorrelationIdAsync(string correlationId, CancellationToken cancellationToken = default)
    {
        var results = _logs.Values.Where(log => log.CorrelationId == correlationId).ToList();
        return Task.FromResult<IEnumerable<NotificationLog>>(results);
    }

    public Task UpdateAsync(NotificationLog notificationLog, CancellationToken cancellationToken = default)
    {
        // In-memory update is essentially an overwrite or handled by reference if the object is mutable
        if (_logs.ContainsKey(notificationLog.Id))
        {
            _logs[notificationLog.Id] = notificationLog;
        }
        // Optionally, throw if not found, or handle as an upsert
        return Task.CompletedTask;
    }
} 