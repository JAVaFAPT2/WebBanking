using Autofac;
using Microsoft.Extensions.Configuration;
using NotificationService.Application;
using NotificationService.Domain.Configuration;
using NotificationService.Infrastructure;

namespace NotificationService.Presentation;

public static class ContainerConfig
{
    public class PresentationLayerModule : Autofac.Module
    {
        private readonly IConfiguration _configuration;

        public PresentationLayerModule(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        protected override void Load(ContainerBuilder builder)
        {
            // Register other Autofac modules
            builder.RegisterModule<ApplicationModule>();
            builder.RegisterModule<InfrastructureModule>();

            // Register NotificationServiceSettings
            var notificationSettings = new NotificationServiceSettings();
            _configuration.GetSection("NotificationService").Bind(notificationSettings);
            builder.RegisterInstance(notificationSettings).AsSelf().SingleInstance();
            builder.RegisterInstance(Microsoft.Extensions.Options.Options.Create(notificationSettings)).As<Microsoft.Extensions.Options.IOptions<NotificationServiceSettings>>().SingleInstance();
        }
    }
} 