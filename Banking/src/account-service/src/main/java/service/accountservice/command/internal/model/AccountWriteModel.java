package service.accountservice.command.internal.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import service.shared.models.Account;
import service.shared.models.User;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountWriteModel {

    @Id
    private UUID id;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private String currency;
    private String status;
    private String ownerType;
    private UUID userId;
    @ElementCollection
    @CollectionTable(name = "account_transaction_ids", joinColumns = @JoinColumn(name = "account_id"))
    @Column(name = "transaction_id")
    private Set<UUID> transactionIds;


}
