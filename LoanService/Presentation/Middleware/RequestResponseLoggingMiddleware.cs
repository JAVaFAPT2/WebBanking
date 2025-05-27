using System.Diagnostics;

namespace Presentation.Middleware;

public class RequestResponseLoggingMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<RequestResponseLoggingMiddleware> _logger;

    public RequestResponseLoggingMiddleware(RequestDelegate next, ILogger<RequestResponseLoggingMiddleware> logger)
    {
        _next = next;
        _logger = logger;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        var correlationId = Guid.NewGuid().ToString();
        context.Response.Headers.Add("X-Correlation-ID", correlationId);

        var stopwatch = Stopwatch.StartNew();
        try
        {
            _logger.LogInformation(
                "Request starting [{CorrelationId}] {Method} {Path} {Query}",
                correlationId,
                context.Request.Method,
                context.Request.Path,
                context.Request.QueryString);

            await _next(context);

            stopwatch.Stop();
            _logger.LogInformation(
                "Request completed [{CorrelationId}] {Method} {Path} {StatusCode} in {ElapsedMilliseconds}ms",
                correlationId,
                context.Request.Method,
                context.Request.Path,
                context.Response.StatusCode,
                stopwatch.ElapsedMilliseconds);
        }
        catch (Exception ex)
        {
            stopwatch.Stop();
            _logger.LogError(
                ex,
                "Request failed [{CorrelationId}] {Method} {Path} in {ElapsedMilliseconds}ms",
                correlationId,
                context.Request.Method,
                context.Request.Path,
                stopwatch.ElapsedMilliseconds);
            throw;
        }
    }
} 