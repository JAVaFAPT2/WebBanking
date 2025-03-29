package service.accountservice.command.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateAccountCommand {
    @NotNull(message = "Account ID is required")
    private UUID id;
    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Account type is required")
    private String accountType;

    @NotNull(message = "Initial balance is required")
    @Positive(message = "Initial balance must be a positive value")
    private BigDecimal balance;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Owner type is required")
    private String ownerType;

    @NotNull(message = "User ID is required")
    private UUID userId;
}
