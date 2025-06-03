using Confluent.Kafka;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Newtonsoft.Json;
using System;
using System.Threading.Tasks;
using TransactionService.Infrastructure.Configuration; // Assuming KafkaSettings will be here

namespace TransactionService.Infrastructure.EventBus;

public class KafkaProducerService : IEventProducer, IDisposable
{
    private readonly IProducer<string, string> _producer;
    private readonly ILogger<KafkaProducerService> _logger;

    public KafkaProducerService(IOptions<KafkaSettings> kafkaSettings, ILogger<KafkaProducerService> logger)
    {
        _logger = logger;
        var config = new ProducerConfig 
        { 
            BootstrapServers = kafkaSettings.Value.BootstrapServers,
            // Add any other necessary producer configurations here, e.g., Acks, MessageTimeoutMs
        };
        _producer = new ProducerBuilder<string, string>(config).Build();
        _logger.LogInformation("KafkaProducerService initialized for servers: {BootstrapServers}", kafkaSettings.Value.BootstrapServers);
    }

    public async Task ProduceAsync<TKey, TValue>(string topic, TKey key, TValue value)
    {
        try
        {
            var messageKey = key?.ToString(); // Kafka producer typically uses string keys
            var messageValue = JsonConvert.SerializeObject(value, new JsonSerializerSettings
            {
                TypeNameHandling = TypeNameHandling.All // Important for polymorphic deserialization on consumer side
            });

            _logger.LogInformation("Producing message to Kafka. Topic: {Topic}, Key: {Key}", topic, messageKey);
            // _logger.LogDebug("Message value: {Value}", messageValue); // Be cautious logging full message value

            var deliveryResult = await _producer.ProduceAsync(topic, new Message<string, string> { Key = messageKey, Value = messageValue });

            if (deliveryResult.Status == PersistenceStatus.NotPersisted || deliveryResult.Status == PersistenceStatus.PossiblyPersisted)
            {
                _logger.LogWarning("Message to topic {Topic} with key {Key} was not persisted or possibly not persisted. Status: {Status}", 
                    topic, messageKey, deliveryResult.Status);
            }
            else
            {
                _logger.LogInformation("Message delivered to Kafka. Topic: {Topic}, Partition: {Partition}, Offset: {Offset}", 
                    deliveryResult.Topic, deliveryResult.Partition, deliveryResult.Offset);
            }
        }
        catch (ProduceException<string, string> e)
        {
            _logger.LogError(e, "Error producing message to Kafka. Topic: {Topic}, Key: {Key}, Error: {Error}", topic, key?.ToString(), e.Error.Reason);
            throw; // Rethrow to allow caller to handle
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "An unexpected error occurred while producing message to Kafka. Topic: {Topic}, Key: {Key}", topic, key?.ToString());
            throw;
        }
    }

    public void Dispose()
    {
        _producer.Flush(TimeSpan.FromSeconds(10)); // Ensure all outstanding messages are sent
        _producer.Dispose();
        _logger.LogInformation("KafkaProducerService disposed.");
    }
} 