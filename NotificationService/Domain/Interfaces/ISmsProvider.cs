using System.Threading.Tasks;

namespace NotificationService.Domain.Interfaces;

public interface ISmsProvider
{
    Task SendSmsAsync(
        string toNumber,
        string message,
        string? fromNumber = null, // Optional: if not using a default sender ID/number
        CancellationToken cancellationToken = default);
} 