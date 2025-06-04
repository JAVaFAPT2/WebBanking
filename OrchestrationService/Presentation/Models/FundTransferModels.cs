using System;
using System.ComponentModel.DataAnnotations;

namespace OrchestrationService.Presentation.Models;

/// <summary>
/// Request to start a fund transfer
/// </summary>
public class FundTransferRequest
{
    /// <summary>
    /// ID of the user initiating the transfer
    /// </summary>
    [Required]
    public string UserId { get; set; } = string.Empty;
    
    /// <summary>
    /// ID of the source account
    /// </summary>
    [Required]
    public string SourceAccountId { get; set; } = string.Empty;
    
    /// <summary>
    /// ID of the destination account
    /// </summary>
    [Required]
    public string DestinationAccountId { get; set; } = string.Empty;
    
    /// <summary>
    /// Amount to transfer
    /// </summary>
    [Required]
    [Range(0.01, double.MaxValue, ErrorMessage = "Amount must be greater than zero")]
    public decimal Amount { get; set; }
    
    /// <summary>
    /// Currency of the amount
    /// </summary>
    [Required]
    public string Currency { get; set; } = "USD";
    
    /// <summary>
    /// Reference or description for the transfer
    /// </summary>
    public string Reference { get; set; } = string.Empty;
}

/// <summary>
/// Response after starting a fund transfer
/// </summary>
public class FundTransferResponse
{
    /// <summary>
    /// Unique transaction ID for tracking the transfer
    /// </summary>
    public string TransactionId { get; set; } = string.Empty;
    
    /// <summary>
    /// Status of the transfer request
    /// </summary>
    public string Status { get; set; } = string.Empty;
    
    /// <summary>
    /// Additional information about the transfer
    /// </summary>
    public string Message { get; set; } = string.Empty;
}

/// <summary>
/// Response with the status of a fund transfer
/// </summary>
public class FundTransferStatusResponse
{
    /// <summary>
    /// Unique transaction ID
    /// </summary>
    public string TransactionId { get; set; } = string.Empty;
    
    /// <summary>
    /// Current status of the transfer
    /// </summary>
    public string Status { get; set; } = string.Empty;
    
    /// <summary>
    /// When the transfer was created
    /// </summary>
    public DateTime CreatedAt { get; set; }
    
    /// <summary>
    /// When the transfer was last updated
    /// </summary>
    public DateTime? LastUpdatedAt { get; set; }
    
    /// <summary>
    /// Current step in the transfer process
    /// </summary>
    public int CurrentStep { get; set; }
    
    /// <summary>
    /// Total number of steps in the transfer process
    /// </summary>
    public int TotalSteps { get; set; }
    
    /// <summary>
    /// ID of the source account
    /// </summary>
    public string SourceAccountId { get; set; } = string.Empty;
    
    /// <summary>
    /// ID of the destination account
    /// </summary>
    public string DestinationAccountId { get; set; } = string.Empty;
    
    /// <summary>
    /// Amount being transferred
    /// </summary>
    public decimal Amount { get; set; }
    
    /// <summary>
    /// Currency of the amount
    /// </summary>
    public string Currency { get; set; } = string.Empty;
    
    /// <summary>
    /// Error message if the transfer failed
    /// </summary>
    public string? ErrorMessage { get; set; }
} 