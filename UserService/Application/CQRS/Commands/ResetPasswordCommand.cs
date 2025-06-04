using MediatR;

namespace Application.CQRS.Commands;

public record ResetPasswordCommand(string Email, string Token, string NewPassword) : IRequest<bool>;