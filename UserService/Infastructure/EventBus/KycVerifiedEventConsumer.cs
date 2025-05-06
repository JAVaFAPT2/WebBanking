using Confluent.Kafka;
using Domain.Events;
using Domain.Interface;
using Domain.models;
using Microsoft.Extensions.Configuration;
using StackExchange.Redis;
using System.Text.Json;

namespace Infrastructure.EventBus
{
    public class KycVerifiedEventConsumer
    {
        private readonly IKycDocumentRepository _kycDocumentRepository;
        private readonly IConnectionMultiplexer _redis;
        private readonly IConsumer<string, string> _consumer;

        public KycVerifiedEventConsumer(
            IKycDocumentRepository kycDocumentRepository,
            IConnectionMultiplexer redis,
            IConfiguration configuration)
        {
            _kycDocumentRepository = kycDocumentRepository;
            _redis = redis;

            var config = new ConsumerConfig
            {
                BootstrapServers = configuration["Kafka:BootstrapServers"],
                GroupId = "kyc-service-group",
                AutoOffsetReset = AutoOffsetReset.Earliest
            };

            _consumer = new ConsumerBuilder<string, string>(config).Build();
        }

        public void StartConsuming(CancellationToken cancellationToken)
        {
            _consumer.Subscribe("user-events");

            try
            {
                while (!cancellationToken.IsCancellationRequested)
                {
                    var consumeResult = _consumer.Consume(cancellationToken);

                    if (consumeResult.Message.Key == nameof(KycVerifiedEvent))
                    {
                        var @event = JsonSerializer.Deserialize<KycVerifiedEvent>(consumeResult.Message.Value);
                        if (@event != null)
                        {
                            HandleEvent(@event).Wait();
                        }
                    }
                }
            }
            catch (OperationCanceledException)
            {
                _consumer.Close();
            }
        }

        private async Task HandleEvent(KycVerifiedEvent @event)
        {
            // Save KYC document
            var kycDocument = new KycDocument(
                @event.UserId,
                "VerifiedDocument",
                "/path/to/document"
            );
            await _kycDocumentRepository.AddAsync(kycDocument);

            // Cache KYC status in Redis
            var db = _redis.GetDatabase();
            await db.StringSetAsync($"KYC:{@event.UserId}", @event.Status.ToString());
        }
    }
}
