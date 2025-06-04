namespace TransactionService.Domain.Enums;

public enum TransactionType
{
    Debit,         // Money out from an account
    Credit,        // Money in to an account
    Transfer,      // Movement between accounts
    Fee,           // Service fee
    Refund,
    Payment,       // e.g. Loan payment, bill payment
    Reversal       // A previous transaction reversed
} 