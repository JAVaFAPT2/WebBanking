using Application.CQRS.Commands;
using Domain.Interface;
using MediatR;

namespace Application.CQRS.Handler;

public class ResetPasswordCommandHandler(
    IUserRepository userRepository,
    ITokenService tokenService,
    IPasswordHasher passwordHasher)
    : IRequestHandler<ResetPasswordCommand, bool>
{
    public async Task<bool> Handle(ResetPasswordCommand request, CancellationToken cancellationToken)
    {
        var user = await userRepository.GetByEmailAsync(request.Email);
        if (user == null)
            return false;

        var isValid = await tokenService.ValidatePasswordResetTokenAsync(user.Id, request.Token);
        if (!isValid)
            return false;

        user.PasswordHash = passwordHasher.Hash(request.NewPassword);
        await userRepository.UpdateAsync(user);
        await tokenService.InvalidatePasswordResetTokenAsync(user.Id, request.Token);

        return true;
    }
}