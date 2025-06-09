using Application.CQRS.Commands;
using Application.CQRS.DTO;
using Domain.Interface;
using MediatR;
using System.Threading;
using System.Threading.Tasks;
using BCrypt.Net;

namespace Application.CQRS.Handler
{
    public class LoginUserCommandHandler : IRequestHandler<LoginUserCommand, AuthResponse>
    {
        private readonly IUserRepository _userRepository;

        public LoginUserCommandHandler(IUserRepository userRepository)
        {
            _userRepository = userRepository;
        }

        public async Task<AuthResponse> Handle(LoginUserCommand request, CancellationToken cancellationToken)
        {
            var user = await _userRepository.GetByUsernameAsync(request.Username);
            if (user == null)
                return null;

            // For now, just check if password matches (dummy logic, replace with real hash check)
            if (!BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash))
                return null;

            return new AuthResponse
            {
                Token = "dummy-token",
                Username = user.Username,
                Email = user.Email,
                UserId = user.Id
            };
        }
    }
} 