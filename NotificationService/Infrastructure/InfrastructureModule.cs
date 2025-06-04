using Autofac;
using NotificationService.Domain.Interfaces;
using NotificationService.Infrastructure.Persistence.Repositories;
using NotificationService.Infrastructure.Services;

namespace NotificationService.Infrastructure;

public class InfrastructureModule : Autofac.Module
{
    protected override void Load(ContainerBuilder builder)
    {
        // Register Repositories
        builder.RegisterType<InMemoryNotificationLogRepository>()
            .As<INotificationLogRepository>()
            .SingleInstance(); // InMemory is a good candidate for Singleton

        // Register Notification Providers (Placeholders)
        builder.RegisterType<PlaceholderEmailProvider>()
            .As<IEmailProvider>()
            .InstancePerLifetimeScope();

        builder.RegisterType<PlaceholderSmsProvider>()
            .As<ISmsProvider>()
            .InstancePerLifetimeScope();

        builder.RegisterType<PlaceholderPushProvider>()
            .As<IPushProvider>()
            .InstancePerLifetimeScope();

        // Note: NotificationServiceSettings registration will be handled
        // in the Presentation layer's ContainerConfig where IConfiguration is available,
        // allowing for proper binding from appsettings.json.
        // If specific infrastructure components needed IOptions<NotificationServiceSettings> directly,
        // they would typically be configured and registered in the main composition root (Presentation layer).
    }
} 