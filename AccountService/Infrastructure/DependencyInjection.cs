using Application.CQRS.Commands.BlockAccount;
using Application.CQRS.Commands.CloseAccount;
using Application.CQRS.Commands.CreateAccount;
using Application.CQRS.Commands.UpdateBalance;
using Application.CQRS.Queries.GetAccount;
using Application.CQRS.Queries.GetUserAccounts;
using Autofac;
using Domain.Interface;
using Infrastructure.EventBus;
using Infrastructure.Logging;
using Infrastructure.Persistence;
using Infrastructure.Repositories;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Shared.Configuration;
using StackExchange.Redis;

namespace Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        services.Configure<AccountServiceSettings>(options => 
            configuration.GetSection(nameof(AccountServiceSettings)).Bind(options));
            
        var settings = configuration.GetSection(nameof(AccountServiceSettings)).Get<AccountServiceSettings>();

        if (settings == null)
            throw new ArgumentNullException(nameof(settings), "AccountServiceSettings is not configured");

        // Database
        services.AddDbContext<AccountDbContext>(options =>
            options.UseSqlServer(settings.Database.ConnectionString));

        // Redis
        services.AddSingleton<IConnectionMultiplexer>(sp =>
            ConnectionMultiplexer.Connect(settings.Redis.ConnectionString));

        // Redis Logger
        services.AddSingleton<ILoggerProvider>(sp =>
        {
            var redis = sp.GetRequiredService<IConnectionMultiplexer>();
            return new RedisLoggerProvider(redis, "logs:account-service");
        });

        // Kafka Producer
        services.AddSingleton<IKafkaProducer, KafkaProducer>();

        // Repositories
        services.AddScoped<IAccountRepository, AccountRepository>();

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

public class RedisLoggerProvider : ILoggerProvider
{
    private readonly IConnectionMultiplexer _redis;
    private readonly string _keyPrefix;

    public RedisLoggerProvider(IConnectionMultiplexer redis, string keyPrefix)
    {
        _redis = redis;
        _keyPrefix = keyPrefix;
    }

    public ILogger CreateLogger(string categoryName)
    {
        return new RedisLogger(categoryName, _redis, _keyPrefix);
    }

    public void Dispose()
    {
        // Redis connection is managed by DI container
    }
} 