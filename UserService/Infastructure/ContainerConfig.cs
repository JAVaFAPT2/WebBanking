using Application.CQRS.Commands;
using Application.CQRS.DTO;
using Application.CQRS.Handler.QueriesH;
using Application.CQRS.Handler;
using Application.CQRS.Queries;
using Autofac;
using Autofac.Extensions.DependencyInjection;
using Autofac.Features.Variance;
using Domain.Interface;
using Infrastructure.Persistence.DBContext;
using Infrastructure.Persistence.Repositories;
using MediatR;
using MediatR.Pipeline;
using Microsoft.Extensions.DependencyInjection;
using System.Reflection;

namespace UserService.Infrastructure;

public static class ContainerConfig
{
    public static ContainerBuilder AddGenericHandlers(this ContainerBuilder builder)
    {
        // Enable contravariant resolution for notification handlers
        builder.RegisterSource(new ContravariantRegistrationSource());

        // MediatR core registrations
        builder.RegisterType<Mediator>()
               .As<IMediator>()
               .InstancePerLifetimeScope();

        
        var handlerAssemblies = new[]
             {
                Assembly.GetExecutingAssembly()
            };

        // Pipeline behaviors
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


        // Explicitly register command handlers
        builder.RegisterType<CreateUserCommandHandler>()
               .As<IRequestHandler<CreateUserCommand, Guid>>()
               .InstancePerLifetimeScope();
        builder.RegisterType<UpdateUserCommandHandler>()
               .As<IRequestHandler<UpdateUserCommand, Unit>>()
               .InstancePerLifetimeScope();
        builder.RegisterType<DeleteUserCommandHandler>()
               .As<IRequestHandler<DeleteUserCommand, Unit>>()
               .InstancePerLifetimeScope();

        // Explicitly register query handlers
        builder.RegisterType<GetUserByIdQueryHandler>()
               .As<IRequestHandler<GetUserByIdQuery, UserDto>>()
               .InstancePerLifetimeScope();
        builder.RegisterType<GetAllUsersQueryHandler>()
               .As<IRequestHandler<GetAllUsersQuery, IEnumerable<UserDto>>>()
               .InstancePerLifetimeScope();
        builder.RegisterType<GetUserByEmailQueryHandler>()
               .As<IRequestHandler<GetUserByEmailQuery, UserDto>>()
               .InstancePerLifetimeScope();

        // EF Core DbContext & Repositories
        builder.RegisterType<ApplicationDbContext>()
               .AsSelf()
               .InstancePerLifetimeScope();

        builder.RegisterType<UserRepository>()
               .As<IUserRepository>()
               .InstancePerLifetimeScope();

        // Populate ASP.NET Core services
        var services = new ServiceCollection();
        builder.Populate(services);

        return builder;
    }
}
