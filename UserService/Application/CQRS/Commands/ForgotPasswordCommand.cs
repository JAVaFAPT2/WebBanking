using MediatR;

public record ForgotPasswordCommand(string Email) : IRequest;
