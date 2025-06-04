using System.Threading;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Logging;

namespace TransactionService.Application.Behaviors;

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
        // In a real app, you might want to serialize the request for more detailed logging,
        // but be careful about logging sensitive data.
        _logger.LogInformation("----- Handling command {CommandName} ({@Command})", requestName, request);
        
        var response = await next();
        
        _logger.LogInformation("----- Command {CommandName} handled - response: {@Response}", requestName, response);
        
        return response;
    }
} 