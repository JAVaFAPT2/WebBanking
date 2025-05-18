using Domain.Interface;
using MediatR;

public class ForgotPasswordCommandHandler : IRequestHandler<ForgotPasswordCommand>
{
    private readonly IUserRepository _userRepository;
    private readonly ITokenService _tokenService;
    private readonly IEmailService _emailService;

    public ForgotPasswordCommandHandler(
        IUserRepository userRepository,
        ITokenService tokenService,
        IEmailService emailService)
    {
        _userRepository = userRepository;
        _tokenService = tokenService;
        _emailService = emailService;
    }

    public async Task Handle(ForgotPasswordCommand request, CancellationToken cancellationToken)
    {
        var user = await _userRepository.GetByEmailAsync(request.Email);
        if (user == null)
            return; // Do not reveal if email exists

        var token = await _tokenService.GeneratePasswordResetTokenAsync(user.Id);

        // Compose reset link (adjust frontend URL as needed)
        var resetLink = $"https://yourfrontend.com/reset-password?email={Uri.EscapeDataString(request.Email)}&token={Uri.EscapeDataString(token)}";
        await _emailService.SendPasswordResetEmailAsync(request.Email, resetLink);
    }
}