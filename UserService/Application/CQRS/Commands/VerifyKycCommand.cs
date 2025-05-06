using MediatR;

namespace Application.CQRS.Commands
{
    public record VerifyKycCommand(
        Guid UserId,
        bool IsVerified
    ) : IRequest<Unit>;
}