using FundTransferService.Domain.Configuration;
using FundTransferService.Domain.Interfaces;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using StackExchange.Redis;
using System.Text.Json;

namespace FundTransferService.Infrastructure.Caching;

public class RedisCacheService : ICacheService
{
    private readonly IDatabase _db;
    private readonly ILogger<RedisCacheService> _logger;
    private readonly FundTransferServiceSettings _settings;
    private readonly JsonSerializerOptions _jsonOptions = new JsonSerializerOptions
    {
        PropertyNameCaseInsensitive = true
        // Add other options if needed
    };

    public RedisCacheService(
        IConnectionMultiplexer redis,
        IOptions<FundTransferServiceSettings> settings,
        ILogger<RedisCacheService> logger)
    {
        _db = redis.GetDatabase();
        _settings = settings.Value;
        _logger = logger;
    }

    private string GetPrefixedKey(string key) => $"{_settings.CacheKeyPrefix}{key}";

    public async Task<T?> GetAsync<T>(string key, CancellationToken cancellationToken = default)
    {
        var prefixedKey = GetPrefixedKey(key);
        try
        {
            RedisValue redisValue = await _db.StringGetAsync(prefixedKey);
            if (redisValue.IsNullOrEmpty)
            {
                _logger.LogDebug("Cache miss for key: {PrefixedKey}", prefixedKey);
                return default;
            }
            _logger.LogDebug("Cache hit for key: {PrefixedKey}", prefixedKey);
            return JsonSerializer.Deserialize<T>(redisValue.ToString(), _jsonOptions);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error getting value from Redis for key {PrefixedKey}", prefixedKey);
            return default; // Or rethrow, depending on error handling strategy
        }
    }

    public async Task SetAsync<T>(string key, T value, TimeSpan? expiry = null, CancellationToken cancellationToken = default)
    {
        var prefixedKey = GetPrefixedKey(key);
        try
        {
            var actualExpiry = expiry ?? TimeSpan.FromMinutes(_settings.CacheExpirationMinutes);
            var serializedValue = JsonSerializer.Serialize(value, _jsonOptions);
            bool setResult = await _db.StringSetAsync(prefixedKey, serializedValue, actualExpiry);
            if (setResult)
            {
                _logger.LogDebug("Value set in Redis for key {PrefixedKey} with expiry {Expiry}", prefixedKey, actualExpiry);
            }
            else
            {
                _logger.LogWarning("Failed to set value in Redis for key {PrefixedKey}", prefixedKey);
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error setting value in Redis for key {PrefixedKey}", prefixedKey);
        }
    }

    public async Task RemoveAsync(string key, CancellationToken cancellationToken = default)
    {
        var prefixedKey = GetPrefixedKey(key);
        try
        {
            await _db.KeyDeleteAsync(prefixedKey);
            _logger.LogDebug("Key {PrefixedKey} removed from Redis", prefixedKey);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error removing key {PrefixedKey} from Redis", prefixedKey);
        }
    }

    public async Task<bool> ExistsAsync(string key, CancellationToken cancellationToken = default)
    {
        var prefixedKey = GetPrefixedKey(key);
        try
        {
            return await _db.KeyExistsAsync(prefixedKey);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error checking key {PrefixedKey} existence in Redis", prefixedKey);
            return false;
        }
    }
}

// Optional: Static class for cache keys if you want to centralize them
public static class CacheKeys
{
    public static string FundTransferById(Guid transferId) => $"transfer:{transferId}";
    public static string FundTransfersByAccountId(Guid accountId) => $"account:{accountId}:transfers";
    // Add more specific keys as needed
} 