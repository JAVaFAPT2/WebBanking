using Domain.Configuration;
using Microsoft.Extensions.Options;
using StackExchange.Redis;
using System.Text.Json;

namespace Infrastructure.Caching;

public interface ICacheService
{
    Task<T?> GetAsync<T>(string key);
    Task SetAsync<T>(string key, T value, TimeSpan? expiry = null);
    Task RemoveAsync(string key);
    Task<bool> ExistsAsync(string key);
}

public class RedisCacheService : ICacheService
{
    private readonly IConnectionMultiplexer _redis;
    private readonly CardServiceSettings _settings;
    private readonly IDatabase _db;

    public RedisCacheService(
        IConnectionMultiplexer redis,
        IOptions<CardServiceSettings> settings)
    {
        _redis = redis;
        _settings = settings.Value;
        _db = _redis.GetDatabase();
    }

    public async Task<T?> GetAsync<T>(string key)
    {
        var value = await _db.StringGetAsync(_settings.CacheKeyPrefix + key);
        return value.HasValue ? JsonSerializer.Deserialize<T>(value!) : default;
    }

    public async Task SetAsync<T>(string key, T value, TimeSpan? expiry = null)
    {
        var serializedValue = JsonSerializer.Serialize(value);
        await _db.StringSetAsync(
            _settings.CacheKeyPrefix + key,
            serializedValue,
            expiry ?? TimeSpan.FromMinutes(_settings.CacheExpirationMinutes)
        );
    }

    public async Task RemoveAsync(string key)
    {
        await _db.KeyDeleteAsync(_settings.CacheKeyPrefix + key);
    }

    public async Task<bool> ExistsAsync(string key)
    {
        return await _db.KeyExistsAsync(_settings.CacheKeyPrefix + key);
    }

    // Cache key patterns for card data
    public static class CacheKeys
    {
        public static string Card(Guid cardId) => $"card:{cardId}";
        public static string CardByNumber(string cardNumber) => $"card:number:{cardNumber}";
        public static string AccountCards(Guid accountId) => $"account:{accountId}:cards";
        
        // For authorization rate limiting
        public static string AuthAttempts(string cardNumber) => $"auth:attempts:{cardNumber}";
    }
} 