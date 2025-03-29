package service.accountservice.command.api;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountCommand  {
    @NotBlank(message = "Account ID is required")
    private UUID accountId;
    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @Positive(message = "Balance must be a positive value")
    private BigDecimal balance;

    private String accountType;

    private String currency;

    private String status;

    private UUID userId;

    // Optional: Add a field to identify which user is making the update
    private String updatedBy;
}
