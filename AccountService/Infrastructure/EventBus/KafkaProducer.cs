using Confluent.Kafka;
using Microsoft.Extensions.Options;
using Shared.Configuration;
using System.Text.Json;

namespace Infrastructure.EventBus;

public interface IKafkaProducer
{
    Task PublishAsync<T>(string topic, T message);
}

public class KafkaProducer : IKafkaProducer, IDisposable
{
    private readonly IProducer<Null, string> _producer;
    private readonly KafkaSettings _settings;

    public KafkaProducer(IOptions<AccountServiceSettings> settings)
    {
        _settings = settings.Value.Kafka;
        var config = new ProducerConfig
        {
            BootstrapServers = _settings.BootstrapServers,
            // Add any additional configuration here
            Acks = Acks.All,
            EnableIdempotence = true,
            MessageSendMaxRetries = 3,
            RetryBackoffMs = 1000
        };

        _producer = new ProducerBuilder<Null, string>(config).Build();
    }

    public async Task PublishAsync<T>(string topic, T message)
    {
        try
        {
            var json = JsonSerializer.Serialize(message);
            var kafkaMessage = new Message<Null, string>
            {
                Value = json
            };

            var deliveryResult = await _producer.ProduceAsync(topic, kafkaMessage);
            
            if (deliveryResult.Status != PersistenceStatus.Persisted)
            {
                throw new Exception($"Failed to deliver message to Kafka. Status: {deliveryResult.Status}");
            }
        }
        catch (ProduceException<Null, string> ex)
        {
            // Handle or log the error appropriately
            throw new Exception($"Failed to publish message to Kafka: {ex.Message}", ex);
        }
    }

    public void Dispose()
    {
        _producer?.Dispose();
    }
} 