using Domain.Events;
using Domain.Interface;
using Domain.Models;
using MediatR;
using Microsoft.Extensions.Logging;

namespace Infrastructure.EventBus;

public class AccountEventHandler :
    INotificationHandler<AccountCreatedEvent>,
    INotificationHandler<AccountClosedEvent>,
    INotificationHandler<AccountBlockedEvent>
{
    private readonly ICardRepository _cardRepository;
    private readonly ILogger<AccountEventHandler> _logger;

    public AccountEventHandler(
        ICardRepository cardRepository,
        ILogger<AccountEventHandler> logger)
    {
        _cardRepository = cardRepository;
        _logger = logger;
    }

    public async Task Handle(AccountCreatedEvent notification, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Account created event received for account {AccountId}", notification.AccountId);
        // No action needed for card service when account is created
    }

    public async Task Handle(AccountClosedEvent notification, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Account closed event received for account {AccountId}", notification.AccountId);

        try
        {
            var cards = await _cardRepository.GetByAccountIdAsync(notification.AccountId);
            foreach (var card in cards)
            {
                if (!card.IsBlocked)
                {
                    card.Block("Account closed");
                    await _cardRepository.UpdateAsync(card);
                }
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error processing account closed event for account {AccountId}", notification.AccountId);
            throw;
        }
    }

    public async Task Handle(AccountBlockedEvent notification, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Account blocked event received for account {AccountId}", notification.AccountId);

        try
        {
            var cards = await _cardRepository.GetByAccountIdAsync(notification.AccountId);
            foreach (var card in cards)
            {
                if (!card.IsBlocked)
                {
                    card.Block("Account blocked");
                    await _cardRepository.UpdateAsync(card);
                }
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error processing account blocked event for account {AccountId}", notification.AccountId);
            throw;
        }
    }
} 