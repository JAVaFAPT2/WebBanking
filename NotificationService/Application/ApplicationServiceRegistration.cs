using FluentValidation;
using MediatR;
using Microsoft.Extensions.DependencyInjection;
using NotificationService.Application.Behaviors;
using System.Reflection;

namespace NotificationService.Application
{
    public static class ApplicationServiceRegistration
    {
        public static IServiceCollection AddApplicationServices(this IServiceCollection services)
        {
            // Register MediatR
            // This scans the calling assembly (Application) for handlers, requests, notifications.
            services.AddMediatR(cfg => cfg.RegisterServicesFromAssembly(Assembly.GetExecutingAssembly()));

            // Register FluentValidation validators
            // This scans the calling assembly (Application) for types inheriting from AbstractValidator.
            services.AddValidatorsFromAssembly(Assembly.GetExecutingAssembly());

            // Register MediatR pipeline behaviors.
            // Order can be important. Validation usually comes before handling the request.
            // Logging can be at the beginning or end of the pipeline.
            services.AddTransient(typeof(IPipelineBehavior<,>), typeof(LoggingBehavior<,>));
            services.AddTransient(typeof(IPipelineBehavior<,>), typeof(ValidationBehavior<,>));
            
            // If AutoMapper is used, you might want to add its registration here:
            // services.AddAutoMapper(Assembly.GetExecutingAssembly());

            return services;
        }
    }
} 