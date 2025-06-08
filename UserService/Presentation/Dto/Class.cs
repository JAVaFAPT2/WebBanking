namespace Presentation.Dto;

public record ForgotPasswordRequest
{
    public string Email { get; set; }
}

public class LoginRequest
{
    public string Username { get; set; }
    public string Password { get; set; }
}