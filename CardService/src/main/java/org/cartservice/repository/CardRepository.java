package org.cartservice.repository;

import org.cartservice.model.entity.Cards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Cards, Long> {
    Optional<Cards> findByCardNumber(String cardNumber);

    Optional<Cards> findByCardHolderName(String cardHolderName);

    Optional<Cards> findByUserPhoneNumber(String userPhoneNumber);
    
}
