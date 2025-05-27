using System;

namespace Application.CQRS.DTO;

public record AccountDto(
    Guid Id,
    string AccountNumber,
    Guid UserId,
    string Type,
    decimal Balance,
    string Status,
    string Currency,
    DateTime CreatedAt,
    DateTime? LastModifiedAt); 