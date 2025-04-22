CREATE TABLE FundTransfers (
    Id UUID PRIMARY KEY,
    FromAccountId UUID NOT NULL,
    ToAccountId UUID NOT NULL,
    Amount DECIMAL(18,2) NOT NULL,
    CurrencyCode VARCHAR(10) NOT NULL,
    Status VARCHAR(50), 
    Reason TEXT,
    CreatedAt TIMESTAMP,
    UpdatedAt TIMESTAMP
);
