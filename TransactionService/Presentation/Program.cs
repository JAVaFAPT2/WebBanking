using Autofac;
using Autofac.Extensions.DependencyInjection;
using FluentValidation;
using HealthChecks.UI.Client;
using MediatR;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Diagnostics.HealthChecks;
using Microsoft.AspNetCore.Hosting;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Diagnostics.HealthChecks;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging; // Required for ILogger and logging extensions
using System.Reflection;
using TransactionService.Application.Behaviors;
using TransactionService.Application.CQRS.Commands.InitiateTransaction; // For one command/handler to get assembly
using TransactionService.Application.IntegrationEvents.Events; // For PaymentGatewayCallbackEvent
using TransactionService.Domain.Interfaces;
using TransactionService.Infrastructure;
using TransactionService.Infrastructure.Caching;
using TransactionService.Infrastructure.Configuration;
using TransactionService.Infrastructure.EventBus; // For KafkaConsumerService & IEventProducer
using TransactionService.Infrastructure.Persistence;
using TransactionService.Infrastructure.Persistence.Repositories;
using TransactionService.Presentation.Services; // For TransactionGrpcService

var builder = WebApplication.CreateBuilder(args);

// --- Configuration --- 
builder.Configuration.AddJsonFile("appsettings.json", optional: false, reloadOnChange: true)
    .AddJsonFile($"appsettings.{builder.Environment.EnvironmentName}.json", optional: true, reloadOnChange: true)
    .AddEnvironmentVariables();

// Bind KafkaSettings and CacheSettings from configuration
builder.Services.Configure<KafkaSettings>(builder.Configuration.GetSection("KafkaSettings"));
builder.Services.Configure<CacheSettings>(builder.Configuration.GetSection("CacheSettings"));

// --- Logging --- 
builder.Logging.ClearProviders();
builder.Logging.AddConsole();
builder.Logging.AddDebug();
// Add other logging providers if needed (e.g., Serilog, Seq)

// --- Service Registration (Standard .NET Core DI) --- 

// Add MediatR - handlers and validators will be registered by Autofac from assemblies
builder.Services.AddMediatR(cfg => 
    cfg.RegisterServicesFromAssembly(typeof(InitiateTransactionCommand).Assembly) // Application Assembly
);

// Add FluentValidation validators from Application assembly
builder.Services.AddValidatorsFromAssembly(typeof(InitiateTransactionCommandValidator).Assembly);

// Add DbContext
builder.Services.AddDbContext<TransactionDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection"),
        npgsqlOptionsAction: sqlOptions =>
        {
            sqlOptions.MigrationsAssembly(typeof(TransactionDbContext).Assembly.FullName); // Infrastructure assembly
            sqlOptions.EnableRetryOnFailure(maxRetryCount: 5, maxRetryDelay: TimeSpan.FromSeconds(30), errorCodesToAdd: null);
        })
    // .UseSnakeCaseNamingConvention() // Optional: if you prefer snake_case for DB objects
    // .LogTo(Console.WriteLine, LogLevel.Information) // Enable EF Core logging if needed
    // .EnableSensitiveDataLogging() // For development only
    );

// Add Redis Distributed Cache
builder.Services.AddStackExchangeRedisCache(options =>
{
    options.Configuration = builder.Configuration.GetValue<string>("CacheSettings:RedisConnectionString");
    options.InstanceName = builder.Configuration.GetValue<string>("CacheSettings:InstanceName");
});

// Define the mapping of topic names to event types for the Kafka consumer
// The key is the topic name, the value is the .NET Type of the event expected on that topic.
// This needs to be configured based on your inter-service communication contracts.
var eventTypesToTopics = new Dictionary<string, Type>
{
    // Example: if PaymentGatewayCallbackEvent messages are expected on a topic named "payment.gateway.callbacks"
    { "payment.gateway.callbacks", typeof(PaymentGatewayCallbackEvent) },
    // Add other topic-to-event-type mappings here
    // { "another.topic.name", typeof(AnotherIntegrationEvent) }
};
builder.Services.AddSingleton(eventTypesToTopics); // Register the map for KafkaConsumerService

// Register KafkaConsumerService as a HostedService
// It will be started and stopped with the application lifecycle.
builder.Services.AddHostedService<KafkaConsumerService>();

