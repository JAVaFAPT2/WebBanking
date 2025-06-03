using Grpc.Core;
using MediatR;
using Microsoft.Extensions.Logging;
using System;
using System.Linq;
using System.Threading.Tasks;
using TransactionService.Application.CQRS.Commands.InitiateTransaction;
using TransactionService.Application.CQRS.Queries.GetTransactionById;
using TransactionService.Application.CQRS.Queries.GetTransactionsByAccountId;
using TransactionService.Protos; // Namespace from your .proto file
using static TransactionService.Protos.Transactioner; // For TransactionerBase

namespace TransactionService.Presentation.Services;

public class TransactionGrpcService : TransactionerBase
{
    private readonly IMediator _mediator;
    private readonly ILogger<TransactionGrpcService> _logger;

    public TransactionGrpcService(IMediator mediator, ILogger<TransactionGrpcService> logger)
    {
        _mediator = mediator;
        _logger = logger;
    }

    public override async Task<InitiateTransactionResponse> InitiateTransaction(InitiateTransactionRequest request, ServerCallContext context)
    {
        _logger.LogInformation("gRPC InitiateTransaction called for InitiatedBy: {InitiatedBy}", request.InitiatedBy);
        try
        {
            var command = new InitiateTransactionCommand(
                string.IsNullOrWhiteSpace(request.AccountFromId) ? (Guid?)null : Guid.Parse(request.AccountFromId),
                string.IsNullOrWhiteSpace(request.AccountToId) ? (Guid?)null : Guid.Parse(request.AccountToId),
                MapProtoToDomain(request.Type),
                new Domain.ValueObjects.Money((decimal)request.Amount.Amount, request.Amount.Currency),
                request.Description,
                request.InitiatedBy,
                string.IsNullOrWhiteSpace(request.CorrelationId) ? (Guid?)null : Guid.Parse(request.CorrelationId)
            );

            var result = await _mediator.Send(command);

            return new InitiateTransactionResponse
            {
                TransactionId = result.TransactionId.ToString(),
                InitialStatus = MapDomainToProto(result.InitialStatus)
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error in gRPC InitiateTransaction for InitiatedBy: {InitiatedBy}", request.InitiatedBy);
            throw new RpcException(new Status(StatusCode.Internal, ex.Message));
        }
    }

    public override async Task<TransactionDetailsResponse> GetTransactionById(GetTransactionByIdRequest request, ServerCallContext context)
    {
        _logger.LogInformation("gRPC GetTransactionById called for ID: {TransactionId}", request.TransactionId);
        try
        {
            var query = new GetTransactionByIdQuery(Guid.Parse(request.TransactionId));
            var result = await _mediator.Send(query);

            if (result == null)
            {
                throw new RpcException(new Status(StatusCode.NotFound, $"Transaction with ID {request.TransactionId} not found."));
            }
            return MapDomainToProto(result);
        }
        catch (FormatException ex)
        {
            _logger.LogError(ex, "Invalid GUID format for TransactionId: {TransactionId}", request.TransactionId);
            throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid TransactionId format."));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error in gRPC GetTransactionById for ID: {TransactionId}", request.TransactionId);
            throw new RpcException(new Status(StatusCode.Internal, ex.Message));
        }
    }

    public override async Task<GetTransactionsByAccountIdResponse> GetTransactionsByAccountId(GetTransactionsByAccountIdRequest request, ServerCallContext context)
    {
        _logger.LogInformation("gRPC GetTransactionsByAccountId called for Account ID: {AccountId}", request.AccountId);
        try
        {
            DateTime? fromDate = null;
            if (request.HasFromDate && !string.IsNullOrWhiteSpace(request.FromDate))
            {
                if (DateTime.TryParse(request.FromDate, out var parsedFromDate))
                    fromDate = parsedFromDate;
                else
                    _logger.LogWarning("Invalid FromDate format: {FromDate}, defaulting to null.", request.FromDate);
            }

            DateTime? toDate = null;
            if (request.HasToDate && !string.IsNullOrWhiteSpace(request.ToDate))
            {
                if (DateTime.TryParse(request.ToDate, out var parsedToDate))
                    toDate = parsedToDate;
                else
                    _logger.LogWarning("Invalid ToDate format: {ToDate}, defaulting to null.", request.ToDate);
            }

            var query = new GetTransactionsByAccountIdQuery(
                Guid.Parse(request.AccountId),
                fromDate,
                toDate,
                request.HasPageNumber ? request.PageNumber : 1,
                request.HasPageSize ? request.PageSize : 20
            );

            var results = await _mediator.Send(query);
            var response = new GetTransactionsByAccountIdResponse();
            response.Transactions.AddRange(results.Select(MapDomainToProto));
            return response;
        }
        catch (FormatException ex)
        {
            _logger.LogError(ex, "Invalid GUID format for AccountId: {AccountId}", request.AccountId);
            throw new RpcException(new Status(StatusCode.InvalidArgument, "Invalid AccountId format."));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error in gRPC GetTransactionsByAccountId for Account ID: {AccountId}", request.AccountId);
            throw new RpcException(new Status(StatusCode.Internal, ex.Message));
        }
    }

