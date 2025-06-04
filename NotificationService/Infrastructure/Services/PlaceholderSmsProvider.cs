using Microsoft.Extensions.Logging;
using NotificationService.Domain.Interfaces;
using System.Threading;
using System.Threading.Tasks;

namespace NotificationService.Infrastructure.Services;

public class PlaceholderSmsProvider : ISmsProvider
{
    private readonly ILogger<PlaceholderSmsProvider> _logger;

    public PlaceholderSmsProvider(ILogger<PlaceholderSmsProvider> logger)
    {
        _logger = logger;
    }

    public Task SendSmsAsync(string toNumber, string message, string? fromNumber = null, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation(
            "[PlaceholderSmsProvider] Sending SMS: To={ToNumber}, From={FromNumber}, MessageLength={MessageLength}",
            toNumber, fromNumber ?? "DefaultSenderID", message.Length);
        
        // Simulate a successful send
        return Task.CompletedTask;

        // To simulate a failure:
        // return Task.FromException(new System.Exception("Simulated SMS provider failure."));
    }
} 