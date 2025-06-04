namespace Domain.Interface;

public interface IEmailService
{
    Task SendPasswordResetEmailAsync(string email, string resetLink);
}