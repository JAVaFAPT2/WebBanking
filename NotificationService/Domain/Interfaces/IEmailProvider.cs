using System.Threading.Tasks;

namespace NotificationService.Domain.Interfaces;

public interface IEmailProvider
{
    Task SendEmailAsync(
        string toEmail,
        string subject,
        string htmlBody,
        string? fromEmail = null, // Optional: if not using a default sender
        string? fromName = null,  // Optional
        CancellationToken cancellationToken = default);
} 