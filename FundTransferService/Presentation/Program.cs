using Autofac;
using Autofac.Extensions.DependencyInjection;
using FundTransferService.Application.Behaviors;
using FundTransferService.Application.CQRS.Commands.InitiateTransfer; // For Assembly scanning
using FundTransferService.Domain.Configuration;
using FundTransferService.Domain.Interfaces;
using FundTransferService.Infrastructure.Caching;
using FundTransferService.Infrastructure.Persistence;
using FundTransferService.Infrastructure.Repositories;
using FundTransferService.Presentation.Configuration; // For ContainerConfig
using FundTransferService.Presentation.Services;
using FluentValidation;
using MediatR;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Authorization;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;
using StackExchange.Redis;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

// --- Start Autofac Configuration ---
builder.Host.UseServiceProviderFactory(new AutofacServiceProviderFactory());
// --- End Autofac Configuration ---

// Add services to the container using default .NET DI (these can be picked up by Autofac or registered directly in modules)

// Configuration
builder.Services.Configure<FundTransferServiceSettings>(builder.Configuration.GetSection("FundTransferServiceSettings"));
builder.Services.Configure<JwtSettings>(builder.Configuration.GetSection("JwtSettings"));
builder.Services.Configure<KafkaSettings>(builder.Configuration.GetSection("Kafka"));
var serviceSettings = builder.Configuration.GetSection("FundTransferServiceSettings").Get<FundTransferServiceSettings>() ?? new FundTransferServiceSettings();
var jwtSettings = builder.Configuration.GetSection("JwtSettings").Get<JwtSettings>() ?? new JwtSettings();

// Authentication & Authorization
builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.Authority = jwtSettings.Authority;
    options.Audience = jwtSettings.Audience;
    options.RequireHttpsMetadata = jwtSettings.RequireHttpsMetadata; // Usually true in production
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer = jwtSettings.ValidateIssuer,
        ValidateAudience = jwtSettings.ValidateAudience,
        ValidateLifetime = jwtSettings.ValidateLifetime,
        ValidateIssuerSigningKey = jwtSettings.ValidateIssuerSigningKey,
        // ValidIssuer = jwtSettings.Issuer, // Only if not using Authority/discovery
        // ValidAudience = jwtSettings.Audience,
        // IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtSettings.IssuerSigningKey ?? "")) // Only for symmetric keys
    };
    // If you need to handle gRPC specific authentication events or context population:
    options.Events = new JwtBearerEvents
    {
        OnAuthenticationFailed = context =>
        {
            // Log detailed authentication failure
            var logger = context.HttpContext.RequestServices.GetRequiredService<ILogger<Program>>();
            logger.LogError(context.Exception, "JWT Authentication failed.");
            return Task.CompletedTask;
        },
        OnTokenValidated = context =>
        {
             var logger = context.HttpContext.RequestServices.GetRequiredService<ILogger<Program>>();
            logger.LogInformation("JWT Token validated for user: {UserPrincipalName}", context.Principal?.Identity?.Name ?? "(unknown)");
            return Task.CompletedTask;
        }
    };
});

builder.Services.AddAuthorization(options =>
{
    // Example: Define a policy for fund transfers if needed
    // options.AddPolicy("CanInitiateTransfer", policy => 
    //     policy.RequireAuthenticatedUser()
    //           .RequireClaim("scope", "fundtransfer.initiate")); // Or other relevant claims/roles
    options.DefaultPolicy = new AuthorizationPolicyBuilder()
        .RequireAuthenticatedUser()
        .Build();
});

// MediatR - Core services
builder.Services.AddMediatR(cfg => 
    cfg.RegisterServicesFromAssembly(typeof(InitiateFundTransferCommand).Assembly));

// FluentValidation - Core services for validators (to be resolved by ValidationBehavior, which itself can be an Autofac component)
builder.Services.AddValidatorsFromAssembly(typeof(InitiateFundTransferCommandValidator).Assembly);
// The IPipelineBehavior for Validation (ValidationBehavior) will be registered via Autofac module.

// Database
builder.Services.AddDbContext<FundTransferDbContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection"),
        sqlServerOptionsAction: sqlOptions =>
        {
            sqlOptions.EnableRetryOnFailure(
                maxRetryCount: 5,
                maxRetryDelay: TimeSpan.FromSeconds(30),
                errorNumbersToAdd: null);
        }));

// Repositories
builder.Services.AddScoped<IFundTransferRepository, FundTransferRepository>();

// Caching (Redis)
var redisEnabled = builder.Configuration.GetValue<bool>("Redis:Enabled", true); // Make it configurable
var redisConnectionString = builder.Configuration.GetConnectionString("RedisConnection");
var useInMemoryCacheFallback = false;

if (redisEnabled)
{
    if (!string.IsNullOrEmpty(redisConnectionString))
    {
        builder.Services.AddSingleton<IConnectionMultiplexer>(ConnectionMultiplexer.Connect(redisConnectionString));
        builder.Services.AddSingleton<ICacheService, RedisCacheService>();
    }
    else
    {
        // Fallback to in-memory cache, log warning after app is built
        builder.Services.AddSingleton<ICacheService, InMemoryCacheService>();
        useInMemoryCacheFallback = true;
    }
}
else
{
    builder.Services.AddSingleton<ICacheService, InMemoryCacheService>(); // Example fallback if Redis is disabled
}

// gRPC services
builder.Services.AddGrpc(options =>
{
    options.EnableDetailedErrors = true;
});
builder.Services.AddGrpcReflection();

// Swagger/OpenAPI (if exposing HTTP/JSON alongside gRPC)
// builder.Services.AddControllers();
// builder.Services.AddEndpointsApiExplorer();
// builder.Services.AddSwaggerGen();

