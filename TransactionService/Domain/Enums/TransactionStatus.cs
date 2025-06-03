namespace TransactionService.Domain.Enums;

public enum TransactionStatus
{
    Pending,       // Initial state
    Processing,    // Actively being processed
    Succeeded,     // Successfully completed
    Failed,        // Failed to complete
    Cancelled,     // Cancelled by user or system
    RequiresAction // Requires further action (e.g., 3DS, manual review)
} 