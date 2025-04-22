CREATE TABLE Users (
    Id UUID PRIMARY KEY,
    BusinessName VARCHAR(255) NOT NULL,
    Email VARCHAR(255) UNIQUE NOT NULL,
    PhoneNumber VARCHAR(20),
    Status VARCHAR(50), 
    CreatedAt TIMESTAMP,
    UpdatedAt TIMESTAMP
);
