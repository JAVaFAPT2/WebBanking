using Autofac;
using Autofac.Extensions.DependencyInjection;
using Infrastructure;
using Microsoft.AspNetCore.Server.Kestrel.Core;
using Microsoft.Extensions.Caching.StackExchangeRedis;
using Serilog;

var builder = WebApplication.CreateBuilder(args);

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
    options.ListenLocalhost(5003, o => o.Protocols = HttpProtocols.Http2);
});

// Redis Cache
builder.Services.AddStackExchangeRedisCache(options =>
{
    options.Configuration = builder.Configuration["Redis:ConnectionString"];
});

// Switch to Autofac
builder.Host.UseServiceProviderFactory(new AutofacServiceProviderFactory());
builder.Host.ConfigureContainer<ContainerBuilder>(builder => builder.AddGenericHandlers());

var app = builder.Build();

// Configure the HTTP request pipeline.
app.MapGrpcService<CardService.Services.CardGrpcService>();

app.Run();