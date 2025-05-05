using Application.CQRS.Commands;
using Application.EventBus;
using Confluent.Kafka;
using Domain.Interface;
using Domain.models;
using Domain.ValueObjects;
using MediatR;
using Microsoft.EntityFrameworkCore;
using System.Text.Json;
using Polly;
using StackExchange.Redis;
using IDatabase = Microsoft.EntityFrameworkCore.Storage.IDatabase;

namespace Application.CQRS.Handler
{
    public class CreateUserCommandHandler : IRequestHandler<CreateUserCommand, Guid>
    {
        private readonly IUserRepository _userRepository;
        private readonly IProducer<Null, string> _kafkaProducer;
        private readonly StackExchange.Redis.IDatabase _redis;

        public CreateUserCommandHandler(IUserRepository userRepository, IProducer<Null, string> kafkaProducer, IConnectionMultiplexer redis)
        {
            _userRepository = userRepository;
            _kafkaProducer = kafkaProducer;
            _redis = redis.GetDatabase();
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

            var policy = Policy.Handle<DbUpdateException>()
                .WaitAndRetryAsync(3, retryAttempt => TimeSpan.FromSeconds(5));
            await policy.ExecuteAsync(async () => await _userRepository.AddAsync(user));

            var eventMessage = JsonSerializer.Serialize(new
            {
                UserId = user.Id,
                Email = user.Email,
                EventType = "UserCreated"
            });
            await _kafkaProducer.ProduceAsync("user-events", new Message<Null, string> { Value = eventMessage });

            await _redis.StringSetAsync($"user:{user.Id}", JsonSerializer.Serialize(user), TimeSpan.FromMinutes(10));

            return user.Id;
        }
    }
}
