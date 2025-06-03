using System;

namespace FundTransferService.Application.Messaging.Events
{
    // Example event that might be consumed from another service
    public class AccountUpdatedEvent
    {
        public Guid AccountId { get; set; }
        public decimal NewBalance { get; set; }
        public string Currency { get; set; } = string.Empty;
        public DateTime Timestamp { get; set; }
    }
} 