// --- Health Checks --- 
builder.Services.AddHealthChecks()
    .AddNpgSql(
        builder.Configuration.GetConnectionString("DefaultConnection")!,
        name: "postgresql",
        failureStatus: HealthStatus.Degraded,
        tags: new[] { "db", "sql", "postgresql" })
    .AddRedis(
        builder.Configuration.GetValue<string>("CacheSettings:RedisConnectionString")!,
        name: "redis",
        failureStatus: HealthStatus.Degraded,
        tags: new[] { "cache", "redis" })
    .AddKafka(
        new Confluent.Kafka.ProducerConfig { BootstrapServers = builder.Configuration.GetValue<string>("KafkaSettings:BootstrapServers")! },
        name: "kafka-broker",
        failureStatus: HealthStatus.Degraded,
        tags: new[] { "messaging", "kafka" });
        // You can add a more specific topic check for Kafka if needed: .AddKafka(..., topic: "healthcheck-topic")

// Add gRPC services
builder.Services.AddGrpc(options =>
{
    options.EnableDetailedErrors = true;
    // Add interceptors if needed
});
builder.Services.AddGrpcReflection(); // Optional: for gRPC server reflection (e.g. grpcurl)

// Add HttpContextAccessor (often useful)
builder.Services.AddHttpContextAccessor();

// For API controllers if you add any REST endpoints later
// builder.Services.AddControllers();
// builder.Services.AddSwaggerGen(c =>
// {
//    c.SwaggerDoc("v1", new() { Title = "TransactionService API", Version = "v1" });
// });

// --- Autofac Configuration --- 
builder.Host.UseServiceProviderFactory(new AutofacServiceProviderFactory());
builder.Host.ConfigureContainer<ContainerBuilder>(autofacBuilder =>
{
    // Register services from Infrastructure layer using the ContainerConfig
    autofacBuilder.AddInfrastructureServices(builder.Configuration);
});

// --- Application Pipeline --- 
var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseDeveloperExceptionPage();
    // app.UseSwagger();
    // app.UseSwaggerUI(c => c.SwaggerEndpoint("/swagger/v1/swagger.json", "TransactionService API v1"));
    app.MapGrpcReflectionService(); // Expose gRPC reflection in dev
}

// app.UseHttpsRedirection(); // If using HTTPS
// app.UseRouting();
// app.UseAuthentication(); // If authentication is added
// app.UseAuthorization(); // If authorization is added

// Map gRPC services
app.MapGrpcService<TransactionGrpcService>();

// Map a default HTTP endpoint (e.g., for health checks or basic info)
app.MapGet("/", () => $"TransactionService is running. Environment: {app.Environment.EnvironmentName}");

// Map Health Checks endpoint
app.MapHealthChecks("/health", new HealthCheckOptions
{
    Predicate = _ => true, // Include all health checks
    ResponseWriter = UIResponseWriter.WriteHealthCheckUIResponse // Pretty JSON response
});

// Optional: Detailed health check for dependencies (could be a separate endpoint or filtered)
app.MapHealthChecks("/health/ready", new HealthCheckOptions
{
    Predicate = healthCheck => healthCheck.Tags.Contains("db") || healthCheck.Tags.Contains("cache") || healthCheck.Tags.Contains("messaging"),
    ResponseWriter = UIResponseWriter.WriteHealthCheckUIResponse,
    ResultStatusCodes = 
    {
        [HealthStatus.Healthy] = StatusCodes.Status200OK,
        [HealthStatus.Degraded] = StatusCodes.Status200OK, // Still consider service operational if a dependency is degraded
        [HealthStatus.Unhealthy] = StatusCodes.Status503ServiceUnavailable
    }
});

app.MapHealthChecks("/health/live", new HealthCheckOptions
{
    Predicate = _ => false, // Only basic liveness, no dependencies checked for this endpoint
    ResponseWriter = UIResponseWriter.WriteHealthCheckUIResponse
});

// --- Database Migrations (Optional: Run on startup for dev/testing) ---
/*
using (var scope = app.Services.CreateScope())
{
    var services = scope.ServiceProvider;
    try
    {
        var context = services.GetRequiredService<TransactionDbContext>();
        context.Database.Migrate(); // Applies pending migrations
        // Seed data if needed
    }
    catch (Exception ex)
    {
        var logger = services.GetRequiredService<ILogger<Program>>();
        logger.LogError(ex, "An error occurred while migrating or seeding the database.");
    }
}
*/

app.Run(); 