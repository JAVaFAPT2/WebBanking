using Application.CQRS.Commands;
using Application.EventBus;
using Domain.Interface;
using Domain.models;
using MediatR;

namespace Application.CQRS.Handler
{
    public class CreateUserCommandHandler(IUserRepository repo, IEventBus events) : IRequestHandler<CreateUserCommand, Guid>
    {
        private readonly IUserRepository _repo = repo;
        private readonly IEventBus _events = events;

        public async Task<Guid> Handle(CreateUserCommand request, CancellationToken ct)
        {
            if (string.IsNullOrWhiteSpace(request.Password))
            {
                throw new ArgumentException("Password cannot be null or empty.", nameof(request.Password));
            }
            var hashedPassword = BCrypt.Net.BCrypt.HashPassword(request.Password);
            var user = new User
            {
                Id = Guid.NewGuid(),
                Username = request.Username,
                Email = request.Email,
                PasswordHash = hashedPassword, // Ensure this is set
                CreatedAt = DateTime.UtcNow
            };
            await _repo.AddAsync(user);

            var @evt = new Domain.Events.UserCreatedEvent(
                          user.Id, user.Username, user.Email, user.CreatedAt);
            await _events.PublishAsync(@evt);

            return user.Id;
        }
    }
}