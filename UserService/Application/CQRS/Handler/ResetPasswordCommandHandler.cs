using Application.CQRS.Commands;
using Domain.Interface;
using MediatR;

namespace Application.CQRS.Handler;

public class ResetPasswordCommandHandler : IRequestHandler<ResetPasswordCommand, bool>
{
    private readonly IUserRepository _userRepository;

    public ResetPasswordCommandHandler(IUserRepository userRepository)
    {
        _userRepository = userRepository;
    }

    public async Task<bool> Handle(ResetPasswordCommand request, CancellationToken cancellationToken)
    {
        var user = await _userRepository.GetByEmailAsync(request.Email);
        if (user == null)
            return false;

        user.PasswordHash = request.NewPassword;
        await _userRepository.UpdateAsync(user);

        return true;
    }
}