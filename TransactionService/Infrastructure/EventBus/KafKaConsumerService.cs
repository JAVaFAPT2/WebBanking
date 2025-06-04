using Confluent.Kafka;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Threading;
using System.Threading.Tasks;
using TransactionService.Application.IntegrationEvents.Handlers; // For IIntegrationEventHandler
using TransactionService.Infrastructure.Configuration; // For KafkaSettings

namespace TransactionService.Infrastructure.EventBus;

public class KafkaConsumerService : BackgroundService
{
    private readonly ILogger<KafkaConsumerService> _logger;
    private readonly KafkaSettings _kafkaSettings;
    private readonly IServiceProvider _serviceProvider; // To resolve scoped services like handlers
    private readonly Dictionary<string, Type> _eventTypesToTopics; // Maps event type names to topic names
    private readonly List<string> _topicsToSubscribe;

    public KafkaConsumerService(
        IOptions<KafkaSettings> kafkaSettings,
        ILogger<KafkaConsumerService> logger,
        IServiceProvider serviceProvider,
        // Inject a map of topics to event types or a way to discover them
        // For simplicity, we'll assume a convention or explicit mapping here
        // This part might need more sophisticated DI registration
        Dictionary<string, Type> eventTypesToTopics 
        )
    {
        _logger = logger;
        _kafkaSettings = kafkaSettings.Value;
        _serviceProvider = serviceProvider;
        _eventTypesToTopics = eventTypesToTopics ?? new Dictionary<string, Type>();

        // In a real app, topics might come from config or be dynamically registered.
        // For now, let's assume KafkaSettings might have a list of topics, or we derive from eventTypesToTopics.
        _topicsToSubscribe = _eventTypesToTopics.Values.Select(et => GetTopicNameForEventType(et)).Distinct().ToList();

        if(string.IsNullOrWhiteSpace(_kafkaSettings.ConsumerGroupId))
        {
            _logger.LogWarning("Kafka ConsumerGroupId is not configured. Using a default Guid-based group ID.");
            _kafkaSettings.ConsumerGroupId = $"transaction-service-{Guid.NewGuid().ToString().Substring(0,8)}";
        }

        if (!_topicsToSubscribe.Any())
        {
            _logger.LogWarning("KafkaConsumerService initialized, but no topics are configured for subscription based on event types.");
        }
        else
        {
            _logger.LogInformation("KafkaConsumerService initialized. GroupId: {GroupId}. Subscribing to topics: {Topics}", 
                _kafkaSettings.ConsumerGroupId, string.Join(", ", _topicsToSubscribe));
        }
    }

    // Helper to derive topic name (could be a convention, e.g., event type name)
    // This needs to align with how your producers are naming topics.
    private string GetTopicNameForEventType(Type eventType)
    {
        // Example convention: "com.company.events.TransactionService.PaymentGatewayCallbackEvent"
        // Or a simpler name like "payment-gateway-callbacks"
        // This needs to be defined and consistent across services.
        // For this example, let's assume the key in _eventTypesToTopics IS the topic name and the value is the event type.
        // So, the logic above in the constructor for _topicsToSubscribe needs adjustment if this is the case.
        // Let's refine constructor logic assuming _eventTypesToTopics keys are topics.
        return _eventTypesToTopics.FirstOrDefault(kvp => kvp.Value == eventType).Key ?? eventType.Name.ToLowerInvariant() + "s"; // Fallback convention
    }


    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        if (!_topicsToSubscribe.Any())
        {
            _logger.LogInformation("KafkaConsumerService: No topics to subscribe to. Execution will not start.");
            return;
        }

        var conf = new ConsumerConfig
        {
            GroupId = _kafkaSettings.ConsumerGroupId,
            BootstrapServers = _kafkaSettings.BootstrapServers,
            AutoOffsetReset = AutoOffsetReset.Earliest,
            EnableAutoCommit = false, // Manual commit for better control
            // Add other consumer configurations as needed
            // e.g., security, SSL settings
        };

        using var consumer = new ConsumerBuilder<Ignore, string>(conf)
            .SetErrorHandler((_, e) => _logger.LogError($"Kafka Consumer Error: {e.Reason}"))
            // .SetValueDeserializer(new JsonDeserializer<object>()) // Generic deserializer, then handle type
            .Build();
        
        consumer.Subscribe(_topicsToSubscribe);
        _logger.LogInformation("KafkaConsumerService subscribed to topics: {Topics}", string.Join(", ", _topicsToSubscribe));

