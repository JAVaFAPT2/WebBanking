using System;
using System.Linq;
using System.Reflection;
using Autofac;
using Autofac.Extensions.DependencyInjection;
using Autofac.Features.Variance;
using Domain.Interface;
using Infrastructure.Persistence.DBContext;
using Infrastructure.Persistence.Repository;
using MediatR;
using MediatR.Pipeline;
using Microsoft.Extensions.DependencyInjection;

namespace UserService
{
    public static class ContainerConfig
    {
        public static ContainerBuilder AddGenericHandlers(this ContainerBuilder builder)
        {
            // 1. Enable contravariant resolution for handler interfaces
            builder.RegisterSource(new ContravariantRegistrationSource());

            // 2. Register MediatR core types by scanning its assembly
            builder
                .RegisterAssemblyTypes(typeof(IMediator).GetTypeInfo().Assembly)
                .AsImplementedInterfaces();

            // 3. Explicitly register the Mediator and ServiceFactory delegate
            builder.RegisterType<Mediator>()
                   .As<IMediator>()
                   .InstancePerLifetimeScope();


            // 4. Register pipeline behaviors (open generics)
            builder.RegisterGeneric(typeof(RequestPreProcessorBehavior<,>))
                   .As(typeof(IPipelineBehavior<,>))
                   .InstancePerLifetimeScope();
            builder.RegisterGeneric(typeof(RequestPostProcessorBehavior<,>))
                   .As(typeof(IPipelineBehavior<,>))
                   .InstancePerLifetimeScope();
            builder.RegisterGeneric(typeof(RequestExceptionActionProcessorBehavior<,>))
                   .As(typeof(IPipelineBehavior<,>))
                   .InstancePerLifetimeScope();
            builder.RegisterGeneric(typeof(RequestExceptionProcessorBehavior<,>))
                   .As(typeof(IPipelineBehavior<,>))
                   .InstancePerLifetimeScope();

            // 5. Scan your application assemblies for closed-generic handlers
            var handlerAssemblies = new[]
            {
                Assembly.GetExecutingAssembly()
                // You can add more assemblies containing handlers here
            };

            builder.RegisterAssemblyTypes(handlerAssemblies)
                   .AsClosedTypesOf(typeof(IRequestHandler<,>))
                   .AsImplementedInterfaces()
                   .InstancePerLifetimeScope();

            builder.RegisterAssemblyTypes(handlerAssemblies)
                   .AsClosedTypesOf(typeof(INotificationHandler<>))
                   .AsImplementedInterfaces()
                   .InstancePerLifetimeScope();

            // 6. Register application DbContext and repositories
            builder.RegisterType<ApplicationDbContext>()
                   .AsSelf()
                   .InstancePerLifetimeScope();

            builder.RegisterType<UserRepository>()
                   .As<IUserRepository>()
                   .InstancePerLifetimeScope();
            var services = new ServiceCollection();

            builder.Populate(services);

            return builder;

        }
    }
}
