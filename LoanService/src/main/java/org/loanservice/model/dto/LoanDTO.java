package org.loanservice.model.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;


@Schema(
        name = "Loan",
        description = "Schema to hold Loan information"
)
@Data
public class LoanDTO {
    @NotEmpty(message = "Loan type cannot be empty")
    @Schema(
            description = "Type of the loan"
    )
    private String loanType;
    @NotEmpty(message = "Mobile Number cannot be null or empty")
    @Pattern(regexp = "(^$|[0-9]{10})",message = "Mobile Number must be 10 digits")
    @Schema(
            description = "Mobile number of customer",
            example = "1234567890"
    )
    private String mobileNumber;
    @NotEmpty(message = "loanNumber cannot be null or empty")
    @Schema(
            description = "Loan number",
            example = "1234567890"
    )
    private String loanNumber;
    @NotEmpty(message = "Total Loan Amount cannot be null or empty")
    @Schema(
            description = "Total loan amount",
            example = "100000"
    )
    private double totalLoanAmount;
    @PositiveOrZero(message = "Total loan amount paid should be equal or greater than zero")
    @Schema(
            description = "Total loan amount paid", example = "1000"
    )
    private int amountPaid;
    @PositiveOrZero(message = "Total outstanding amount should be equal or greater than zero")
    @Schema(
            description = "Total outstanding amount against a loan", example = "99000"
    )
    private int outstandingAmount;

}
