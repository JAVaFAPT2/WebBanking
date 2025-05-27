using Microsoft.Extensions.Logging;
using StackExchange.Redis;
using System.Text.Json;

namespace Infrastructure.Logging;

public class RedisLogger : ILogger
{
    private readonly string _categoryName;
    private readonly IConnectionMultiplexer _redis;
    private readonly string _keyPrefix;

    public RedisLogger(string categoryName, IConnectionMultiplexer redis, string keyPrefix)
    {
        _categoryName = categoryName;
        _redis = redis;
        _keyPrefix = keyPrefix;
    }

    public IDisposable? BeginScope<TState>(TState state) where TState : notnull => null;

    public bool IsEnabled(LogLevel logLevel) => true;

    public void Log<TState>(
        LogLevel logLevel,
        EventId eventId,
        TState state,
        Exception? exception,
        Func<TState, Exception?, string> formatter)
    {
        if (!IsEnabled(logLevel))
            return;

        var logEntry = new LogEntry
        {
            Timestamp = DateTime.UtcNow,
            Level = logLevel.ToString(),
            Category = _categoryName,
            Message = formatter(state, exception),
            Exception = exception?.ToString(),
            EventId = eventId.Id
        };

        var json = JsonSerializer.Serialize(logEntry);
        var db = _redis.GetDatabase();
        var key = $"{_keyPrefix}:{DateTime.UtcNow:yyyy-MM-dd}";
        
        db.ListRightPush(key, json);
        // Set expiry to 30 days
        db.KeyExpire(key, TimeSpan.FromDays(30));
    }
}

public class LogEntry
{
    public DateTime Timestamp { get; set; }
    public string Level { get; set; } = string.Empty;
    public string Category { get; set; } = string.Empty;
    public string Message { get; set; } = string.Empty;
    public string? Exception { get; set; }
    public int EventId { get; set; }
} 