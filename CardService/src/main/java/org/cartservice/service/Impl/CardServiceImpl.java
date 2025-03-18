package org.cartservice.service.Impl;

import org.cartservice.exception.CardAlreadyExistsException;
import org.cartservice.exception.ResourceNotFound;
import org.cartservice.model.CardType;
import org.cartservice.model.StatusCode;
import org.cartservice.model.dto.CardsDTO;
import org.cartservice.model.entity.Cards;
import org.cartservice.model.mapper.CardMapper;
import org.cartservice.repository.CardRepository;
import org.cartservice.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

@Service
public class CardServiceImpl implements CardService {

    @Autowired
    private final CardRepository cardRepository;


    @Autowired
    private final CardMapper cardMapper = new CardMapper();

    public CardServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }


    @Override
    public void createCard(String mobileNumber) {
        Optional<Cards> optionalCards = cardRepository.findByUserPhoneNumber(mobileNumber);
        if(optionalCards.isPresent())
            throw new CardAlreadyExistsException("Card already exists with given mobile numer "+mobileNumber);
        cardRepository.save(createNewCard(mobileNumber));
    }

    private Cards createNewCard(String mobileNumber) {
        Cards newCard = new Cards();
//        newCard.setCardNumber("1234567890123456");
//        newCard.setCardHolderName("John Doe");
//        newCard.setCardType(CardType.CREDIT);
//        newCard.setExpiryDate("12/25");
//        newCard.setCvv("123");
//        newCard.setUserPhoneNumber(mobileNumber);
//        return newCard;
        long randomCardNumber = 1000000000L + new Random().nextInt(9000000);
        newCard.setCardNumber(Long.toString(randomCardNumber));
        newCard.setUserPhoneNumber(mobileNumber);
        newCard.setCardType(CardType.CREDIT);
        newCard.setCardLimit(StatusCode.NEW_CARD_LIMIT);
        newCard.setAmountUsed(BigDecimal.valueOf(10000));
        newCard.setAvailableAmount(StatusCode.NEW_CARD_LIMIT);
        return newCard;
    }

    @Override
    public CardsDTO fetchCard(String mobileNumber) {
        Optional<Cards> optionalCards = cardRepository.findByUserPhoneNumber(mobileNumber);
        if(optionalCards.isEmpty())
            throw new ResourceNotFound("Not found","Not found Mobile Number :",mobileNumber);
        return cardMapper.convertToDto(optionalCards.get());

    }

    @Override
    public boolean updateCard(CardsDTO cardsDTO) {
        Cards cards = cardRepository.findByCardNumber(
                cardsDTO.getCardNumber()
        ).orElseThrow(
                ()-> new ResourceNotFound(  "Not found","Not found Card Number :",cardsDTO.getCardNumber())
        );
        Cards updatedCard = cardMapper.convertToEntity(cardsDTO);
        updatedCard.setCardNumber(cards.getCardNumber());
        updatedCard.setCardId(cards.getCardId());
        cardRepository.save(updatedCard);
        return true;
    }

    @Override
    public boolean deleteCard(String mobileNumber) {
        Optional<Cards> optionalCards = cardRepository.findByUserPhoneNumber(mobileNumber);
        if(optionalCards.isEmpty())
            throw new ResourceNotFound("Not found","Not found Mobile Number :",mobileNumber);
        cardRepository.delete(optionalCards.get());
        return true;
    }
}
