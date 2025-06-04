using System;
using System.Collections.Generic;
using MediatR;
using TransactionService.Application.CQRS.Queries.GetTransactionById; // For TransactionDetailsDto

namespace TransactionService.Application.CQRS.Queries.GetTransactionsByAccountId;

public record GetTransactionsByAccountIdQuery(
    Guid AccountId,
    DateTime? FromDate = null, 
    DateTime? ToDate = null,
    int PageNumber = 1,
    int PageSize = 20
) : IRequest<IEnumerable<TransactionDetailsDto>>; 