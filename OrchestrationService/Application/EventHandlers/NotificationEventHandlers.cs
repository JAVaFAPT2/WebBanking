using System;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using OrchestrationService.Application.Models.Events;
using OrchestrationService.Application.Services;

namespace OrchestrationService.Application.EventHandlers;

/// <summary>
/// Handles events from the Notification service
/// </summary>
public class NotificationEventHandlers
{
    private readonly FundTransferOrchestrator _fundTransferOrchestrator;
    private readonly ILogger<NotificationEventHandlers> _logger;

    /// <summary>
    /// Constructor
    /// </summary>
    public NotificationEventHandlers(
        FundTransferOrchestrator fundTransferOrchestrator,
        ILogger<NotificationEventHandlers> logger)
    {
        _fundTransferOrchestrator = fundTransferOrchestrator ?? throw new ArgumentNullException(nameof(fundTransferOrchestrator));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    /// <summary>
    /// Handle notification sent events
    /// </summary>
    public async Task HandleNotificationSentEventAsync(NotificationSentEvent @event, string key, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Received NotificationSentEvent for transaction {TransactionId}", @event.TransactionId);
        
        await _fundTransferOrchestrator.HandleNotificationSentAsync(
            @event.TransactionId,
            @event.NotificationId,
            cancellationToken);
    }

    /// <summary>
    /// Handle notification failed events
    /// </summary>
    public Task HandleNotificationFailedEventAsync(NotificationFailedEvent @event, string key, CancellationToken cancellationToken)
    {
        _logger.LogWarning("Received NotificationFailedEvent for transaction {TransactionId}: {Reason}",
            @event.TransactionId, @event.Reason);
        
        // We'll log the failure but still consider the fund transfer complete
        // since the actual money transfer was successful, even if notification failed
        
        return Task.CompletedTask;
    }
} 