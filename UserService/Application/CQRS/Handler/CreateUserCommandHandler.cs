using Application.CQRS.Commands;
using Confluent.Kafka;
using Domain.Interface;
using Domain.models;
using MediatR;
using Microsoft.EntityFrameworkCore;
using System.Text.Json;
using Polly;
using StackExchange.Redis;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Configuration;

namespace Application.CQRS.Handler
{
    public class CreateUserCommandHandler : IRequestHandler<CreateUserCommand, Guid>
    {
        private readonly IUserRepository _userRepository;
        private readonly IProducer<Null, string>? _kafkaProducer;
        private readonly IConnectionMultiplexer? _redis;
        private readonly ILogger<CreateUserCommandHandler>? _logger;
        private readonly IConfiguration _configuration;

        public CreateUserCommandHandler(
            IUserRepository userRepository,
            IConfiguration configuration,
            IProducer<Null, string>? kafkaProducer = null,
            IConnectionMultiplexer? redis = null,
            ILogger<CreateUserCommandHandler>? logger = null)
        {
            _userRepository = userRepository;
            _kafkaProducer = kafkaProducer;
            _redis = redis;
            _logger = logger;
            _configuration = configuration;
        }

        public async Task<Guid> Handle(CreateUserCommand request, CancellationToken cancellationToken)
        {
            var user = new User(
                request.Username,
                request.FirstName,
                request.LastName,
                request.Email,
                request.PhoneNumber,
                BCrypt.Net.BCrypt.HashPassword(request.Password),
                request.DateOfBirth,
                new Domain.ValueObjects.Address(
                    request.Address.Street,
                    request.Address.City,
                    request.Address.State,
                    request.Address.ZipCode,
                    request.Address.Country
                )
            );
            user.KycVerificationNotes = string.Empty;

            // Add user to database with retry policy
            var policy = Policy.Handle<DbUpdateException>()
                .WaitAndRetryAsync(3, retryAttempt => TimeSpan.FromSeconds(5));
            
            await policy.ExecuteAsync(async () => await _userRepository.AddAsync(user));

            // Try to publish to Kafka, but don't fail if it's not available
            try
            {
                if (_kafkaProducer != null)
                {
                    var kafkaTopic = _configuration["Kafka:Topic"] ?? "user-events";
                    var eventMessage = JsonSerializer.Serialize(new
                    {
                        UserId = user.Id,
                        Email = user.Email,
                        Username = user.Username,
                        EventType = "UserCreated"
                    });
                    
                    await _kafkaProducer.ProduceAsync(kafkaTopic, new Message<Null, string> { Value = eventMessage }, cancellationToken);
                    _logger?.LogInformation("Published user created event to Kafka topic {Topic} for user {UserId}", kafkaTopic, user.Id);
                }
                else
                {
                    _logger?.LogInformation("Kafka producer not available - skipping event publishing for user {UserId}", user.Id);
                }
            }
            catch (Exception ex)
            {
                // Log the error but don't fail the registration
                _logger?.LogWarning(ex, "Failed to publish user created event to Kafka for user {UserId}", user.Id);
            }

            // Try to cache in Redis, but don't fail if it's not available
            try
            {
                if (_redis != null)
                {
                    var database = _redis.GetDatabase();
                    var serializedUser = JsonSerializer.Serialize(new
                    {
                        user.Id,
                        user.Username,
                        user.Email,
                        user.FirstName,
                        user.LastName
                    });
                    
                    var cacheDuration = _configuration.GetValue<int?>("Redis:DefaultCacheDurationMinutes") ?? 10;
                    await database.StringSetAsync($"user:{user.Id}", serializedUser, TimeSpan.FromMinutes(cacheDuration));
                    _logger?.LogInformation("Cached user {UserId} in Redis for {Duration} minutes", user.Id, cacheDuration);
                }
                else
                {
                    _logger?.LogInformation("Redis connection not available - skipping caching for user {UserId}", user.Id);
                }
            }
            catch (Exception ex)
            {
                // Log the error but don't fail the registration
                _logger?.LogWarning(ex, "Failed to cache user in Redis for user {UserId}", user.Id);
            }

            return user.Id;
        }
    }
}
