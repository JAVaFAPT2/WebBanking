using System.Threading;
using System.Threading.Tasks;

namespace OrchestrationService.Domain.Interfaces;

/// <summary>
/// Interface for message broker communication
/// </summary>
public interface IMessageBroker
{
    /// <summary>
    /// Publish a message to a topic
    /// </summary>
    /// <typeparam name="T">Type of message</typeparam>
    /// <param name="topic">Kafka topic</param>
    /// <param name="key">Message key</param>
    /// <param name="message">Message content</param>
    /// <param name="cancellationToken">Cancellation token</param>
    /// <returns>Task representing the asynchronous operation</returns>
    Task PublishAsync<T>(string topic, string key, T message, CancellationToken cancellationToken = default);
    
    /// <summary>
    /// Subscribe to a topic
    /// </summary>
    /// <typeparam name="T">Type of message to expect</typeparam>
    /// <param name="topic">Kafka topic</param>
    /// <param name="groupId">Consumer group ID</param>
    /// <param name="handler">Message handler delegate</param>
    /// <param name="cancellationToken">Cancellation token</param>
    /// <returns>Task representing the asynchronous operation</returns>
    Task SubscribeAsync<T>(string topic, string groupId, MessageHandler<T> handler, CancellationToken cancellationToken = default);
}

/// <summary>
/// Delegate for message handlers
/// </summary>
/// <typeparam name="T">Type of message</typeparam>
/// <param name="message">Message content</param>
/// <param name="key">Message key</param>
/// <param name="cancellationToken">Cancellation token</param>
/// <returns>Task representing the asynchronous operation</returns>
public delegate Task MessageHandler<T>(T message, string key, CancellationToken cancellationToken); 