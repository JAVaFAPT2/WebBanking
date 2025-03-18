package org.cartservice.service;

import org.cartservice.model.dto.CardsDTO;
import org.cartservice.model.dto.request.CardCreationRequest;

public interface CardService {
    void createCard(String phoneNumber);

    CardsDTO fetchCard(String mobileNumber);

    boolean updateCard(CardsDTO cardsDTO);

    boolean deleteCard(String mobileNumber);
}
