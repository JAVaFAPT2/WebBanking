package service.accountservice.query.internal.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AccountReadModel {
    @Id
    private UUID id;                // Unique identifier for the account
    private String accountNumber;   // The account number
    private String accountType;     // Type of the account (e.g., savings, checking)
    private BigDecimal balance;      // Current balance of the account
    private String currency;         // Currency of the account
    private String status;           // Status of the account (e.g., active, inactive)
    private String ownerType;        // Type of owner (e.g., individual, business)
    private UUID userId;            // ID of the user who owns the account
}
