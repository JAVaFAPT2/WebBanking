using System;
using TransactionService.Domain.Enums; // For TransactionStatus

namespace TransactionService.Application.IntegrationEvents.Events;

// This event would be published by an external system/service (e.g., Payment Gateway Adapter)
// and consumed by this TransactionService to update the status of a transaction.
public class PaymentGatewayCallbackEvent
{
    public Guid TransactionId { get; set; }
    public Guid CorrelationId { get; set; } // To correlate with the original transaction
    public TransactionStatus NewStatus { get; set; } // e.g., Succeeded, Failed
    public string? PaymentGatewayReferenceId { get; set; }
    public string? FailureReason { get; set; }
    public DateTime Timestamp { get; set; }
} 