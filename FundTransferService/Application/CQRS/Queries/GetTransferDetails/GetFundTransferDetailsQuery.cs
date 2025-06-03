using MediatR;
using FundTransferService.Application.DTOs;

namespace FundTransferService.Application.CQRS.Queries.GetTransferDetails;

public record GetFundTransferDetailsQuery(Guid TransferId) : IRequest<FundTransferDetailsDto?>; 