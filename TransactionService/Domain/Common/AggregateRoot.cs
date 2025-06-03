using MediatR;
using System.Collections.Generic;
using System.Linq;

namespace TransactionService.Domain.Common;

public abstract class AggregateRoot
{
    private readonly List<INotification> _domainEvents = new();

    public IReadOnlyCollection<INotification> DomainEvents => _domainEvents.AsReadOnly();

    protected void AddDomainEvent(INotification domainEvent)
    {
        _domainEvents.Add(domainEvent);
    }

    public void ClearDomainEvents()
    {
        _domainEvents.Clear();
    }

    // Optional: If you want to add multiple events at once
    protected void AddDomainEvents(IEnumerable<INotification> domainEvents)
    {
        _domainEvents.AddRange(domainEvents);
    }
} 