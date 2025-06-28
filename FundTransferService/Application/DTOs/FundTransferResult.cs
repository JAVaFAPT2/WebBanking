namespace FundTransferService.Application.DTOs;

public record FundTransferResult(
    bool IsSuccess,
    Guid? TransferId,
    string? ErrorMessage
); 