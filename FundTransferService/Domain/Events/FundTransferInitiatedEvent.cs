using FundTransferService.Domain.Common;
using FundTransferService.Domain.Entities;
using FundTransferService.Domain.ValueObjects;
using System;

namespace FundTransferService.Domain.Events
{
    public class FundTransferInitiatedEvent : DomainEvent
    {
        public Guid TransferId { get; private set; }
        public Guid SourceAccountId { get; private set; }
        public Guid DestinationAccountId { get; private set; }
        public Money Amount { get; private set; }
        public string TransactionReference { get; private set; } = string.Empty;
        public DateTime InitiatedAt { get; private set; }
        public FundTransferStatus InitialStatus { get; private set; }

        public FundTransferInitiatedEvent(Guid transferId, Guid sourceAccountId, Guid destinationAccountId, Money amount, string transactionReference, DateTime initiatedAt, FundTransferStatus initialStatus)
        {
            TransferId = transferId;
            SourceAccountId = sourceAccountId;
            DestinationAccountId = destinationAccountId;
            Amount = amount;
            TransactionReference = transactionReference;
            InitiatedAt = initiatedAt;
            InitialStatus = initialStatus;
        }
    }
}