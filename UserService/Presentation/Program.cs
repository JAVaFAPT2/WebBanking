using Autofac;
using Autofac.Extensions.DependencyInjection;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Sqlite;
using Microsoft.IdentityModel.Tokens;
using System.Text;
using Infrastructure;
using Infrastructure.Persistence.DBContext;
using Serilog;
using Shared.Middleware;
using Infrastructure.EventBus;

Console.WriteLine("Starting UserService...");

var builder = WebApplication.CreateBuilder(args);


// Fix: Replace the incorrect method call with the correct one for Serilog configuration
builder.Host.UseSerilog((context, config) =>
{
    config.WriteTo.Console()
          .ReadFrom
          .Configuration(
              context.Configuration.GetSection("Serilog")); // Corrected to explicitly get the "Serilog" section
});

// Standard DI registrations
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// EF Core
builder.Services.AddDbContext<ApplicationDbContext>(options =>
{
    var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");
    if (connectionString.Contains("Data Source=") && !connectionString.Contains("Server="))
    {
        // Use SQLite for local development
        options.UseSqlite(connectionString);
    }
    else
    {
        // Use MySQL for production/Docker
        options.UseMySql(connectionString, new MySqlServerVersion(new Version(8, 0, 21)));
    }
});

// Redis Cache
var redisConnectionString = builder.Configuration["Redis:ConnectionString"];
if (!string.IsNullOrEmpty(redisConnectionString))
{
    builder.Services.AddStackExchangeRedisCache(options =>
    {
        options.Configuration = redisConnectionString;
        options.InstanceName = "UserService:";
    });
}

// Keycloak OIDC Authentication
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
builder.Host.ConfigureContainer<ContainerBuilder>(container =>
{
    container.AddGenericHandlers();
});

var app = builder.Build();

// Ensure database is created and migrations are applied
using (var scope = app.Services.CreateScope())
{
    var context = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();
    try
    {
        // Create database if it doesn't exist
        context.Database.EnsureCreated();
        Console.WriteLine("Database created successfully");
    }
    catch (Exception ex)
    {
        Console.WriteLine($"Error creating database: {ex.Message}");
    }
}

// Start Kafka Consumer
// var kycVerifiedEventConsumer = app.Services.GetRequiredService<KycVerifiedEventConsumer>();
// var cts = new CancellationTokenSource();
// _ = Task.Run(() => kycVerifiedEventConsumer.StartConsuming(cts.Token)); // Fire-and-forget


// Middleware pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseMiddleware<ExceptionMiddleware>();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();
