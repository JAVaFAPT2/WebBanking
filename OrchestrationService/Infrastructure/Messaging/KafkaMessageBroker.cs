using System;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using Confluent.Kafka;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using OrchestrationService.Domain.Interfaces;
using OrchestrationService.Infrastructure.Configuration;

namespace OrchestrationService.Infrastructure.Messaging;

/// <summary>
/// Kafka implementation of IMessageBroker
/// </summary>
public class KafkaMessageBroker : IMessageBroker
{
    private readonly KafkaSettings _settings;
    private readonly ILogger<KafkaMessageBroker> _logger;
    
    public KafkaMessageBroker(IOptions<KafkaSettings> settings, ILogger<KafkaMessageBroker> logger)
    {
        _settings = settings.Value ?? throw new ArgumentNullException(nameof(settings));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }
    
    /// <inheritdoc />
    public async Task PublishAsync<T>(string topic, string key, T message, CancellationToken cancellationToken = default)
    {
        var config = new ProducerConfig
        {
            BootstrapServers = "kafka:9092",
            ClientId = _settings.ClientId
        };
        
        using var producer = new ProducerBuilder<string, string>(config).Build();
        
        try
        {
            var jsonMessage = JsonSerializer.Serialize(message);
            var result = await producer.ProduceAsync(topic, new Message<string, string>
            {
                Key = key,
                Value = jsonMessage
            }, cancellationToken);
            
            _logger.LogInformation("Message published to topic {Topic} with key {Key} at offset {Offset}",
                topic, key, result.Offset);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error publishing message to topic {Topic} with key {Key}", topic, key);
            throw;
        }
    }
    
    /// <inheritdoc />
    public Task SubscribeAsync<T>(string topic, string groupId, MessageHandler<T> handler, CancellationToken cancellationToken = default)
    {
        var config = new ConsumerConfig
        {
            BootstrapServers = "kafka:9092",
            GroupId = groupId,
            AutoOffsetReset = AutoOffsetReset.Earliest,
            EnableAutoCommit = true
        };
        
        var consumer = new ConsumerBuilder<string, string>(config).Build();
        consumer.Subscribe(topic);
        
        // Start a background task to process messages
        _ = Task.Run(async () =>
        {
            try
            {
                _logger.LogInformation("Starting to consume messages from topic {Topic} with group ID {GroupId}", 
                    topic, groupId);
                
                while (!cancellationToken.IsCancellationRequested)
                {
                    try
                    {
                        var consumeResult = consumer.Consume(cancellationToken);
                        
                        if (consumeResult?.Message?.Value == null)
                            continue;
                        
                        _logger.LogDebug("Received message from topic {Topic} with key {Key} at offset {Offset}",
                            topic, consumeResult.Message.Key, consumeResult.Offset);
                        
                        var deserializedMessage = JsonSerializer.Deserialize<T>(consumeResult.Message.Value);
                        
                        if (deserializedMessage != null)
                        {
                            await handler(deserializedMessage, consumeResult.Message.Key, cancellationToken);
                        }
                    }
                    catch (ConsumeException ex)
                    {
                        _logger.LogError(ex, "Error consuming message from topic {Topic}", topic);
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError(ex, "Error processing message from topic {Topic}", topic);
                    }
                }
            }
            catch (OperationCanceledException)
            {
                _logger.LogInformation("Message consumption from topic {Topic} was cancelled", topic);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Unexpected error during message consumption from topic {Topic}", topic);
            }
            finally
            {
                consumer.Close();
                consumer.Dispose();
            }
        }, cancellationToken);
        
        return Task.CompletedTask;
    }
} 