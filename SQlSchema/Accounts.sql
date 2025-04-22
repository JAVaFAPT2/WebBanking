CREATE TABLE Accounts (
    Id UUID PRIMARY KEY,
    UserId UUID NOT NULL,
    AccountNumber VARCHAR(20) UNIQUE NOT NULL,
    CurrencyCode VARCHAR(10) NOT NULL,
    Balance DECIMAL(18,2) NOT NULL,
    AccountType VARCHAR(50),
    Status VARCHAR(50),
    CreatedAt TIMESTAMP,
    UpdatedAt TIMESTAMP
);