        try
        {
            while (!stoppingToken.IsCancellationRequested)
            {
                try
                {
                    var consumeResult = consumer.Consume(stoppingToken); // Consume with cancellation token
                    if (consumeResult.IsPartitionEOF)
                    {
                        _logger.LogDebug($"Reached end of partition: {consumeResult.TopicPartitionOffset}. Continuing...");
                        continue;
                    }

                    _logger.LogInformation("Received message from Kafka. Topic: {Topic}, Partition: {Partition}, Offset: {Offset}", 
                        consumeResult.Topic, consumeResult.Partition, consumeResult.Offset);

                    await ProcessMessageAsync(consumeResult.Message.Value, consumeResult.Topic, stoppingToken);
                    
                    consumer.Commit(consumeResult); // Commit offset after successful processing
                    _logger.LogDebug("Offset committed for message. Topic: {Topic}, Offset: {Offset}", consumeResult.Topic, consumeResult.Offset);
                }
                catch (ConsumeException e)
                {
                    _logger.LogError(e, "Error consuming message from Kafka: {Reason}", e.Error.Reason);
                    // Implement retry/DLQ logic here if necessary
                    await Task.Delay(TimeSpan.FromSeconds(5), stoppingToken); // Delay before retrying to avoid tight loop on persistent errors
                }
                catch (OperationCanceledException) // Expected when stoppingToken is cancelled
                {
                    _logger.LogInformation("KafkaConsumerService stopping as cancellation was requested.");
                    break;
                }
                catch (Exception ex) // Catch-all for unexpected errors during message processing or commit
                {
                    _logger.LogError(ex, "Unexpected error in KafkaConsumerService loop.");
                    // Delay to prevent rapid looping on unexpected errors
                    await Task.Delay(TimeSpan.FromSeconds(10), stoppingToken);
                }
            }
        }
        finally
        {
            _logger.LogInformation("KafkaConsumerService unsubscribing and closing.");
            consumer.Close(); // Close the consumer connection
        }
    }

    private async Task ProcessMessageAsync(string messageJson, string topic, CancellationToken cancellationToken)
    {
        _logger.LogDebug("Processing message from topic {Topic}: {MessageJson}", topic, messageJson);
        Type? eventType = null;

        try
        {
            // Attempt to find the event type based on the topic it came from.
            // This assumes that _eventTypesToTopics maps TopicName -> EventType.
            // Adjust this logic if your mapping is different (e.g., based on a message header or a field in the JSON).

            if(!_eventTypesToTopics.TryGetValue(topic, out eventType))
            {
                 // Fallback: Try to determine type from Newtonsoft.Json $type if present (requires TypeNameHandling.All on producer)
                try 
                {
                    var tempObj = JsonConvert.DeserializeObject<dynamic>(messageJson, new JsonSerializerSettings { TypeNameHandling = TypeNameHandling.Auto });
                    string? typeName = tempObj?["$type"]?.Value;
                    if (!string.IsNullOrWhiteSpace(typeName))
                    {
                         // Attempt to find a matching registered type
                        eventType = _eventTypesToTopics.Values.FirstOrDefault(t => t.AssemblyQualifiedName?.Contains(typeName.Split(',')[0]) ?? false);
                        if (eventType == null) 
                        { 
                           // Last resort: Try Type.GetType - requires assembly qualified name for types not in current/executing assembly
                           // This is fragile and might require more robust type resolution (e.g. iterating loaded assemblies)
                           eventType = Type.GetType(typeName);
                        }
                    }
                }
                catch (Exception ex) { 
                    _logger.LogWarning(ex, "Could not determine event type from $type field for message on topic {Topic}", topic);
                }
            }

            if (eventType == null)
            {
                _logger.LogWarning("Could not determine event type for message from topic {Topic}. Message: {MessageJson}", topic, messageJson);
                return;
            }

            var integrationEvent = JsonConvert.DeserializeObject(messageJson, eventType, new JsonSerializerSettings 
            {
                 TypeNameHandling = TypeNameHandling.Auto // Allow $type if present for verification
            });

            if (integrationEvent == null)
            {
                _logger.LogWarning("Failed to deserialize message to type {EventType} from topic {Topic}. Message: {MessageJson}", eventType.FullName, topic, messageJson);
                return;
            }

            using var scope = _serviceProvider.CreateScope();
            var handlerType = typeof(IIntegrationEventHandler<>).MakeGenericType(eventType);
            var handler = scope.ServiceProvider.GetService(handlerType);

            if (handler == null)
            {
                _logger.LogWarning("No handler found for event type {EventType} from topic {Topic}", eventType.FullName, topic);
                return;
            }

            // Dynamically invoke the Handle method
            var handleMethod = handlerType.GetMethod("Handle");
            if (handleMethod != null)
            {
                await (Task)handleMethod.Invoke(handler, new[] { integrationEvent });
                _logger.LogInformation("Successfully processed event {EventType} from topic {Topic}", eventType.FullName, topic);
            }
            else
            {
                 _logger.LogError("Could not find Handle method on handler for event type {EventType}", eventType.FullName);
            }
        }
        catch (JsonSerializationException jsonEx)
        {
            _logger.LogError(jsonEx, "JSON Deserialization error processing message from topic {Topic}. Message: {MessageJson}", topic, messageJson);
            // Potentially move to DLQ
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error processing message of type {EventType} from topic {Topic}. Message: {MessageJson}", eventType?.FullName ?? "Unknown", topic, messageJson);
            // Potentially move to DLQ
        }
    }

    public override Task StopAsync(CancellationToken cancellationToken)
    {
        _logger.LogInformation("KafkaConsumerService is stopping.");
        return base.StopAsync(cancellationToken);
    }
} 