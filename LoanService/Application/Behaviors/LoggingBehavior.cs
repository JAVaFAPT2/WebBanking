using System.Diagnostics;
using MediatR;
using Microsoft.Extensions.Logging;

namespace Application.Behaviors;

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
        var requestGuid = Guid.NewGuid().ToString();

        var requestNamespace = typeof(TRequest).Namespace;
        var isLoanOperation = requestNamespace?.Contains("Loan") ?? false;
        
        try
        {
            if (isLoanOperation)
            {
                _logger.LogInformation(
                    "Beginning Loan Operation: {RequestName} [{RequestGuid}] {@Request}",
                    requestName, requestGuid, request);
            }

            var stopwatch = Stopwatch.StartNew();
            var response = await next();
            stopwatch.Stop();

            if (isLoanOperation)
            {
                _logger.LogInformation(
                    "Completed Loan Operation: {RequestName} [{RequestGuid}] in {ElapsedMilliseconds}ms {@Response}",
                    requestName, requestGuid, stopwatch.ElapsedMilliseconds, response);
            }

            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Error Processing Loan Operation: {RequestName} [{RequestGuid}] {@Request}",
                requestName, requestGuid, request);
            throw;
        }
    }
} 