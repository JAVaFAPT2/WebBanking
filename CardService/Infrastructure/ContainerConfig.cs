using System.Reflection;
using Application.Behaviors;
using Application.CQRS.Commands.IssueCard;
using Autofac;
using Autofac.Extensions.DependencyInjection;
using Autofac.Features.Variance;
using Domain.Interface;
using Domain.Models;
using FluentValidation;
using Infrastructure.Caching;
using Infrastructure.EventBus;
using Infrastructure.Services;
using MediatR;
using MediatR.Pipeline;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
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
        var handlerAssemblies = new[] { typeof(IssueCardCommand).Assembly };
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
            builder.RegisterAssemblyTypes(assembly)
                   .AsClosedTypesOf(typeof(IValidator<>))
                   .AsImplementedInterfaces()
                   .InstancePerLifetimeScope();
        }

        // Register card service specific components
        builder.RegisterType<CardNumberGenerator>()
               .As<ICardNumberGenerator>()
               .InstancePerLifetimeScope();

        // Register Redis cache service
        builder.Register(c =>
        {
            var config = c.Resolve<IConfiguration>();
            return ConnectionMultiplexer.Connect(config["Redis:ConnectionString"] ?? throw new InvalidOperationException());
        }).As<IConnectionMultiplexer>().SingleInstance();

        builder.RegisterType<RedisCacheService>()
               .As<ICacheService>()
               .InstancePerLifetimeScope();

        // Register event handlers
        builder.RegisterType<AccountEventHandler>()
               .AsImplementedInterfaces()
               .InstancePerLifetimeScope();

        // Populate services
        var services = new ServiceCollection();
        builder.Populate(services);

        return builder;
    }
} 