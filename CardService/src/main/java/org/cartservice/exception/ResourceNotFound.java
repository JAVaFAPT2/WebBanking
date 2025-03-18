package org.cartservice.exception;


import org.cartservice.model.dto.CardsDTO;
import org.cartservice.model.entity.Cards;

public class ResourceNotFound extends GlobalException {
    public ResourceNotFound(String errorCode, String message, String mobileNumber) {
        super(errorCode, message + " mobileNumber: " + mobileNumber);
    }

}
