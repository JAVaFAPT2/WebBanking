using AccountService.Protos;
using Application.CQRS.Commands.BlockAccount;
using Application.CQRS.Commands.CloseAccount;
using Application.CQRS.Commands.CreateAccount;
using Application.CQRS.Commands.UpdateBalance;
using Application.CQRS.DTO;
using Application.CQRS.Queries.GetAccount;
using Application.CQRS.Queries.GetUserAccounts;
using Grpc.Core;
using Infrastructure.EventBus;
using MediatR;
using Microsoft.Extensions.Logging;
using StackExchange.Redis;
using System.Text.Json;
using ProtoMoney = AccountService.Protos.Money;

namespace Presentation.Services;

public class AccountGrpcService : AccountService.Protos.AccountService.AccountServiceBase
{
    private readonly IMediator _mediator;
    private readonly IKafkaProducer _kafkaProducer;
    private readonly IConnectionMultiplexer _redis;
    private readonly ILogger<AccountGrpcService> _logger;
    private const decimal AMOUNT_MULTIPLIER = 100M; // For converting between decimal and long
    private const string ACCOUNT_CACHE_KEY = "account:{0}";
    private const string USER_ACCOUNTS_CACHE_KEY = "user:{0}:accounts";
    private static readonly TimeSpan CACHE_EXPIRY = TimeSpan.FromMinutes(10);

    public AccountGrpcService(
        IMediator mediator,
        IKafkaProducer kafkaProducer,
        IConnectionMultiplexer redis,
        ILogger<AccountGrpcService> logger)
    {
        _mediator = mediator;
        _kafkaProducer = kafkaProducer;
        _redis = redis;
        _logger = logger;
    }

