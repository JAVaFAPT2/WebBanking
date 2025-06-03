namespace FundTransferService.Application.DTOs;

public class FundTransferDetailsDto
{
    public Guid Id { get; set; }
    public Guid FromAccountId { get; set; }
    public Guid ToAccountId { get; set; }
    public decimal Amount { get; set; }
    public string Currency { get; set; }
    public DateTime TransferDate { get; set; }
    public string Status { get; set; }
    public string? ReferenceNumber { get; set; }
    public string? FailureReason { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime LastModifiedAt { get; set; }
} 