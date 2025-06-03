using Microsoft.Extensions.Caching.Distributed;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Newtonsoft.Json;
using System;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using TransactionService.Domain.Interfaces;
using TransactionService.Infrastructure.Configuration;

namespace TransactionService.Infrastructure.Caching;

public class RedisCacheService : ICacheService
{
    private readonly IDistributedCache _cache;
    private readonly ILogger<RedisCacheService> _logger;
    private readonly CacheSettings _cacheSettings;
    private readonly JsonSerializerSettings _serializerSettings;

    public RedisCacheService(
        IDistributedCache cache, 
        ILogger<RedisCacheService> logger, 
        IOptions<CacheSettings> cacheSettings)
    {
        _cache = cache;
        _logger = logger;
        _cacheSettings = cacheSettings.Value;
        _serializerSettings = new JsonSerializerSettings
        {
            TypeNameHandling = TypeNameHandling.All // Important for polymorphic types
        };
    }

    private string GetPrefixedKey(string key) => $"{_cacheSettings.InstanceName}{key}";

    public async Task<T?> GetAsync<T>(string key, CancellationToken cancellationToken = default)
    {
        var prefixedKey = GetPrefixedKey(key);
        _logger.LogDebug("Fetching from cache. Key: {PrefixedKey}", prefixedKey);
        try
        {
            var jsonData = await _cache.GetStringAsync(prefixedKey, cancellationToken);
            if (jsonData == null)
            {
                _logger.LogDebug("Cache miss for key: {PrefixedKey}", prefixedKey);
                return default;
            }
            _logger.LogDebug("Cache hit for key: {PrefixedKey}", prefixedKey);
            return JsonConvert.DeserializeObject<T>(jsonData, _serializerSettings);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error getting data from Redis cache for key {PrefixedKey}", prefixedKey);
            return default; // Or rethrow, depending on error handling strategy
        }
    }

    public async Task SetAsync<T>(string key, T value, TimeSpan? expiry = null, CancellationToken cancellationToken = default)
    {
        var prefixedKey = GetPrefixedKey(key);
        _logger.LogDebug("Setting cache. Key: {PrefixedKey}", prefixedKey);
        try
        {
            var jsonData = JsonConvert.SerializeObject(value, _serializerSettings);
            var options = new DistributedCacheEntryOptions
            {
                AbsoluteExpirationRelativeToNow = expiry ?? TimeSpan.FromMinutes(_cacheSettings.DefaultExpirationMinutes)
            };
            await _cache.SetStringAsync(prefixedKey, jsonData, options, cancellationToken);
            _logger.LogInformation("Successfully set cache for key: {PrefixedKey}", prefixedKey);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error setting data in Redis cache for key {PrefixedKey}", prefixedKey);
            // Depending on strategy, you might want to rethrow or handle gracefully
        }
    }

    public async Task RemoveAsync(string key, CancellationToken cancellationToken = default)
    {
        var prefixedKey = GetPrefixedKey(key);
        _logger.LogDebug("Removing from cache. Key: {PrefixedKey}", prefixedKey);
        try
        {
            await _cache.RemoveAsync(prefixedKey, cancellationToken);
            _logger.LogInformation("Successfully removed cache for key: {PrefixedKey}", prefixedKey);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error removing data from Redis cache for key {PrefixedKey}", prefixedKey);
        }
    }

    public async Task<bool> ExistsAsync(string key, CancellationToken cancellationToken = default)
    {
        var prefixedKey = GetPrefixedKey(key);
        _logger.LogDebug("Checking cache existence. Key: {PrefixedKey}", prefixedKey);
        try
        {
            var value = await _cache.GetAsync(prefixedKey, cancellationToken);
            return value != null;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error checking cache existence in Redis for key {PrefixedKey}", prefixedKey);
            return false;
        }
    }
} 