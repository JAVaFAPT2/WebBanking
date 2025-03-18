package org.cartservice.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.cartservice.model.CardStatus;
import org.cartservice.model.CardType;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Cards extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;
    private String cardNumber;
    private CardType cardType;
    private BigDecimal amountUsed;
    private BigDecimal availableAmount;
    private String cardHolderName;
    private String cvv;
    private String expiryDate;
    private CardStatus isActive = CardStatus.ACTIVE;
}
