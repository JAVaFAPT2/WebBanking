using System.Collections.Generic;
using System.Threading.Tasks;

namespace NotificationService.Domain.Interfaces;

public interface IPushProvider
{
    Task SendPushNotificationAsync(
        string deviceToken, // Or a user ID/topic if your provider supports it
        string title,
        string body,
        IDictionary<string, string>? data = null, // Optional payload
        CancellationToken cancellationToken = default);

    Task SendMulticastPushNotificationAsync(
        IEnumerable<string> deviceTokens,
        string title,
        string body,
        IDictionary<string, string>? data = null,
        CancellationToken cancellationToken = default);

    // Potentially methods for subscribing/unsubscribing to topics if applicable
    // Task SubscribeToTopicAsync(string deviceToken, string topic, CancellationToken cancellationToken = default);
    // Task UnsubscribeFromTopicAsync(string deviceToken, string topic, CancellationToken cancellationToken = default);
} 