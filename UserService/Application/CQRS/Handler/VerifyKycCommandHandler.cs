using Application.CQRS.Commands;
using Application.EventBus;
using Domain.Events;
using Domain.Interface;
using Domain.models;
using MediatR;

namespace Application.CQRS.Handler
{
    public class VerifyKycCommandHandler : IRequestHandler<VerifyKycCommand, Unit>
    {
        private readonly IUserRepository _userRepository;
        private readonly IEventBus _eventBus;

        public VerifyKycCommandHandler(IUserRepository userRepository, IEventBus eventBus)
        {
            _userRepository = userRepository;
            _eventBus = eventBus;
        }

        public async Task<Unit> Handle(VerifyKycCommand request, CancellationToken cancellationToken)
        {
            var user = await _userRepository.GetByIdAsync(request.UserId);
            if (user == null)
            {
                throw new Exception($"User with ID {request.UserId} not found");
            }

            user.UpdateKycStatus(request.IsVerified ? KycStatus.Verified : KycStatus.Rejected);
            await _userRepository.UpdateAsync(user);

            // Publish KYC Verified Event
            var @event = new KycVerifiedEvent(
                request.UserId,
                request.IsVerified ? KycStatus.Verified : KycStatus.Rejected
            );
            await _eventBus.PublishAsync(@event);

            return Unit.Value;
        }
    }
}