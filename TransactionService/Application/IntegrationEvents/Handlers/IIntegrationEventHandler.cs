using System.Threading.Tasks;

namespace TransactionService.Application.IntegrationEvents.Handlers;

public interface IIntegrationEventHandler<in TIntegrationEvent>
    where TIntegrationEvent : class // Ensuring it's a reference type, common for DTOs/events
{
    Task Handle(TIntegrationEvent @event);
}

// Non-generic version if needed for DI registration or other purposes,
// though typically the generic one is registered and resolved.
public interface IIntegrationEventHandler
{
    Task Handle(object @eventData); // Less type-safe, usually avoid if possible
} 