    public override async Task<CreateAccountResponse> CreateAccount(CreateAccountRequest request, ServerCallContext context)
    {
        try
        {
            var command = new CreateAccountCommand(
                Guid.Parse(request.UserId),
                Enum.Parse<Domain.Models.AccountType>(request.AccountType),
                request.InitialBalance.Currency
            );

            var accountId = await _mediator.Send(command);

            // Publish event to Kafka
            await _kafkaProducer.PublishAsync("account-events", new
            {
                EventType = "AccountCreated",
                AccountId = accountId,
                UserId = request.UserId,
                AccountType = request.AccountType,
                Timestamp = DateTime.UtcNow
            });

            return new CreateAccountResponse { AccountId = accountId.ToString() };
        }
        catch (FormatException)
        {
            throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid GUID format"));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error creating account");
            throw new RpcException(new Status(StatusCode.Internal, ex.Message));
        }
    }

    public override async Task<AccountResponse> GetAccount(GetAccountRequest request, ServerCallContext context)
    {
        try
        {
            var accountId = Guid.Parse(request.AccountId);
            var db = _redis.GetDatabase();
            var cacheKey = string.Format(ACCOUNT_CACHE_KEY, accountId);

            // Try to get from cache
            var cachedValue = await db.StringGetAsync(cacheKey);
            if (cachedValue.HasValue)
            {
                var cachedAccount = JsonSerializer.Deserialize<AccountDto>(cachedValue!);
                if (cachedAccount != null)
                    return MapToAccountResponse(cachedAccount);
            }

            // Get from database
            var query = new GetAccountQuery(accountId);
            var account = await _mediator.Send(query);

            if (account == null)
                throw new RpcException(new Status(StatusCode.NotFound, "Account not found"));

            // Cache the result
            await db.StringSetAsync(
                cacheKey,
                JsonSerializer.Serialize(account),
                CACHE_EXPIRY
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
            throw new RpcException(new Status(StatusCode.Internal, ex.Message));
        }
    }

    public override async Task<GetUserAccountsResponse> GetUserAccounts(GetUserAccountsRequest request, ServerCallContext context)
    {
        try
        {
            var userId = Guid.Parse(request.UserId);
            var db = _redis.GetDatabase();
            var cacheKey = string.Format(USER_ACCOUNTS_CACHE_KEY, userId);

            // Try to get from cache
            var cachedValue = await db.StringGetAsync(cacheKey);
            if (cachedValue.HasValue)
            {
                var cachedAccounts = JsonSerializer.Deserialize<IEnumerable<AccountDto>>(cachedValue!);
                if (cachedAccounts != null)
                {
                    var cachedResponse = new GetUserAccountsResponse();
                    cachedResponse.Accounts.AddRange(cachedAccounts.Select(MapToAccountResponse));
                    return cachedResponse;
                }
            }

            // Get from database
            var query = new GetUserAccountsQuery(userId);
            var accounts = await _mediator.Send(query);

            // Cache the result
            await db.StringSetAsync(
                cacheKey,
                JsonSerializer.Serialize(accounts),
                CACHE_EXPIRY
            );

            var response = new GetUserAccountsResponse();
            response.Accounts.AddRange(accounts.Select(MapToAccountResponse));
            return response;
        }
        catch (FormatException)
        {
            throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid GUID format"));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error getting user accounts");
            throw new RpcException(new Status(StatusCode.Internal, ex.Message));
        }
    }

    public override async Task<UpdateBalanceResponse> UpdateBalance(UpdateBalanceRequest request, ServerCallContext context)
    {
        try
        {
            var accountId = Guid.Parse(request.AccountId);
            var amount = request.Amount.Amount / (long)AMOUNT_MULTIPLIER;
            var transactionType = amount >= 0 
                ? Domain.Models.TransactionType.Credit 
                : Domain.Models.TransactionType.Debit;

            var command = new UpdateBalanceCommand(
                accountId,
                Math.Abs(amount),
                transactionType
            );

            var success = await _mediator.Send(command);

            if (success)
            {
                // Invalidate cache
                var db = _redis.GetDatabase();
                await db.KeyDeleteAsync(string.Format(ACCOUNT_CACHE_KEY, accountId));

                // Create transaction ID for tracking
                var transactionId = Guid.NewGuid();

                // Publish balance update event
                await _kafkaProducer.PublishAsync("account-events", new
                {
                    EventType = "BalanceUpdated",
                    AccountId = accountId,
                    Amount = amount,
                    TransactionType = transactionType.ToString(),
                    Timestamp = DateTime.UtcNow
                });

                // Publish transaction event
                await _kafkaProducer.PublishAsync("transaction-events", new
                {
                    EventType = "TransactionCreated",
                    TransactionId = transactionId,
                    AccountId = accountId,
                    Amount = amount,
                    TransactionType = transactionType.ToString(),
                    Description = $"{transactionType} transaction",
                    Currency = request.Amount.Currency,
                    Timestamp = DateTime.UtcNow
                });

                // Publish notification event
                await _kafkaProducer.PublishAsync("notification-events", new
                {
                    EventType = "TransactionNotification",
                    AccountId = accountId,
                    TransactionId = transactionId,
                    Amount = amount,
                    TransactionType = transactionType.ToString(),
                    Currency = request.Amount.Currency,
                    Timestamp = DateTime.UtcNow,
                    Message = $"Your account {accountId} has been {(amount >= 0 ? "credited with" : "debited")} {Math.Abs(amount)} {request.Amount.Currency}"
                });
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
            throw new RpcException(new Status(StatusCode.Internal, ex.Message));
        }
    }

    public override async Task<CloseAccountResponse> CloseAccount(CloseAccountRequest request, ServerCallContext context)
    {
        try
        {
            var accountId = Guid.Parse(request.AccountId);
            var command = new CloseAccountCommand(accountId);
            var success = await _mediator.Send(command);

            if (success)
            {
                // Invalidate cache
                var db = _redis.GetDatabase();
                await db.KeyDeleteAsync(string.Format(ACCOUNT_CACHE_KEY, accountId));

                // Publish event to Kafka
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
            throw new RpcException(new Status(StatusCode.Internal, ex.Message));
        }
    }

    public override async Task<BlockAccountResponse> BlockAccount(BlockAccountRequest request, ServerCallContext context)
    {
        try
        {
            var accountId = Guid.Parse(request.AccountId);
            var command = new BlockAccountCommand(accountId, request.Block);
            var success = await _mediator.Send(command);

            if (success)
            {
                // Invalidate cache
                var db = _redis.GetDatabase();
                await db.KeyDeleteAsync(string.Format(ACCOUNT_CACHE_KEY, accountId));

                // Publish event to Kafka
                await _kafkaProducer.PublishAsync("account-events", new
                {
                    EventType = request.Block ? "AccountBlocked" : "AccountUnblocked",
                    AccountId = accountId,
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
            throw new RpcException(new Status(StatusCode.Internal, ex.Message));
        }
    }

    private static AccountResponse MapToAccountResponse(AccountDto account)
    {
        return new AccountResponse
        {
            AccountId = account.Id.ToString(),
            UserId = account.UserId.ToString(),
            AccountType = account.Type,
            Balance = new ProtoMoney { 
                Amount = (long)(account.Balance * AMOUNT_MULTIPLIER), 
                Currency = account.Currency 
            },
            IsActive = account.Status == "Active",
            IsBlocked = account.Status == "Blocked",
            CreatedAt = account.CreatedAt.ToString("O"),
            UpdatedAt = account.LastModifiedAt?.ToString("O") ?? account.CreatedAt.ToString("O")
        };
    }
} 