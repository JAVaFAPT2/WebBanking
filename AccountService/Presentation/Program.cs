using Application.CQRS.Commands.CreateAccount;
using Application.CQRS.Queries.GetAccount;
using Autofac;
using Autofac.Extensions.DependencyInjection;
using Infrastructure;
using Infrastructure.Persistence;
using Microsoft.AspNetCore.Server.Kestrel.Core;
using Microsoft.EntityFrameworkCore;
using Serilog;
using Shared.Configuration;
using Microsoft.AspNetCore.Authentication.JwtBearer;

var builder = WebApplication.CreateBuilder(args);

// Configure settings
var settings = builder.Configuration.GetSection("AccountServiceSettings").Get<AccountServiceSettings>()
    ?? throw new InvalidOperationException("AccountServiceSettings section is missing in configuration");
builder.Services.Configure<AccountServiceSettings>(builder.Configuration.GetSection("AccountServiceSettings"));

// Configure Serilog
builder.Host.UseSerilog((context, config) =>
{
    config.WriteTo.Console()
          .ReadFrom
          .Configuration(context.Configuration.GetSection("Serilog"));
});

// Configure gRPC
builder.Services.AddGrpc();
builder.WebHost.ConfigureKestrel(options =>
{
    options.ListenLocalhost(5001, o => o.Protocols = HttpProtocols.Http2);
});

// Redis Cache
builder.Services.AddStackExchangeRedisCache(options =>
{
    options.Configuration = settings.Redis.ConnectionString;
    options.InstanceName = settings.Redis.InstanceName;
});

// Add infrastructure services
builder.Services.AddInfrastructure(builder.Configuration);

// Add AutoMapper
builder.Services.AddAutoMapper(typeof(AccountMappingProfile).Assembly);

// Add MediatR
builder.Services.AddMediatR(cfg => {
    cfg.RegisterServicesFromAssembly(typeof(CreateAccountCommand).Assembly);
});

// Add Health Checks
builder.Services.AddHealthChecks()
    .AddSqlServer(settings.Database.ConnectionString)
    .AddRedis(settings.Redis.ConnectionString)
    .AddKafka(new Confluent.Kafka.ProducerConfig
    {
        BootstrapServers = settings.Kafka.BootstrapServers
    });

// Add Authentication and Authorization
builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.Authority = builder.Configuration["Keycloak:Authority"];
    options.Audience = builder.Configuration["Keycloak:Audience"];
    options.RequireHttpsMetadata = builder.Configuration.GetValue<bool>("Keycloak:RequireHttpsMetadata");
});
builder.Services.AddAuthorization();

// Switch to Autofac
builder.Host.UseServiceProviderFactory(new AutofacServiceProviderFactory());
builder.Host.ConfigureContainer<ContainerBuilder>(containerBuilder =>
{
    containerBuilder.AddInfrastructure();
});

builder.Services.AddGrpcReflection();

var app = builder.Build();

// Apply migrations at startup
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<AccountDbContext>();
    if (!db.Database.CanConnect())
    {
        db.Database.Migrate();
    }
}

// Use Authentication and Authorization
app.UseAuthentication();
app.UseAuthorization();

// Map gRPC service
app.MapGrpcService<Presentation.Services.AccountGrpcService>();

if (app.Environment.IsDevelopment())
{
        app.MapGrpcReflectionService();
}

app.Run();