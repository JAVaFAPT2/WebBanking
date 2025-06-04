using Autofac;
using FluentValidation;
using MediatR;
using MediatR.Pipeline;
using NotificationService.Application.Behaviors;
using System.Linq;
using System.Reflection;

namespace NotificationService.Application;

public class ApplicationModule : Autofac.Module
{
    protected override void Load(ContainerBuilder builder)
    {
        // MediatR
        // Register IMediator
        builder.RegisterType<Mediator>()
            .As<IMediator>()
            .InstancePerLifetimeScope();

        // Register MediatR request handlers, notification handlers, and pre/post processors from the current assembly
        builder.RegisterAssemblyTypes(Assembly.GetExecutingAssembly())
            .AsClosedTypesOf(typeof(IRequestHandler<,>))
            .AsImplementedInterfaces()
            .InstancePerDependency();

        builder.RegisterAssemblyTypes(Assembly.GetExecutingAssembly())
            .AsClosedTypesOf(typeof(INotificationHandler<>))
            .AsImplementedInterfaces()
            .InstancePerDependency();

        // Pipeline Behaviors
        // Order is important. They are resolved in the order of registration.
        // LoggingBehavior should typically come before ValidationBehavior if you want to log the request before validation potentially throws.
        builder.RegisterGeneric(typeof(LoggingBehavior<,>)).As(typeof(IPipelineBehavior<,>)).InstancePerDependency();
        builder.RegisterGeneric(typeof(ValidationBehavior<,>)).As(typeof(IPipelineBehavior<,>)).InstancePerDependency();
        
        // FluentValidation: Register all validators from this assembly
        builder.RegisterAssemblyTypes(Assembly.GetExecutingAssembly())
            .Where(t => t.IsClosedTypeOf(typeof(IValidator<>)))
            .AsImplementedInterfaces()
            .InstancePerDependency();

        // Register RequestPreProcessorBehavior and RequestPostProcessorBehavior if you use IRequestPreProcessor or IRequestPostProcessor
        // builder.RegisterGeneric(typeof(RequestPreProcessorBehavior<,>)).As(typeof(IPipelineBehavior<,>));
        // builder.RegisterGeneric(typeof(RequestPostProcessorBehavior<,>)).As(typeof(IPipelineBehavior<,>));

        // Example of how you might register specific pre/post processors if not using the generic behaviors:
        // builder.RegisterAssemblyTypes(typeof(ApplicationModule).Assembly)
        //     .As(type => type.GetInterfaces()
        //         .Where(interfacetype => interfacetype.IsGenericType &&
        //                               (interfacetype.GetGenericTypeDefinition() == typeof(IRequestPreProcessor<>) ||
        //                                interfacetype.GetGenericTypeDefinition() == typeof(IRequestPostProcessor<,>)))
        //         .ToArray());
    }
} 