    // --- Helper Mappers --- 
    private static TransactionDetailsResponse MapDomainToProto(TransactionDetailsDto dto)
    {
        return new TransactionDetailsResponse
        {
            Id = dto.Id.ToString(),
            CorrelationId = dto.CorrelationId.ToString(),
            AccountFromId = dto.AccountFromId?.ToString(),
            AccountToId = dto.AccountToId?.ToString(),
            Type = MapDomainToProto(dto.Type),
            Status = MapDomainToProto(dto.Status),
            Amount = new Protos.Money { Amount = (double)dto.Amount.Amount, Currency = dto.Amount.Currency },
            InitiatedAt = dto.InitiatedAt.ToString("o"), // ISO 8601 format
            LastUpdatedAt = dto.LastUpdatedAt.ToString("o"), // ISO 8601 format
            Description = dto.Description ?? string.Empty,
            FailureReason = dto.FailureReason ?? string.Empty,
            ReferenceNumber = dto.ReferenceNumber ?? string.Empty,
            InitiatedBy = dto.InitiatedBy ?? string.Empty
        };
    }

    private static Domain.Enums.TransactionType MapProtoToDomain(TransactionTypeProto protoType)
    {
        return protoType switch
        {
            TransactionTypeProto.TransactionTypeDebit => Domain.Enums.TransactionType.Debit,
            TransactionTypeProto.TransactionTypeCredit => Domain.Enums.TransactionType.Credit,
            TransactionTypeProto.TransactionTypeTransfer => Domain.Enums.TransactionType.Transfer,
            TransactionTypeProto.TransactionTypeFee => Domain.Enums.TransactionType.Fee,
            TransactionTypeProto.TransactionTypeRefund => Domain.Enums.TransactionType.Refund,
            TransactionTypeProto.TransactionTypePayment => Domain.Enums.TransactionType.Payment,
            TransactionTypeProto.TransactionTypeReversal => Domain.Enums.TransactionType.Reversal,
            _ => throw new ArgumentOutOfRangeException(nameof(protoType), $"Unsupported transaction type: {protoType}")
        };
    }

    private static TransactionTypeProto MapDomainToProto(Domain.Enums.TransactionType domainType)
    {
        return domainType switch
        {
            Domain.Enums.TransactionType.Debit => TransactionTypeProto.TransactionTypeDebit,
            Domain.Enums.TransactionType.Credit => TransactionTypeProto.TransactionTypeCredit,
            Domain.Enums.TransactionType.Transfer => TransactionTypeProto.TransactionTypeTransfer,
            Domain.Enums.TransactionType.Fee => TransactionTypeProto.TransactionTypeFee,
            Domain.Enums.TransactionType.Refund => TransactionTypeProto.TransactionTypeRefund,
            Domain.Enums.TransactionType.Payment => TransactionTypeProto.TransactionTypePayment,
            Domain.Enums.TransactionType.Reversal => TransactionTypeProto.TransactionTypeReversal,
            _ => TransactionTypeProto.TransactionTypeUnspecified
        };
    }

    private static TransactionStatusProto MapDomainToProto(Domain.Enums.TransactionStatus domainStatus)
    {
        return domainStatus switch
        {
            Domain.Enums.TransactionStatus.Pending => TransactionStatusProto.TransactionStatusPending,
            Domain.Enums.TransactionStatus.Processing => TransactionStatusProto.TransactionStatusProcessing,
            Domain.Enums.TransactionStatus.Succeeded => TransactionStatusProto.TransactionStatusSucceeded,
            Domain.Enums.TransactionStatus.Failed => TransactionStatusProto.TransactionStatusFailed,
            Domain.Enums.TransactionStatus.Cancelled => TransactionStatusProto.TransactionStatusCancelled,
            Domain.Enums.TransactionStatus.RequiresAction => TransactionStatusProto.TransactionStatusRequiresAction,
            _ => TransactionStatusProto.TransactionStatusUnspecified
        };
    }
} 