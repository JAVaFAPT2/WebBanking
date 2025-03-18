package org.cartservice.model.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardCreationRequest {
    private String cardType;
    private String cardHolderName;
    private String cardNumber;
    private String cvv;
    private String expiryDate;
    private Boolean isActive = true;
}
