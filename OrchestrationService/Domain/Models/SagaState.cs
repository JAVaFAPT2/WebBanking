namespace OrchestrationService.Domain.Models;

/// <summary>
/// Represents the state of a saga or distributed transaction
/// </summary>
public enum SagaState
{
    /// <summary>
    /// The saga has been initialized but no steps have been executed
    /// </summary>
    NotStarted,

    /// <summary>
    /// The saga is currently in progress
    /// </summary>
    Running,

    /// <summary>
    /// The saga has completed successfully
    /// </summary>
    Completed,

    /// <summary>
    /// The saga has failed and is currently executing compensating actions
    /// </summary>
    Compensating,

    /// <summary>
    /// The saga has failed and all compensating actions have been completed
    /// </summary>
    Failed,

    /// <summary>
    /// The saga is in an unknown state and requires manual intervention
    /// </summary>
    Unknown
} 