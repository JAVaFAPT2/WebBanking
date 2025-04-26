using Application.CQRS.Commands;
using Domain.Interface;
using Domain.models;
using MediatR;

namespace Application.CQRS.Handler
{
    public class CreateUserCommandHandler(IUserRepository userRepository) : IRequestHandler<CreateUserCommand, Unit>
    {
        private readonly IUserRepository _userRepository = userRepository;

        async Task<Unit> IRequestHandler<CreateUserCommand, Unit>.Handle(CreateUserCommand request, CancellationToken cancellationToken)
        {
            var user = new User
            {
                Name = request.UserName,
                Email = request.Email,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password),
                CreatedAt = DateTime.UtcNow
            };
            await _userRepository.AddAsync(user);
            return Unit.Value;
        }
    }
}
