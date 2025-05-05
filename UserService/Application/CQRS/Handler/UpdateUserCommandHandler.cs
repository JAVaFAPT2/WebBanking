using Application.CQRS.Commands;
using Application.EventBus;
using Domain.Interface;
using Domain.ValueObjects;
using MediatR;

namespace Application.CQRS.Handler;

public class UpdateUserCommandHandler : IRequestHandler<UpdateUserCommand, Unit>
{
    private readonly IUserRepository _userRepository;
    private readonly IEventBus _eventBus;

    public UpdateUserCommandHandler(IUserRepository userRepository, IEventBus eventBus)
    {
        _userRepository = userRepository;
        _eventBus = eventBus;
    }

    public async Task<Unit> Handle(UpdateUserCommand request, CancellationToken cancellationToken)
    {
        var user = await _userRepository.GetByIdAsync(request.UserId);
        if (user == null)
        {
            throw new Exception($"User with ID {request.UserId} not found");
        }

        // Map AddressC to Address
        var address = new Address(
            request.Street,
            request.City,
            request.State,
            request.ZipCode,
            request.Country
        );

        // Update user profile using the proper method
        user.UpdateProfile(
            firstName: request.FirstName,
            lastName: request.LastName,
            phoneNumber: request.PhoneNumber,
            address: address
        );

        await _userRepository.UpdateAsync(user);

        // Publish user updated event
        var @event = new Domain.Events.UserUpdatedEvent(
            user.Id,
            user.Username,
            user.Email,
            DateTime.UtcNow
        );
        await _eventBus.PublishAsync(@event);

        return Unit.Value;
    }
}