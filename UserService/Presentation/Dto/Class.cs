namespace Presentation.Dto;

public record ForgotPasswordRequest
{
    public string Email { get; set; }
}