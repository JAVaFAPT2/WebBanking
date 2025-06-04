using Microsoft.Extensions.Logging;
using NotificationService.Domain.Interfaces;
using System.Threading;
using System.Threading.Tasks;

namespace NotificationService.Infrastructure.Services;

public class PlaceholderEmailProvider : IEmailProvider
{
    private readonly ILogger<PlaceholderEmailProvider> _logger;

    public PlaceholderEmailProvider(ILogger<PlaceholderEmailProvider> logger)
    {
        _logger = logger;
    }

    public Task SendEmailAsync(string toEmail, string subject, string htmlBody, string? fromEmail = null, string? fromName = null, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation(
            "[PlaceholderEmailProvider] Sending email: To={ToEmail}, From={FromEmail}, Name={FromName}, Subject={Subject}, BodyLength={BodyLength}",
            toEmail, fromEmail ?? "DefaultSender", fromName ?? "Default Name", subject, htmlBody.Length);
        
        // Simulate a successful send
        return Task.CompletedTask;
        
        // To simulate a failure:
        // return Task.FromException(new System.Net.Mail.SmtpException("Simulated SMTP failure."));
    }
} 