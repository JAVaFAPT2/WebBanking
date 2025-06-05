using Autofac;
using Autofac.Extensions.DependencyInjection;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.Text;
using Infrastructure;
using Infrastructure.Persistence.DBContext;
using Serilog;
using Shared.Middleware;
using Infrastructure.EventBus;


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
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection")));

// Redis Cache
builder.Services.AddStackExchangeRedisCache(options =>
    options.Configuration = builder.Configuration["Redis:ConnectionString"]);

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


// Start Kafka Consumer
var kycVerifiedEventConsumer = app.Services.GetRequiredService<KycVerifiedEventConsumer>();

var cts = new CancellationTokenSource();
await Task.Run(() => kycVerifiedEventConsumer.StartConsuming(cts.Token));


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
