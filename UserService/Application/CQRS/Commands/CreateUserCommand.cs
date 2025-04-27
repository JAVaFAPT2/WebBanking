
using MediatR;

namespace Application.CQRS.Commands
{
    public record CreateUserCommand(string Username, string Email, string Password) : IRequest<Guid>;
}