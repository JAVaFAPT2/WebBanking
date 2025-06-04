using Confluent.Kafka;
using Domain.Events;
using Domain.Interface;
using Domain.models;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using StackExchange.Redis;
using System.Text.Json;

namespace Infrastructure.EventBus
{
    public class KycVerifiedEventConsumer
    {
        private readonly IKycDocumentRepository _kycDocumentRepository;
        private readonly IConnectionMultiplexer _redis;
        private readonly IConsumer<string, string> _consumer;
        private readonly ILogger<KycVerifiedEventConsumer> _logger;

        public KycVerifiedEventConsumer(
            IKycDocumentRepository kycDocumentRepository,
            IConnectionMultiplexer redis,
            IConfiguration configuration,
            ILogger<KycVerifiedEventConsumer> logger)
        {
            _kycDocumentRepository = kycDocumentRepository;
            _redis = redis;
            _logger = logger;

            var config = new ConsumerConfig
            {
                BootstrapServers = configuration["Kafka:BootstrapServers"],
                GroupId = "kyc-service-group",
                AutoOffsetReset = AutoOffsetReset.Earliest
            };

            _consumer = new ConsumerBuilder<string, string>(config).Build();
        }

        public async Task StartConsuming(CancellationToken cancellationToken)
        {
            _consumer.Subscribe("user-events");
            _logger.LogInformation("KYC Verified Consumer started and subscribed to 'user-events'.");

            try
            {
                while (!cancellationToken.IsCancellationRequested)
                {
                    try
                    {
                        var consumeResult = _consumer.Consume(cancellationToken);

                        if (consumeResult?.Message?.Key == nameof(KycVerifiedEvent))
                        {
                            var @event = JsonSerializer.Deserialize<KycVerifiedEvent>(consumeResult.Message.Value);
                            if (@event != null)
                            {
                                _logger.LogInformation("Received KYC verified event for user {UserId}", @event.UserId);
                                await HandleEvent(@event);
                            }
                        }
                    }
                    catch (ConsumeException ex)
                    {
                        _logger.LogError(ex, "Kafka consume error: {Reason}", ex.Error.Reason);
                    }
                    catch (JsonException ex)
                    {
                        _logger.LogError(ex, "JSON deserialization failed.");
                    }
                }
            }
            catch (OperationCanceledException)
            {
                _logger.LogInformation("KYC Consumer stopping due to cancellation.");
            }
            finally
            {
                _consumer.Close();
                _consumer.Dispose();
                _logger.LogInformation("KYC Consumer gracefully shut down.");
            }
        }

        private async Task HandleEvent(KycVerifiedEvent @event)
        {
            try
            {
                var kycDocument = new KycDocument
                {
                    UserId = @event.UserId,
                    DocumentType = @event.DocumentType ?? "Unknown",
                    DocumentNumber = @event.DocumentNumber ?? "N/A",
                    IssuingCountry = @event.IssuingCountry ?? "N/A",
                    ExpiryDate = @event.ExpiryDate ?? DateTime.UtcNow.AddYears(1),
                    DocumentPath = @event.DocumentPath ?? "/path/to/document",
                    Status = @event.Status,
                    SubmissionDate = @event.VerifiedAt ?? DateTime.UtcNow,
                    VerificationDate = @event.VerifiedAt ?? DateTime.UtcNow
                };

                await _kycDocumentRepository.AddAsync(kycDocument);
                _logger.LogInformation("KYC document saved to database for user {UserId}", @event.UserId);

                var db = _redis.GetDatabase();
                await db.StringSetAsync($"KYC:{@event.UserId}", @event.Status.ToString(), TimeSpan.FromHours(1));
                _logger.LogInformation("KYC status cached in Redis for user {UserId}", @event.UserId);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to handle KYC verification event for user {UserId}", @event.UserId);
            }
        }
    }
}
