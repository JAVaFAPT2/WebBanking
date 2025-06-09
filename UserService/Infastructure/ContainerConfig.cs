using System.Reflection;
using Application.CQRS.Commands;
using Application.CQRS.DTO;
using Application.CQRS.Handler;
using Application.CQRS.Validator;
using Autofac;
using Autofac.Extensions.DependencyInjection;
using Autofac.Features.Variance;
using Confluent.Kafka;
using Domain.Interface;
using FluentValidation;
using Infrastructure.EventBus;
using Infrastructure.Persistence.DBContext;
using Infrastructure.Persistence.Repositories;
using Infrastructure.Persistence.Service;
using MediatR;
using MediatR.Pipeline;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Options;
using Shared.Behaviors;
using StackExchange.Redis;
using System;
using System.Linq;

namespace Infrastructure;

public static class ContainerConfig
{
    public static ContainerBuilder AddGenericHandlers(this ContainerBuilder builder)
    {
        builder.RegisterSource(new ContravariantRegistrationSource());

        // Register MediatR
        builder.RegisterAssemblyTypes(typeof(IMediator).GetTypeInfo().Assembly)
               .AsImplementedInterfaces()
               .InstancePerLifetimeScope();
        builder.RegisterType<Mediator>()
               .As<IMediator>()
               .InstancePerLifetimeScope();

        // Register pipeline behaviors
        builder.RegisterGeneric(typeof(RequestPreProcessorBehavior<,>))
               .As(typeof(IPipelineBehavior<,>))
               .InstancePerLifetimeScope();
        builder.RegisterGeneric(typeof(RequestPostProcessorBehavior<,>))
               .As(typeof(IPipelineBehavior<,>))
               .InstancePerLifetimeScope();
        builder.RegisterGeneric(typeof(RequestExceptionProcessorBehavior<,>))
               .As(typeof(IPipelineBehavior<,>))
               .InstancePerLifetimeScope();
        builder.RegisterGeneric(typeof(RequestExceptionActionProcessorBehavior<,>))
               .As(typeof(IPipelineBehavior<,>))
               .InstancePerLifetimeScope();
        builder.RegisterGeneric(typeof(ValidationBehavior<,>))
               .As(typeof(IPipelineBehavior<,>))
               .InstancePerLifetimeScope();

        // Register handlers and validators
        var handlerAssemblies = new[] { typeof(CreateUserCommand).Assembly };
        foreach (var assembly in handlerAssemblies)
        {
            builder.RegisterAssemblyTypes(assembly)
                   .AsClosedTypesOf(typeof(IRequestHandler<,>))
                   .AsImplementedInterfaces()
                   .InstancePerLifetimeScope();
            builder.RegisterAssemblyTypes(assembly)
                   .AsClosedTypesOf(typeof(INotificationHandler<>))
                   .AsImplementedInterfaces()
                   .InstancePerLifetimeScope();
        }

        builder.RegisterType<CreateUserCommandHandler>()
               .As<IRequestHandler<CreateUserCommand, Guid>>()
               .InstancePerLifetimeScope();
        builder.RegisterType<CreateUserCommandValidator>()
               .As<IValidator<CreateUserCommand>>()
               .InstancePerLifetimeScope();

        // Register database context and repositories
        builder.RegisterType<ApplicationDbContext>()
               .AsSelf()
               .InstancePerLifetimeScope();
        builder.RegisterType<UserRepository>()
               .As<IUserRepository>()
               .InstancePerLifetimeScope();
        builder.RegisterType<KycService>()
               .As<IKycService>()
               .InstancePerLifetimeScope();
        builder.RegisterType<KycDocumentRepository>()
               .As<IKycDocumentRepository>()
               .InstancePerLifetimeScope();

        // Register EmailService
        builder.RegisterType<EmailService>()
            .As<IEmailService>()
            .InstancePerLifetimeScope();

        // KafkaConsumer
        builder.RegisterType<KycVerifiedEventConsumer>()
            .AsSelf()
            .InstancePerLifetimeScope();

        // Register Kafka producer with fallback mechanism
        builder.Register(c =>
        {
            var config = c.Resolve<IConfiguration>();
            var bootstrapServers = config["Kafka:BootstrapServers"];
            bool allowLocalFallback = false;
            if (bool.TryParse(config["Kafka:AllowLocalFallback"], out bool result))
            {
                allowLocalFallback = result;
            }
            
            try
            {
                return new ProducerBuilder<Null, string>(new ProducerConfig
                {
                    BootstrapServers = bootstrapServers,
                    // Fix: Set Acks to All for idempotence
                    Acks = Acks.All,
                    MessageSendMaxRetries = 3,
                    RetryBackoffMs = 1000,
                    EnableIdempotence = true
                }).Build();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Warning: Failed to connect to Kafka at {bootstrapServers}. Error: {ex.Message}");
                
                // Return null instead of a dummy producer
                return null;
            }
        }).As<IProducer<Null, string>>().SingleInstance();

        // Register Redis connection with fallback mechanism
        builder.Register(c =>
        {
            var config = c.Resolve<IConfiguration>();
            var redisConnectionString = config["Redis:ConnectionString"];
            bool allowLocalFallback = false;
            if (bool.TryParse(config["Redis:AllowLocalFallback"], out bool result))
            {
                allowLocalFallback = result;
            }
            
            try
            {
                if (string.IsNullOrEmpty(redisConnectionString))
                    throw new InvalidOperationException("Redis connection string is missing");
                
                return ConnectionMultiplexer.Connect(redisConnectionString);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Warning: Failed to connect to Redis at {redisConnectionString}. Error: {ex.Message}");
                
                // Return null instead of a dummy connection multiplexer
                return null;
            }
        }).As<IConnectionMultiplexer>().SingleInstance();

        // Populate services
        var services = new ServiceCollection();
        builder.Populate(services);

        return builder;
    }
}
