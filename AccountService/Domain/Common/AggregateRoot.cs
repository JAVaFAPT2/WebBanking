using MediatR;

namespace Domain.Common;

public abstract class AggregateRoot
{
    private readonly List<INotification> _domainEvents = new();
    public Guid Id { get; protected set; }
    public IReadOnlyCollection<INotification> DomainEvents => _domainEvents.AsReadOnly();

    protected void AddDomainEvent(INotification domainEvent)
    {
        _domainEvents.Add(domainEvent);
    }

    public void ClearDomainEvents()
    {
        _domainEvents.Clear();
    }
} 