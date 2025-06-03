namespace TransactionService.Infrastructure.Configuration;

public class CacheSettings
{
    public string RedisConnectionString { get; set; } = string.Empty;
    public string InstanceName { get; set; } = "TransactionService:"; // Prefix for cache keys
    public int DefaultExpirationMinutes { get; set; } = 60;
} 