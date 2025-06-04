namespace FundTransferService.Domain.Configuration;

public class FundTransferServiceSettings
{
    // Example: Cache settings, adjust as needed
    public string CacheKeyPrefix { get; set; } = "fundtransfer:";
    public int CacheExpirationMinutes { get; set; } = 30;

    // Add other service-specific settings here
    // e.g., MinTransferAmount, MaxTransferAmount, etc.
} 