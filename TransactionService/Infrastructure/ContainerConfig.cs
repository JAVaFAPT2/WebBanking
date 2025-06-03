using Autofac;
using MediatR;
using System.Reflection;
using TransactionService.Application.Behaviors; // For pipeline behaviors
using TransactionService.Application.IntegrationEvents.Handlers; // For IIntegrationEventHandler
using TransactionService.Domain.Interfaces;
using TransactionService.Infrastructure.Caching;
using TransactionService.Infrastructure.EventBus;
using TransactionService.Infrastructure.Persistence.Repositories;

namespace TransactionService.Infrastructure;

public static class ContainerConfig
{
    public static ContainerBuilder AddInfrastructureServices(this ContainerBuilder builder, Microsoft.Extensions.Configuration.IConfiguration configuration)
    {
        // Register MediatR
        builder.RegisterAssemblyTypes(typeof(IMediator).GetTypeInfo().Assembly)
            .AsImplementedInterfaces();

        // Register all handlers (commands, queries, events) from the Application assembly
        var appAssembly = Assembly.Load("TransactionService.Application"); // Or use typeof(SomeAppClass).Assembly
        builder.RegisterAssemblyTypes(appAssembly)
            .AsClosedTypesOf(typeof(IRequestHandler<,>))
            .AsImplementedInterfaces();
        builder.RegisterAssemblyTypes(appAssembly)
            .AsClosedTypesOf(typeof(INotificationHandler<>))
            .AsImplementedInterfaces();

        // Register Integration Event Handlers from Application assembly
        builder.RegisterAssemblyTypes(appAssembly)
            .AsClosedTypesOf(typeof(IIntegrationEventHandler<>))
            .AsImplementedInterfaces()
            .InstancePerLifetimeScope(); // Handlers might depend on scoped services like DbContext via MediatR

        // Register MediatR pipeline behaviors
        // Order matters: Idempotency -> Logging -> Validation -> (optional: UnitOfWork/Transaction)
        // Idempotency should generally come early to prevent re-processing.
        builder.RegisterGeneric(typeof(IdempotencyBehavior<,>)).As(typeof(IPipelineBehavior<,>));
        builder.RegisterGeneric(typeof(LoggingBehavior<,>)).As(typeof(IPipelineBehavior<,>));
        builder.RegisterGeneric(typeof(ValidationBehavior<,>)).As(typeof(IPipelineBehavior<,>));

        // Register repositories
        builder.RegisterType<TransactionRepository>().As<ITransactionRepository>().InstancePerLifetimeScope();

        // Register Caching Service
        builder.RegisterType<RedisCacheService>().As<ICacheService>().InstancePerLifetimeScope();

        // Register Kafka Producer Service
        // Ensure KafkaSettings are configured in appsettings.json and bound in Program.cs
        builder.RegisterType<KafkaProducerService>().As<IEventProducer>().SingleInstance(); // Kafka producer is often a singleton
        
        // KafkaConsumerService and its eventTypesToTopics map will be registered in Presentation/Program.cs

        return builder;
    }
} 