// OpenTelemetry
var openTelemetrySettings = builder.Configuration.GetSection("OpenTelemetry").Get<OpenTelemetryConfig>() ?? new OpenTelemetryConfig();
builder.Services.AddOpenTelemetry()
    .ConfigureResource(resource => resource.AddService(
        serviceName: openTelemetrySettings.ServiceName,
        serviceVersion: openTelemetrySettings.ServiceVersion))
    .WithTracing(tracing => tracing
        .AddAspNetCoreInstrumentation()
        .AddHttpClientInstrumentation()
        .AddGrpcClientInstrumentation()
        // .AddEntityFrameworkCoreInstrumentation(opt => opt.SetDbStatementForText = true) // If EF Core instrumentation is desired
        .AddSource(openTelemetrySettings.ActivitySourceName)
        .AddOtlpExporter(otlpOptions =>
        {
            otlpOptions.Endpoint = new Uri(builder.Configuration["OpenTelemetry:OtlpExporterEndpoint"] ?? "http://localhost:4317");
        })
        .AddConsoleExporter());

// --- Start Autofac Container Configuration ---
builder.Host.ConfigureContainer<ContainerBuilder>((context, containerBuilder) =>
{
    ContainerConfig.Configure(containerBuilder, context.Configuration); 
});
// --- End Autofac Container Configuration ---

var app = builder.Build();

// Log warning if Redis was enabled but connection string is missing
if (useInMemoryCacheFallback)
{
    var logger = app.Services.GetRequiredService<ILogger<Program>>();
    logger.LogWarning("Redis connection string not found, falling back to in-memory cache.");
}

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    // app.UseSwagger();
    // app.UseSwaggerUI();
    app.MapGrpcReflectionService(); // Enable gRPC reflection for development
}

// app.UseHttpsRedirection(); // Consider if HTTPS redirection is needed

app.UseRouting(); // Must come before UseAuthentication and UseAuthorization

app.UseAuthentication();
app.UseAuthorization();

app.MapGrpcService<FundTransferGrpcService>();
app.MapGet("/", () => "FundTransferService is running. Communications with gRPC endpoints.").RequireAuthorization(); // Secure the health check too

// Apply database migrations (optional, can be done out of band)
// using (var scope = app.Services.CreateScope())
// {
//     var dbContext = scope.ServiceProvider.GetRequiredService<FundTransferDbContext>();
//     dbContext.Database.Migrate();
// }

app.Run();

// Helper class for OpenTelemetry configuration (can be in a separate file)
public class OpenTelemetryConfig
{
    public string ServiceName { get; set; } = "FundTransferService";
    public string ServiceVersion { get; set; } = "1.0.0";
    public string ActivitySourceName { get; set; } = "FundTransferService.Telemetry";
}

// Placeholder for InMemoryCacheService if Redis is not available/configured
// This should ideally be moved to Infrastructure/Caching and registered by Autofac module
// If kept here, ensure its ILogger dependency can be resolved if Autofac creates it.
public class InMemoryCacheService : ICacheService
{
    private readonly Dictionary<string, (object value, DateTimeOffset? expiry)> _cache = new();
    private readonly ILogger<InMemoryCacheService> _logger;

    public InMemoryCacheService(ILogger<InMemoryCacheService> logger) {_logger = logger;}

    public Task<T?> GetAsync<T>(string key, CancellationToken cancellationToken = default)
    {
        if (_cache.TryGetValue(key, out var item))
        {
            if (item.expiry == null || item.expiry > DateTimeOffset.UtcNow)
            {
                _logger.LogDebug("InMemoryCache hit for key: {Key}", key);
                return Task.FromResult((T?)item.value);
            }
            _logger.LogDebug("InMemoryCache expired for key: {Key}", key);
            _cache.Remove(key); // Expired
        }
        _logger.LogDebug("InMemoryCache miss for key: {Key}", key);
        return Task.FromResult(default(T));
    }

    public Task SetAsync<T>(string key, T value, TimeSpan? expiry = null, CancellationToken cancellationToken = default)
    {
        DateTimeOffset? absoluteExpiry = expiry.HasValue ? DateTimeOffset.UtcNow.Add(expiry.Value) : null;
        _cache[key] = (value!, absoluteExpiry);
        _logger.LogDebug("InMemoryCache set key: {Key}", key);
        return Task.CompletedTask;
    }

    public Task RemoveAsync(string key, CancellationToken cancellationToken = default)
    {
        _cache.Remove(key);
        _logger.LogDebug("InMemoryCache removed key: {Key}", key);
        return Task.CompletedTask;
    }

    public Task<bool> ExistsAsync(string key, CancellationToken cancellationToken = default)
    {
        if (_cache.TryGetValue(key, out var item))
        {
            if (item.expiry == null || item.expiry > DateTimeOffset.UtcNow)
            {
                return Task.FromResult(true);
            }
            _cache.Remove(key); // Expired
        }
        return Task.FromResult(false);
    }
}

// Helper class for JWT Settings (can be in a separate file)
public class JwtSettings
{
    public string? Authority { get; set; }
    public string? Audience { get; set; }
    public bool RequireHttpsMetadata { get; set; } = true;
    public bool ValidateIssuer { get; set; } = true;
    public bool ValidateAudience { get; set; } = true;
    public bool ValidateLifetime { get; set; } = true;
    public bool ValidateIssuerSigningKey { get; set; } = true;
    public string? Issuer { get; set; } // For manual validation if Authority is not used
    public string? IssuerSigningKey { get; set; } // For symmetric keys, if Authority is not used
} 