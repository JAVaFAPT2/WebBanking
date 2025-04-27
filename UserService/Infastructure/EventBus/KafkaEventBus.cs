
using Application.EventBus;
using Confluent.Kafka;
using Microsoft.Extensions.Configuration;
using System.Text.Json;
using System.Threading.Tasks;

namespace Infrastructure.EventBus
{
    public class KafkaEventBus : IEventBus
    {
        private readonly IProducer<string, string> _producer;
        private readonly string _topic;

        public KafkaEventBus(IConfiguration cfg)
        {
            var brokerList = cfg["Kafka:BootstrapServers"];
            _topic = cfg["Kafka:Topic"] ?? "user-events";

            var pConfig = new ProducerConfig { BootstrapServers = brokerList };
            _producer = new ProducerBuilder<string, string>(pConfig).Build();
        }

        public async Task PublishAsync<TEvent>(TEvent @event) where TEvent : class
        {
            var key = typeof(TEvent).Name;
            var payload = JsonSerializer.Serialize(@event);
            await _producer.ProduceAsync(_topic, new Message<string, string> { Key = key, Value = payload });
        }
    }
}
