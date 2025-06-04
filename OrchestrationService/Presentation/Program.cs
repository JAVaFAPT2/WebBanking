using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using OrchestrationService.Application.EventHandlers;
using OrchestrationService.Application.Models.Events;
using OrchestrationService.Application.Services;
using OrchestrationService.Domain.Interfaces;
using OrchestrationService.Domain.Models;
using OrchestrationService.Infrastructure.Configuration;
using OrchestrationService.Infrastructure.Messaging;
using OrchestrationService.Infrastructure.Repositories;
using System.Threading.Tasks;
using System;
using Microsoft.AspNetCore.Authentication.JwtBearer;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Configure settings
builder.Services.Configure<KafkaSettings>(builder.Configuration.GetSection("Kafka"));

// Register MediatR
builder.Services.AddMediatR(cfg => cfg.RegisterServicesFromAssembly(typeof(FundTransferOrchestrator).Assembly));

// Register services
builder.Services.AddSingleton<IMessageBroker, KafkaMessageBroker>();
builder.Services.AddSingleton<ISagaRepository<FundTransferSaga>, InMemorySagaRepository<FundTransferSaga>>();
builder.Services.AddSingleton<FundTransferOrchestrator>();
builder.Services.AddSingleton<AccountEventHandlers>();
builder.Services.AddSingleton<NotificationEventHandlers>();

// Configure CORS
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

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

var app = builder.Build();

// Configure the HTTP request pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.UseAuthentication();
app.UseAuthorization();
app.UseCors();
app.MapControllers();

// Set up message subscriptions
await SetupMessageSubscriptionsAsync(app.Services);

app.Run();

// Helper method to set up message subscriptions
async Task SetupMessageSubscriptionsAsync(IServiceProvider serviceProvider)
{
    try
    {
        var messageBroker = serviceProvider.GetRequiredService<IMessageBroker>();
        var accountEventHandlers = serviceProvider.GetRequiredService<AccountEventHandlers>();
        var notificationEventHandlers = serviceProvider.GetRequiredService<NotificationEventHandlers>();
        var kafkaSettings = serviceProvider.GetRequiredService<Microsoft.Extensions.Options.IOptions<KafkaSettings>>().Value;

        // Account service events
        await messageBroker.SubscribeAsync<AccountDebitedEvent>(
            kafkaSettings.AccountEventTopic,
            kafkaSettings.GroupId,
            accountEventHandlers.HandleAccountDebitedEventAsync);

        await messageBroker.SubscribeAsync<AccountCreditedEvent>(
            kafkaSettings.AccountEventTopic,
            kafkaSettings.GroupId,
            accountEventHandlers.HandleAccountCreditedEventAsync);

        await messageBroker.SubscribeAsync<AccountDebitFailedEvent>(
            kafkaSettings.AccountEventTopic,
            kafkaSettings.GroupId,
            accountEventHandlers.HandleAccountDebitFailedEventAsync);

        await messageBroker.SubscribeAsync<AccountCreditFailedEvent>(
            kafkaSettings.AccountEventTopic,
            kafkaSettings.GroupId,
            accountEventHandlers.HandleAccountCreditFailedEventAsync);

        await messageBroker.SubscribeAsync<AccountDebitCompensatedEvent>(
            kafkaSettings.AccountEventTopic,
            kafkaSettings.GroupId,
            accountEventHandlers.HandleAccountDebitCompensatedEventAsync);

        // Notification service events
        await messageBroker.SubscribeAsync<NotificationSentEvent>(
            kafkaSettings.NotificationEventTopic,
            kafkaSettings.GroupId,
            notificationEventHandlers.HandleNotificationSentEventAsync);

        await messageBroker.SubscribeAsync<NotificationFailedEvent>(
            kafkaSettings.NotificationEventTopic,
            kafkaSettings.GroupId,
            notificationEventHandlers.HandleNotificationFailedEventAsync);

        Console.WriteLine("Message subscriptions set up successfully");
    }
    catch (Exception ex)
    {
        Console.WriteLine($"Error setting up message subscriptions: {ex.Message}");
    }
}
