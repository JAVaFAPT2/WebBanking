package org.cartservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Schema(
        name = "Cards",
        description = "Schema to hold card information"
)
@Data
public class CardsDTO {
    @NotEmpty(message = "Mobile number cannot be empty or null")
    @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number must be 10 digits")
    @Schema(
            description = "Mobile number of customer",
            example = "1234567891"
    )
    private String mobileNumber;
    @NotEmpty(message = "Card number should not be null or empty")
    @Pattern(regexp = "(^$|[0-9]{12})", message = "CardNumber must be 12 digits")
    @Schema(
            description = "Card Number of the customer", example = "100646930341"
    )
    private String cardNumber;
    @NotEmpty(message = "CardType can not be a null or empty")
    @Schema(
            description = "Type of the card", example = "Credit Card"
    )
    private String cardType;
    @Positive(message = "Total card limit should be greater than zero")
    @Schema(
            description = "Total amount limit available against a card", example = "100000"
    )
    private int totalLimit;
    @PositiveOrZero(message = "Total amount used should be equal or greater than zero")
    @Schema(
            description = "Total amount used by a Customer", example = "1000"
    )
    private int amountUsed;
    @PositiveOrZero(message = "Total available amount should be equal or greater than zero")
    @Schema(
            description = "Total available amount against a card", example = "90000"
    )
    private int availableAmount;
}