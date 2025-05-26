using Application.CQRS.Commands.BlockAccount;
using Application.CQRS.Commands.CloseAccount;
using Application.CQRS.Commands.CreateAccount;
using Application.CQRS.Commands.UpdateBalance;
using Application.CQRS.Queries.GetAccount;
using Application.CQRS.Queries.GetUserAccounts;
using Autofac;
using Domain.Interface;
using Infrastructure.Persistence;
using Infrastructure.Repositories;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Shared.Configuration;

namespace Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        var settings = configuration.GetSection("AccountServiceSettings").Get<AccountServiceSettings>() 
            ?? throw new InvalidOperationException("AccountServiceSettings section is missing in configuration");

        services.AddDbContext<AccountDbContext>(options =>
            options.UseSqlServer(
                settings.Database.ConnectionString,
                b => b.MigrationsAssembly(typeof(AccountDbContext).Assembly.FullName)));

        services.AddStackExchangeRedisCache(options =>
        {
            options.Configuration = settings.Redis.ConnectionString;
            options.InstanceName = settings.Redis.InstanceName;
        });

        return services;
    }

    public static ContainerBuilder AddInfrastructure(this ContainerBuilder builder)
    {
        builder.RegisterType<AccountRepository>()
            .As<IAccountRepository>()
            .InstancePerLifetimeScope();

        // Register CQRS handlers
        builder.RegisterType<CreateAccountCommandHandler>().AsImplementedInterfaces();
        builder.RegisterType<UpdateBalanceCommandHandler>().AsImplementedInterfaces();
        builder.RegisterType<CloseAccountCommandHandler>().AsImplementedInterfaces();
        builder.RegisterType<BlockAccountCommandHandler>().AsImplementedInterfaces();
        builder.RegisterType<GetAccountQueryHandler>().AsImplementedInterfaces();
        builder.RegisterType<GetUserAccountsQueryHandler>().AsImplementedInterfaces();

        // Register validators
        builder.RegisterType<CreateAccountCommandValidator>().AsImplementedInterfaces();
        builder.RegisterType<UpdateBalanceCommandValidator>().AsImplementedInterfaces();
        builder.RegisterType<CloseAccountCommandValidator>().AsImplementedInterfaces();
        builder.RegisterType<BlockAccountCommandValidator>().AsImplementedInterfaces();
        builder.RegisterType<GetUserAccountsQueryValidator>().AsImplementedInterfaces();

        return builder;
    }
} 