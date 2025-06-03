using MediatR;
using FundTransferService.Domain.ValueObjects;

namespace FundTransferService.Application.CQRS.Commands.InitiateTransfer;

public record InitiateFundTransferCommand(
    Guid FromAccountId,
    Guid ToAccountId,
    Money Amount,
    string? ReferenceNumber // Optional, could be client-provided or generated
) : IRequest<Guid>; // Returns the ID of the new fund transfer 