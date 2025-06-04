using Microsoft.Extensions.Logging;
using NotificationService.Domain.Interfaces;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace NotificationService.Infrastructure.Services;

public class PlaceholderPushProvider : IPushProvider
{
    private readonly ILogger<PlaceholderPushProvider> _logger;

    public PlaceholderPushProvider(ILogger<PlaceholderPushProvider> logger)
    {
        _logger = logger;
    }

    public Task SendPushNotificationAsync(string deviceToken, string title, string body, IDictionary<string, string>? data = null, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation(
            "[PlaceholderPushProvider] Sending SINGLE Push: Token={DeviceToken}, Title={Title}, BodyLength={BodyLength}, DataCount={DataCount}",
            deviceToken, title, body.Length, data?.Count ?? 0);
        
        return Task.CompletedTask;
    }

    public Task SendMulticastPushNotificationAsync(IEnumerable<string> deviceTokens, string title, string body, IDictionary<string, string>? data = null, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation(
            "[PlaceholderPushProvider] Sending MULTICAST Push: TokenCount={TokenCount}, Title={Title}, BodyLength={BodyLength}, DataCount={DataCount}",
            deviceTokens.Count(), title, body.Length, data?.Count ?? 0);
        
        return Task.CompletedTask;
    }
} 