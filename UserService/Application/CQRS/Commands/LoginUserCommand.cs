using MediatR;
using Application.CQRS.DTO;

namespace Application.CQRS.Commands
{
    public record LoginUserCommand(string Username, string Password) : IRequest<AuthResponse>;
} 