using Application.CQRS.Commands;
using Application.EventBus;
using Domain.Interface;
using Domain.ValueObjects;
using MediatR;
using StackExchange.Redis;

namespace Application.CQRS.Handler
{
    public class UpdateUserCommandHandler : IRequestHandler<UpdateUserCommand, Unit>
    {
        private readonly IUserRepository _userRepository;
        private readonly IEventBus _eventBus;
        private readonly IConnectionMultiplexer _redis;

        public UpdateUserCommandHandler(
            IUserRepository userRepository,
            IEventBus eventBus,
            IConnectionMultiplexer redis)
        {
            _userRepository = userRepository;
            _eventBus = eventBus;
            _redis = redis;
        }

        public async Task<Unit> Handle(UpdateUserCommand request, CancellationToken cancellationToken)
        {
            var db = _redis.GetDatabase();
            await db.StringSetAsync($"log:UpdateUser:{request.UserId}", "Processing UpdateUserCommand");

            var user = await _userRepository.GetByIdAsync(request.UserId);
            if (user == null)
            {
                await db.StringSetAsync($"log:UpdateUser:{request.UserId}", "User not found");
                throw new Exception($"User with ID {request.UserId} not found");
            }

            // Map AddressC to Address
            var address = new Address(
                request.Address.Street,
                request.Address.City,
                request.Address.State,
                request.Address.ZipCode,
                request.Address.Country
            );

            // Update user profile
            user.UpdateProfile(
                firstName: request.FirstName,
                lastName: request.LastName,
                phoneNumber: request.PhoneNumber,
                address: address
            );
            await db.StringSetAsync($"log:UpdateUser:{request.UserId}", "User profile updated");
            await _userRepository.UpdateAsync(user);
            await db.StringSetAsync($"log:UpdateUser:{request.UserId}", "User successfully updated");

            // Publish user updated event
            var @event = new Domain.Events.UserUpdatedEvent(
                user.Id,
                user.Username,
                user.Email,
                DateTime.UtcNow
            );
            await _eventBus.PublishAsync(@event);
            await db.StringSetAsync($"log:UpdateUser:{request.UserId}", "UserUpdatedEvent published");

            return Unit.Value;
        }
    }
}
