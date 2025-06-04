using FundTransferService.Application.Messaging.Events;
using Microsoft.Extensions.Logging;
using System.Threading.Tasks;
using System.Threading;

namespace FundTransferService.Application.Messaging.Handlers
{
    // Example handler for AccountUpdatedEvent
    public class AccountUpdatedEventHandler : IMessageHandler<string, AccountUpdatedEvent>
    {
        private readonly ILogger<AccountUpdatedEventHandler> _logger;

        public AccountUpdatedEventHandler(ILogger<AccountUpdatedEventHandler> logger)
        {
            _logger = logger;
        }

        public Task HandleAsync(string key, AccountUpdatedEvent value, CancellationToken cancellationToken)
        {
            _logger.LogInformation("Received AccountUpdatedEvent: AccountId = {AccountId}, NewBalance = {NewBalance} {Currency}, Timestamp = {Timestamp}. Key = {Key}", 
                value.AccountId, value.NewBalance, value.Currency, value.Timestamp, key ?? "null");
            
            // TODO: Implement business logic based on the event
            // For example, update local cache, trigger other processes, etc.

            return Task.CompletedTask;
        }
    }
} 