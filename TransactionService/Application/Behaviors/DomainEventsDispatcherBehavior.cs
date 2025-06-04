using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using MediatR;
using Microsoft.Extensions.Logging;
using TransactionService.Domain.Common; // For AggregateRoot
using TransactionService.Domain.Interfaces; // For ITransactionRepository (as an example of a unit of work)

namespace TransactionService.Application.Behaviors;

public class DomainEventsDispatcherBehavior<TRequest, TResponse> : IPipelineBehavior<TRequest, TResponse>
    where TRequest : IRequest<TResponse>
{
    private readonly IMediator _mediator;
    private readonly ILogger<DomainEventsDispatcherBehavior<TRequest, TResponse>> _logger;
    // In a real application with EF Core, you would inject your DbContext here
    // to access tracked entities and their domain events.
    // For simplicity, if ITransactionRepository were part of a Unit of Work that tracks entities,
    // it could be used. However, AggregateRoot itself holds events.
    // This example assumes we might iterate through known aggregate roots or have a way to get them.
    // A more robust solution involves a Unit of Work pattern or directly using the DbContext change tracker.

    public DomainEventsDispatcherBehavior(
        IMediator mediator, 
        ILogger<DomainEventsDispatcherBehavior<TRequest, TResponse>> logger)
    {
        _mediator = mediator;
        _logger = logger;
    }

    public async Task<TResponse> Handle(TRequest request, RequestHandlerDelegate<TResponse> next, CancellationToken cancellationToken)
    {
        var response = await next(); // Execute the main command handler

        // After the handler, dispatch domain events.
        // This is a simplified dispatch. In a real app with EF Core or a UoW,
        // you'd get entities from the ChangeTracker or UoW.
        // For now, we assume events are collected by the AggregateRoot and we need a way to access those roots.
        // This part needs to be adapted to how you manage your entities' lifecycle.
        
        // Placeholder: In a real scenario, you would iterate over tracked aggregate roots.
        // For demonstration, let's assume there's a conceptual "DomainEventService" or similar
        // that has access to entities that were part of the transaction.
        // Since direct access to all modified aggregates isn't trivial here without a UoW/DbContext,
        // this behavior often works in conjunction with SaveChangesAsync overriding in DbContext
        // or a specific Unit of Work implementation that gathers events before saving.

        // A common pattern: The command handler calls _unitOfWork.SaveChangesAsync().
        // Inside SaveChangesAsync(), before context.SaveChanges(), events are dispatched.
        // If not using EF Core directly in handlers, the ITransactionRepository might save, 
        // and events need to be pulled from the saved entity if it's returned or accessible.

        // For this example, let's assume events are cleared from AggregateRoots after dispatch.
        // The actual dispatching should occur *before* the transaction is committed.
        // This behavior as-is would dispatch *after* the main handler. If the handler calls SaveChanges,
        // a separate mechanism (like DbContext overriding SaveChanges) is better for event dispatch *before* commit.

        // Simulating event dispatch from a hypothetical list of aggregates processed by the handler.
        // This is highly dependent on your architecture for entity tracking and persistence.

        // _logger.LogInformation("----- Checking for domain events after handling {CommandName}", typeof(TRequest).Name);
        // var domainEntities = ... // How to get entities with events?
        // var domainEvents = domainEntities.SelectMany(x => x.Entity.DomainEvents).ToList();
        // domainEntities.ForEach(entity => entity.Entity.ClearDomainEvents());
        // foreach (var domainEvent in domainEvents)
        // {
        //     _logger.LogInformation("Dispatching domain event: {DomainEvent}", domainEvent.GetType().Name);
        //     await _mediator.Publish(domainEvent, cancellationToken);
        // }

        return response;
    }

    // A more EF Core specific approach would be to override DbContext.SaveChangesAsync
    // or use a dedicated UnitOfWork that calls the dispatcher.
    // This behavior is more for cross-cutting concerns like logging/validation.
    // True domain event dispatch often ties into the persistence mechanism.
    // Given the current setup, the command handler itself (or a service it calls) would be more likely
    // to publish events directly after a successful persistence, or the repository would.
    // To make this behavior truly useful for event dispatch, it needs a way to access entities handled by the request.
} 