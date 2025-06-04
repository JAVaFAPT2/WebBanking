using Domain.Common;
using Domain.Events;
using Domain.ValueObjects;

namespace Domain.Models;

public class Card : AggregateRoot
{
    public Guid Id { get; private set; }
    public Guid AccountId { get; private set; }
    public string CardNumber { get; private set; }
    public string CardholderName { get; private set; }
    public CardType Type { get; private set; }
    public string CVV { get; private set; }
    public DateTime ExpiryDate { get; private set; }
    public bool IsActive { get; private set; }
    public bool IsBlocked { get; private set; }
    public Money CreditLimit { get; private set; }
    public Money AvailableBalance { get; private set; }
    public DateTime CreatedAt { get; private set; }
    public DateTime? LastUsedAt { get; private set; }

    private Card() { }

    public static Card Issue(Guid accountId, string cardholderName, CardType type, string cardNumber, string cvv, Money creditLimit)
    {
        var card = new Card
        {
            Id = Guid.NewGuid(),
            AccountId = accountId,
            CardNumber = cardNumber,
            CardholderName = cardholderName,
            Type = type,
            CVV = cvv,
            ExpiryDate = DateTime.UtcNow.AddYears(4),
            IsActive = false,
            IsBlocked = false,
            CreditLimit = creditLimit,
            AvailableBalance = creditLimit,
            CreatedAt = DateTime.UtcNow
        };

        card.AddDomainEvent(new CardIssuedEvent(
            card.Id,
            card.AccountId,
            card.CardNumber,
            card.CardholderName,
            card.Type,
            card.ExpiryDate));

        return card;
    }

    public void Activate()
    {
        if (IsActive)
            throw new InvalidOperationException("Card is already active");

        IsActive = true;
        AddDomainEvent(new CardActivatedEvent(Id, AccountId, DateTime.UtcNow));
    }

    public void Block(string reason)
    {
        if (IsBlocked)
            throw new InvalidOperationException("Card is already blocked");

        IsBlocked = true;
        AddDomainEvent(new CardBlockedEvent(Id, AccountId, reason, DateTime.UtcNow));
    }

    public void AuthorizeTransaction(string merchantName, Money amount)
    {
        if (!IsActive)
            throw new InvalidOperationException("Card is not active");

        if (IsBlocked)
            throw new InvalidOperationException("Card is blocked");

        if (IsExpired())
            throw new InvalidOperationException("Card is expired");

        if (Type == CardType.Credit && amount > AvailableBalance)
            throw new InvalidOperationException("Insufficient funds");

        if (Type == CardType.Credit)
        {
            AvailableBalance -= amount;
        }

        LastUsedAt = DateTime.UtcNow;

        AddDomainEvent(new TransactionAuthorizedEvent(
            Id,
            AccountId,
            merchantName,
            amount,
            AvailableBalance,
            DateTime.UtcNow));
    }

    private bool IsExpired() => DateTime.UtcNow > ExpiryDate;
}