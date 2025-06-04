using System;
using MediatR;

namespace OrchestrationService.Domain.Events;

/// <summary>
/// Event raised when a fund transfer saga is created
/// </summary>
public record FundTransferSagaCreatedEvent(
    Guid SagaId,
    string TransactionId,
    string UserId,
    string SourceAccountId,
    string DestinationAccountId,
    decimal Amount,
    string Currency,
    string Reference) : INotification;

/// <summary>
/// Event raised when the source account has been debited
/// </summary>
public record SourceAccountDebitedEvent(
    Guid SagaId,
    string TransactionId,
    string AccountId,
    decimal Amount,
    string Currency) : INotification;

/// <summary>
/// Event raised when the destination account has been credited
/// </summary>
public record DestinationAccountCreditedEvent(
    Guid SagaId,
    string TransactionId,
    string AccountId,
    decimal Amount,
    string Currency) : INotification;

/// <summary>
/// Event raised when the transfer notification has been sent
/// </summary>
public record TransferNotificationSentEvent(
    Guid SagaId,
    string TransactionId,
    string UserId) : INotification;

/// <summary>
/// Event raised when the fund transfer has been completed successfully
/// </summary>
public record FundTransferCompletedEvent(
    Guid SagaId,
    string TransactionId) : INotification;

/// <summary>
/// Event raised when crediting the destination account fails
/// </summary>
public record DestinationAccountCreditFailedEvent(
    Guid SagaId,
    string TransactionId,
    string AccountId,
    string ErrorMessage) : INotification;

/// <summary>
/// Event raised when the fund transfer has been compensated (rolled back) after a failure
/// </summary>
public record FundTransferCompensatedEvent(
    Guid SagaId,
    string TransactionId,
    string? ErrorMessage) : INotification; 