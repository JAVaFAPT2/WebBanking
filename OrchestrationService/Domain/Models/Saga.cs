using System;
using System.Collections.Generic;
using MediatR;

namespace OrchestrationService.Domain.Models;

/// <summary>
/// Base class for all saga/orchestration processes
/// </summary>
public abstract class Saga
{
    private readonly List<INotification> _domainEvents = new();

    /// <summary>
    /// Unique identifier for the saga
    /// </summary>
    public Guid Id { get; protected set; }

    /// <summary>
    /// Current state of the saga
    /// </summary>
    public SagaState State { get; protected set; }

    /// <summary>
    /// When the saga was created
    /// </summary>
    public DateTime CreatedAt { get; protected set; }

    /// <summary>
    /// When the saga was last updated
    /// </summary>
    public DateTime? LastUpdatedAt { get; protected set; }

    /// <summary>
    /// Error message if the saga failed
    /// </summary>
    public string? ErrorMessage { get; protected set; }

    /// <summary>
    /// Current step in the saga process
    /// </summary>
    public int CurrentStep { get; protected set; }

    /// <summary>
    /// Total number of steps in the saga process
    /// </summary>
    public int TotalSteps { get; protected set; }

    /// <summary>
    /// Domain events that have been raised by this saga
    /// </summary>
    public IReadOnlyCollection<INotification> DomainEvents => _domainEvents.AsReadOnly();

    protected Saga()
    {
        Id = Guid.NewGuid();
        State = SagaState.NotStarted;
        CreatedAt = DateTime.UtcNow;
        CurrentStep = 0;
    }

    /// <summary>
    /// Start the saga execution
    /// </summary>
    public virtual void Start()
    {
        if (State != SagaState.NotStarted)
        {
            throw new InvalidOperationException($"Cannot start saga from state {State}");
        }

        State = SagaState.Running;
        LastUpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Mark the saga as completed successfully
    /// </summary>
    public virtual void Complete()
    {
        if (State != SagaState.Running)
        {
            throw new InvalidOperationException($"Cannot complete saga from state {State}");
        }

        State = SagaState.Completed;
        LastUpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Mark the saga as failed and start compensating actions
    /// </summary>
    public virtual void Fail(string errorMessage)
    {
        if (State != SagaState.Running && State != SagaState.NotStarted)
        {
            throw new InvalidOperationException($"Cannot fail saga from state {State}");
        }

        State = SagaState.Compensating;
        ErrorMessage = errorMessage;
        LastUpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Mark the saga as fully compensated after failure
    /// </summary>
    public virtual void MarkCompensationCompleted()
    {
        if (State != SagaState.Compensating)
        {
            throw new InvalidOperationException($"Cannot mark compensation completed from state {State}");
        }

        State = SagaState.Failed;
        LastUpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Add a domain event to be dispatched
    /// </summary>
    protected void AddDomainEvent(INotification eventItem)
    {
        _domainEvents.Add(eventItem);
    }

    /// <summary>
    /// Clear all domain events
    /// </summary>
    public void ClearDomainEvents()
    {
        _domainEvents.Clear();
    }
} 