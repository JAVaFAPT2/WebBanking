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
using Shared.Behaviors;
using StackExchange.Redis;

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

        // Register LoginCommandHandler
        builder.RegisterType<LoginCommandHandler>()
               .As<IRequestHandler<LoginUserCommand, LoginResponseDto>>()
               .InstancePerLifetimeScope();

        // Register PasswordHasher
        builder.RegisterType<PasswordHasher>()
               .As<IPasswordHasher>()
               .InstancePerLifetimeScope();

        // Register TokenService
        builder.Register(c =>
        {
            var config = c.Resolve<IConfiguration>();
            var secretKey = config["JwtSettings:SecretKey"];
            return new TokenService(secretKey);
        }).As<ITokenService>().InstancePerLifetimeScope();

        // KafkaConsumer
        builder.RegisterType<KycVerifiedEventConsumer>()
            .AsSelf()
            .InstancePerLifetimeScope();

        // Register Kafka producer
        builder.Register(c =>
        {
            var config = c.Resolve<IConfiguration>();
            return new ProducerBuilder<Null, string>(new ProducerConfig
            {
                BootstrapServers = config["Kafka:BootstrapServers"]
            }).Build();
        }).As<IProducer<Null, string>>().InstancePerLifetimeScope();

        // Register Redis connection
        builder.Register(c =>
        {
            var config = c.Resolve<IConfiguration>();
            return ConnectionMultiplexer.Connect(config["Redis:ConnectionString"] ?? throw new InvalidOperationException());
        }).As<IConnectionMultiplexer>().SingleInstance();

        // Populate services
        var services = new ServiceCollection();
        builder.Populate(services);

        return builder;
    }
}
