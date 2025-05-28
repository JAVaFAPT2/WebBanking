using Application.Behaviors;
using Domain.Configuration;
using Domain.Interface;
using HealthChecks.UI.Client;
using Infrastructure.Caching;
using MediatR;
using Microsoft.AspNetCore.Diagnostics.HealthChecks;
using Microsoft.AspNetCore.Server.Kestrel.Core;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;
using Presentation.Middleware;
using Presentation.Services;
using Serilog;
using StackExchange.Redis;
using System.Reflection;
using MediatR.Extensions.FluentValidation.AspNetCore;

var builder = WebApplication.CreateBuilder(args);

// Configure Serilog
Log.Logger = new LoggerConfiguration()
    .ReadFrom.Configuration(builder.Configuration)
    .Enrich.FromLogContext()
    .Enrich.WithCorrelationId()
    .CreateLogger();

builder.Host.UseSerilog();

// Configure Kestrel to listen for gRPC
builder.WebHost.ConfigureKestrel(options =>
{
    // Setup a HTTP/2 endpoint without TLS for development
    options.ListenLocalhost(5001, o => o.Protocols = HttpProtocols.Http2);
});

// Add services to the container
builder.Services.AddGrpc(options =>
{
    options.EnableDetailedErrors = true;
    options.MaxReceiveMessageSize = 6 * 1024 * 1024; // 6 MB
    options.MaxSendMessageSize = 6 * 1024 * 1024; // 6 MB
});
builder.Services.AddGrpcReflection();
builder.Services.AddGrpcHealthChecks();

// Add Health Checks
builder.Services.AddHealthChecks()
    .AddRedis(
        builder.Configuration.GetConnectionString("Redis") ?? "localhost:6379",
        name: "redis",
        tags: new[] { "cache", "infra" })
    .AddSqlServer(
        builder.Configuration.GetConnectionString("Database") ?? throw new InvalidOperationException("Database connection string not found."),
        name: "sql",
        tags: new[] { "db", "infra" });

// Add Health Checks UI
builder.Services.AddHealthChecksUI(options =>
{
    options.SetEvaluationTimeInSeconds(10);
    options.MaximumHistoryEntriesPerEndpoint(60);
    options.SetApiMaxActiveRequests(1);
})
.AddInMemoryStorage();

// Add OpenTelemetry
var otelConfig = builder.Configuration.GetSection("OpenTelemetry").Get<OpenTelemetryConfig>();
builder.Services.AddOpenTelemetry()
    .WithTracing(tracerProviderBuilder =>
        tracerProviderBuilder
            .AddSource(otelConfig?.ActivitySourceName ?? "LoanService.Telemetry")
            .SetResourceBuilder(
                ResourceBuilder.CreateDefault()
                    .AddService(serviceName: otelConfig?.ServiceName ?? "LoanService",
                              serviceVersion: otelConfig?.ServiceVersion ?? "1.0.0"))
            .AddAspNetCoreInstrumentation()
            .AddHttpClientInstrumentation()
            .AddConsoleExporter());

// Add MediatR
builder.Services.AddMediatR(cfg => {
    cfg.RegisterServicesFromAssembly(Assembly.GetExecutingAssembly());
    cfg.RegisterServicesFromAssembly(typeof(ValidationBehavior<,>).Assembly);
});

// Add Pipeline Behaviors
builder.Services.AddTransient(typeof(IPipelineBehavior<,>), typeof(ValidationBehavior<,>));
builder.Services.AddTransient(typeof(IPipelineBehavior<,>), typeof(LoggingBehavior<,>));

// Add Redis Cache
builder.Services.AddSingleton<IConnectionMultiplexer>(sp =>
    ConnectionMultiplexer.Connect(builder.Configuration.GetConnectionString("Redis") ?? "localhost:6379"));
builder.Services.AddSingleton<ICacheService, RedisCacheService>();

// Configure Settings
builder.Services.Configure<LoanServiceSettings>(
    builder.Configuration.GetSection("LoanService"));

var app = builder.Build();

// Configure the HTTP request pipeline
app.UseSerilogRequestLogging(options =>
{
    options.EnrichDiagnosticContext = (diagnosticContext, httpContext) =>
    {
        diagnosticContext.Set("RequestHost", httpContext.Request.Host.Value);
        diagnosticContext.Set("RequestScheme", httpContext.Request.Scheme);
        diagnosticContext.Set("UserAgent", httpContext.Request.Headers["User-Agent"].FirstOrDefault());
    };
});

app.UseMiddleware<RequestResponseLoggingMiddleware>();

app.MapGrpcService<LoanGrpcService>();
app.MapGrpcHealthChecksService();

// Map health check endpoints
app.MapHealthChecks("/health", new HealthCheckOptions
{
    ResponseWriter = UIResponseWriter.WriteHealthCheckUIResponse
});

app.MapHealthChecksUI(options => options.UIPath = "/health-ui");

if (app.Environment.IsDevelopment())
{
    app.MapGrpcReflectionService();
}

app.MapGet("/", () => "Communication with gRPC endpoints must be made through a gRPC client.");

try
{
    Log.Information("Starting LoanService");
    app.Run();
}
catch (Exception ex)
{
    Log.Fatal(ex, "LoanService terminated unexpectedly");
}
finally
{
    Log.CloseAndFlush();
}

public class OpenTelemetryConfig
{
    public string ServiceName { get; set; } = "LoanService";
    public string ServiceVersion { get; set; } = "1.0.0";
    public string ActivitySourceName { get; set; } = "LoanService.Telemetry";
} 