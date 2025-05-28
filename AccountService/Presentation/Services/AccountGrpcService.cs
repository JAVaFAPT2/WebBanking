using Application.CQRS.Commands.BlockAccount;
using Application.CQRS.Commands.CloseAccount;
using Application.CQRS.Commands.CreateAccount;
using Application.CQRS.Commands.UpdateBalance;
using Application.CQRS.DTO;
using Application.CQRS.Queries.GetAccount;
using Application.CQRS.Queries.GetUserAccounts;
using Domain.Models;
using DomainMoney = Domain.ValueObjects.Money;
using Grpc.Core;
using Infrastructure.EventBus;
using MediatR;
using Microsoft.Extensions.Caching.Distributed;
using Microsoft.Extensions.Logging;
using HealthChecks.Redis;
using System.Text.Json;
using Presentation.Protos;

namespace Presentation.Services;

public class AccountGrpcService : AccountService.AccountServiceBase
{
    private readonly IMediator _mediator;
    private readonly IKafkaProducer _kafkaProducer;
    private readonly IDistributedCache _cache;
    private readonly ILogger<AccountGrpcService> _logger;
    private const decimal AMOUNT_MULTIPLIER = 100M; // For converting between decimal and long
    private const string ACCOUNT_CACHE_KEY = "account:{0}";
    private const string USER_ACCOUNTS_CACHE_KEY = "user:{0}:accounts";
    private static readonly TimeSpan CACHE_EXPIRY = TimeSpan.FromMinutes(10);

    public AccountGrpcService(
        IMediator mediator,
        IKafkaProducer kafkaProducer,
        IDistributedCache cache,
        ILogger<AccountGrpcService> logger)
    {
        _mediator = mediator;
        _kafkaProducer = kafkaProducer;
        _cache = cache;
        _logger = logger;
    }

