package org.loanservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanRequest {
    @NotBlank(message = "Loan type cannot be blank")
    private String loanType;

    @NotNull(message = "Loan amount cannot be null")
    @Positive(message = "Loan amount must be positive")
    private double amount;

    @NotNull(message = "Loan term cannot be null")
    @Positive(message = "Loan term must be positive")
    private int term;
}

