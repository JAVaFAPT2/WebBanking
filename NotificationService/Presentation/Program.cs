using Autofac;
using Autofac.Extensions.DependencyInjection;
using NotificationService.Presentation;
using NotificationService.Presentation.Services;

var builder = WebApplication.CreateBuilder(args);

// Configure Autofac
builder.Host.UseServiceProviderFactory(new AutofacServiceProviderFactory());
builder.Host.ConfigureContainer<ContainerBuilder>(containerBuilder =>
{
    containerBuilder.RegisterModule(new ContainerConfig.PresentationLayerModule(builder.Configuration));
});

// Add services to the container that are managed by ASP.NET Core DI (e.g., gRPC, logging)
builder.Services.AddGrpc();
builder.Services.AddLogging(); // Ensures ILogger is available for Autofac to inject where needed

// Application and Infrastructure services are registered via Autofac modules.

var app = builder.Build();

// Configure the HTTP request pipeline.
app.MapGrpcService<NotificationGrpcService>();
app.MapGet("/", () => "Communication with gRPC endpoints must be made through a gRPC client. To learn how to create a client, visit: https://go.microsoft.com/fwlink/?linkid=2086909");

// TODO: Add Health Checks
// app.MapHealthChecks("/health");

app.Run(); 