    public override async Task<CreateAccountResponse> CreateAccount(CreateAccountRequest request, ServerCallContext context)
    {
        try
        {
            var accountType = request.AccountType.ToUpper() switch
            {
                "SAVINGS" => AccountType.Savings,
                "CHECKING" => AccountType.Checking,
                "FIXED_DEPOSIT" => AccountType.FixedDeposit,
                "BUSINESS" => AccountType.Business,
                _ => throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid account type"))
            };

            var command = new CreateAccountCommand(
                Guid.Parse(request.UserId),
                accountType,
                request.InitialBalance.Currency);

            var accountId = await _mediator.Send(command);
            var account = await _mediator.Send(new GetAccountQuery(accountId));

            if (account == null)
                throw new RpcException(new Status(StatusCode.Internal, "Failed to retrieve created account"));

            await _kafkaProducer.PublishAsync("account-events", new
            {
                EventType = "AccountCreated",
                AccountId = accountId,
                UserId = request.UserId,
                AccountType = request.AccountType,
                Timestamp = DateTime.UtcNow
            });

            return new CreateAccountResponse
            {
                AccountId = account.Id.ToString(),
                AccountNumber = account.AccountNumber
            };
        }
        catch (FormatException)
        {
            throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid GUID format"));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error creating account");
            throw new RpcException(new Status(StatusCode.Internal, "Failed to create account"));
        }
    }

    public override async Task<AccountResponse> GetAccount(GetAccountRequest request, ServerCallContext context)
    {
        try
        {
            var accountId = Guid.Parse(request.AccountId);
            var cacheKey = string.Format(ACCOUNT_CACHE_KEY, accountId);

            // Try to get from cache
            var cachedValue = await _cache.GetStringAsync(cacheKey);
            if (!string.IsNullOrEmpty(cachedValue))
            {
                var cachedAccount = JsonSerializer.Deserialize<AccountDto>(cachedValue);
                if (cachedAccount != null)
                    return MapToAccountResponse(cachedAccount);
            }

            // Get from database
            var account = await _mediator.Send(new GetAccountQuery(accountId));
            if (account == null)
                throw new RpcException(new Status(StatusCode.NotFound, "Account not found"));

            // Cache the result
            await _cache.SetStringAsync(
                cacheKey,
                JsonSerializer.Serialize(account),
                new DistributedCacheEntryOptions { AbsoluteExpirationRelativeToNow = CACHE_EXPIRY }
            );

            return MapToAccountResponse(account);
        }
        catch (FormatException)
        {
            throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid GUID format"));
        }
        catch (RpcException)
        {
            throw;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error getting account");
            throw new RpcException(new Status(StatusCode.Internal, "Failed to retrieve account"));
        }
    }

    public override async Task<GetUserAccountsResponse> GetUserAccounts(GetUserAccountsRequest request, ServerCallContext context)
    {
        try
        {
            var userId = Guid.Parse(request.UserId);
            var cacheKey = string.Format(USER_ACCOUNTS_CACHE_KEY, userId);

            // Try to get from cache
            var cachedValue = await _cache.GetStringAsync(cacheKey);
            if (!string.IsNullOrEmpty(cachedValue))
            {
                var cachedAccounts = JsonSerializer.Deserialize<IEnumerable<AccountDto>>(cachedValue);
                if (cachedAccounts != null)
                {
                    var response = new GetUserAccountsResponse();
                    response.Accounts.AddRange(cachedAccounts.Select(MapToAccountResponse));
                    return response;
                }
            }

            // Get from database
            var accounts = await _mediator.Send(new GetUserAccountsQuery(userId));

            // Cache the result
            await _cache.SetStringAsync(
                cacheKey,
                JsonSerializer.Serialize(accounts),
                new DistributedCacheEntryOptions { AbsoluteExpirationRelativeToNow = CACHE_EXPIRY }
            );

            var dbResponse = new GetUserAccountsResponse(); // Renamed variable to avoid conflict
            dbResponse.Accounts.AddRange(accounts.Select(MapToAccountResponse));
            return dbResponse;
        }
        catch (FormatException)
        {
            throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid GUID format"));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error getting user accounts");
            throw new RpcException(new Status(StatusCode.Internal, "Failed to retrieve user accounts"));
        }
    }

    public override async Task<UpdateBalanceResponse> UpdateBalance(UpdateBalanceRequest request, ServerCallContext context)
    {
        try
        {
            var accountId = Guid.Parse(request.AccountId);
            var transactionType = request.TransactionType.ToUpper() switch
            {
                "CREDIT" => TransactionType.Credit,
                "DEBIT" => TransactionType.Debit,
                _ => throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid transaction type"))
            };

            var command = new UpdateBalanceCommand(
                accountId,
                request.Amount.Amount / AMOUNT_MULTIPLIER,
                transactionType);

            var success = await _mediator.Send(command);

            if (success)
            {
                // Invalidate cache
                await _cache.RemoveAsync(string.Format(ACCOUNT_CACHE_KEY, accountId));

                // Create transaction ID for tracking
                var transactionId = Guid.NewGuid();

                // Publish events
                await PublishBalanceUpdateEvents(accountId, request.Amount, transactionType, transactionId);
            }

            return new UpdateBalanceResponse { Success = success };
        }
        catch (FormatException)
        {
            throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid GUID format"));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error updating balance");
            throw new RpcException(new Status(StatusCode.Internal, "Failed to update balance"));
        }
    }

    public override async Task<CloseAccountResponse> CloseAccount(CloseAccountRequest request, ServerCallContext context)
    {
        try
        {
            var accountId = Guid.Parse(request.AccountId);
            var success = await _mediator.Send(new CloseAccountCommand(accountId));

            if (success)
            {
                // Invalidate cache
                await _cache.RemoveAsync(string.Format(ACCOUNT_CACHE_KEY, accountId));

                // Publish event
                await _kafkaProducer.PublishAsync("account-events", new
                {
                    EventType = "AccountClosed",
                    AccountId = accountId,
                    Timestamp = DateTime.UtcNow
                });
            }

            return new CloseAccountResponse { Success = success };
        }
        catch (FormatException)
        {
            throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid GUID format"));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error closing account");
            throw new RpcException(new Status(StatusCode.Internal, "Failed to close account"));
        }
    }

    public override async Task<BlockAccountResponse> BlockAccount(BlockAccountRequest request, ServerCallContext context)
    {
        try
        {
            var accountId = Guid.Parse(request.AccountId);
            var success = await _mediator.Send(new BlockAccountCommand(accountId, request.Block));

            if (success)
            {
                // Invalidate cache
                await _cache.RemoveAsync(string.Format(ACCOUNT_CACHE_KEY, accountId));

                // Publish event
                await _kafkaProducer.PublishAsync("account-events", new
                {
                    EventType = request.Block ? "AccountBlocked" : "AccountUnblocked",
                    AccountId = accountId,
                    Reason = request.Reason,
                    Timestamp = DateTime.UtcNow
                });
            }

            return new BlockAccountResponse { Success = success };
        }
        catch (FormatException)
        {
            throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid GUID format"));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error blocking/unblocking account");
            throw new RpcException(new Status(StatusCode.Internal, "Failed to block/unblock account"));
        }
    }

    private static AccountResponse MapToAccountResponse(AccountDto account)
    {
        return new AccountResponse
        {
            AccountId = account.Id.ToString(),
            UserId = account.UserId.ToString(),
            AccountNumber = account.AccountNumber,
            AccountType = account.Type.ToUpper(),
            Balance = new Money
            {
                Amount = (long)(account.Balance * AMOUNT_MULTIPLIER),
                Currency = account.Currency
            },
            Status = account.Status.ToUpper(),
            Currency = account.Currency,
            CreatedAt = account.CreatedAt.ToString("O"),
            LastModifiedAt = account.LastModifiedAt?.ToString("O") ?? account.CreatedAt.ToString("O")
        };
    }

    private async Task PublishBalanceUpdateEvents(Guid accountId, Money amount, TransactionType transactionType, Guid transactionId)
    {
        // Publish balance update event
        await _kafkaProducer.PublishAsync("account-events", new
        {
            EventType = "BalanceUpdated",
            AccountId = accountId,
            Amount = amount.Amount / AMOUNT_MULTIPLIER,
            TransactionType = transactionType.ToString(),
            Currency = amount.Currency,
            Timestamp = DateTime.UtcNow
        });

        // Publish transaction event
        await _kafkaProducer.PublishAsync("transaction-events", new
        {
            EventType = "TransactionCreated",
            TransactionId = transactionId,
            AccountId = accountId,
            Amount = amount.Amount / AMOUNT_MULTIPLIER,
            TransactionType = transactionType.ToString(),
            Description = $"{transactionType} transaction",
            Currency = amount.Currency,
            Timestamp = DateTime.UtcNow
        });

        // Publish notification event
        await _kafkaProducer.PublishAsync("notification-events", new
        {
            EventType = "TransactionNotification",
            AccountId = accountId,
            TransactionId = transactionId,
            Amount = amount.Amount / AMOUNT_MULTIPLIER,
            TransactionType = transactionType.ToString(),
            Currency = amount.Currency,
            Timestamp = DateTime.UtcNow,
            Message = $"Your account has been {(transactionType == TransactionType.Credit ? "credited with" : "debited")} {Math.Abs(amount.Amount / AMOUNT_MULTIPLIER)} {amount.Currency}"
        });
    }
} 