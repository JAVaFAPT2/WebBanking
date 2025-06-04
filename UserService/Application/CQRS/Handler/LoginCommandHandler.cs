using Application.CQRS.Commands;
using Application.CQRS.DTO;
using Domain.Interface;
using MediatR;

public class LoginCommandHandler : IRequestHandler<LoginUserCommand, LoginResponseDto>
{
    private readonly IUserRepository _userRepository;
    private readonly IPasswordHasher _passwordHasher;
    private readonly ITokenService _tokenService;

    public LoginCommandHandler(IUserRepository userRepository, IPasswordHasher passwordHasher, ITokenService tokenService)
    {
        _userRepository = userRepository ?? throw new ArgumentNullException(nameof(userRepository));
        _passwordHasher = passwordHasher ?? throw new ArgumentNullException(nameof(passwordHasher));
        _tokenService = tokenService ?? throw new ArgumentNullException(nameof(tokenService));
    }

    public async Task<LoginResponseDto> Handle(LoginUserCommand request, CancellationToken cancellationToken)
    {
        if (string.IsNullOrWhiteSpace(request.Username) || string.IsNullOrWhiteSpace(request.Password))
        {
            throw new ArgumentException("Username and password are required.");
        }

        var user = await _userRepository.GetByUsernameAsync(request.Username);
        if (user == null || !_passwordHasher.Verify(request.Password, user.PasswordHash))
        {
            throw new UnauthorizedAccessException("Invalid username or password.");
        }

        var token = _tokenService.GenerateToken(user.Id, user.Username, user.Email);

        return new LoginResponseDto(
            token,
            user.Id,
            user.Username
        );
    }
}