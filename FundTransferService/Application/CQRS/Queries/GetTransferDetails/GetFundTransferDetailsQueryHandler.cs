using MediatR;
using FundTransferService.Application.DTOs;
using FundTransferService.Domain.Interfaces;
using Microsoft.Extensions.Logging;

namespace FundTransferService.Application.CQRS.Queries.GetTransferDetails;

public class GetFundTransferDetailsQueryHandler : IRequestHandler<GetFundTransferDetailsQuery, FundTransferDetailsDto?>
{
    private readonly IFundTransferRepository _fundTransferRepository;
    private readonly ILogger<GetFundTransferDetailsQueryHandler> _logger;

    public GetFundTransferDetailsQueryHandler(
        IFundTransferRepository fundTransferRepository, 
        ILogger<GetFundTransferDetailsQueryHandler> logger)
    {
        _fundTransferRepository = fundTransferRepository ?? throw new ArgumentNullException(nameof(fundTransferRepository));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    public async Task<FundTransferDetailsDto?> Handle(GetFundTransferDetailsQuery request, CancellationToken cancellationToken)
    {
        _logger.LogInformation("Fetching fund transfer details for ID {TransferId}", request.TransferId);

        var fundTransfer = await _fundTransferRepository.GetByIdAsync(request.TransferId, cancellationToken);

        if (fundTransfer == null)
        {
            _logger.LogWarning("Fund transfer with ID {TransferId} not found.", request.TransferId);
            return null;
        }

        return new FundTransferDetailsDto
        {
            Id = fundTransfer.Id,
            FromAccountId = fundTransfer.FromAccountId,
            ToAccountId = fundTransfer.ToAccountId,
            Amount = fundTransfer.Amount.Amount,
            Currency = fundTransfer.Amount.Currency,
            TransferDate = fundTransfer.TransferDate,
            Status = fundTransfer.Status.ToString(),
            ReferenceNumber = fundTransfer.ReferenceNumber,
            FailureReason = fundTransfer.FailureReason,
            CreatedAt = fundTransfer.CreatedAt,
            LastModifiedAt = fundTransfer.LastModifiedAt
        };
    }
} 