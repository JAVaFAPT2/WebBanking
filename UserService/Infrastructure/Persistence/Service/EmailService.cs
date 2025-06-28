using Domain.Interface;
using Microsoft.Extensions.Options;
using System.Net;
using System.Net.Mail;

namespace Infrastructure.Persistence.Service;

public class EmailService : IEmailService
{
    private readonly EmailSettings _settings;

    public EmailService(IOptions<EmailSettings> settings)
    {
        _settings = settings.Value;
    }

    public async Task SendPasswordResetEmailAsync(string email, string resetLink)
    {
        using var client = new SmtpClient(_settings.SmtpHost, _settings.SmtpPort)
        {
            Credentials = new NetworkCredential(_settings.SmtpUser, _settings.SmtpPass),
            EnableSsl = _settings.EnableSsl
        };

        var mail = new MailMessage
        {
            From = new MailAddress(_settings.From),
            Subject = "Password Reset Request",
            Body = $"Click the link to reset your password: {resetLink}",
            IsBodyHtml = false
        };
        mail.To.Add(email);

        await client.SendMailAsync(mail);
    }
}