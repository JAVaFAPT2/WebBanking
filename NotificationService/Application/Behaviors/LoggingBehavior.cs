using MediatR;
using Microsoft.Extensions.Logging;
using System.Diagnostics;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;

namespace NotificationService.Application.Behaviors;

public class LoggingBehavior<TRequest, TResponse> : IPipelineBehavior<TRequest, TResponse>
    where TRequest : IRequest<TResponse>
{
    private readonly ILogger<LoggingBehavior<TRequest, TResponse>> _logger;

    public LoggingBehavior(ILogger<LoggingBehavior<TRequest, TResponse>> logger)
    {
        _logger = logger;
    }

    public async Task<TResponse> Handle(TRequest request, RequestHandlerDelegate<TResponse> next, CancellationToken cancellationToken)
    {
        var requestName = typeof(TRequest).Name;
        var requestGuid = Guid.NewGuid().ToString(); // Unique ID for this request instance for correlation

        _logger.LogInformation(
            "[START {RequestGuid}] Handling request {RequestName}. Request data: {RequestJson}", 
            requestGuid, requestName, JsonSerializer.Serialize(request));

        var stopwatch = Stopwatch.StartNew();
        TResponse response = default!;
        try
        {
            response = await next();
            stopwatch.Stop();
            _logger.LogInformation(
                "[END {RequestGuid}] Handled {RequestName} in {ElapsedMilliseconds}ms. Response data: {ResponseJson}", 
                requestGuid, requestName, stopwatch.ElapsedMilliseconds, JsonSerializer.Serialize(response));
            return response;
        }
        catch (Exception ex)
        {
            stopwatch.Stop();
            _logger.LogError(ex, "[ERROR {RequestGuid}] Exception handling {RequestName} after {ElapsedMilliseconds}ms. Request data: {RequestJson}",
                requestGuid, requestName, stopwatch.ElapsedMilliseconds, JsonSerializer.Serialize(request));
            throw;
        }
    }
} 