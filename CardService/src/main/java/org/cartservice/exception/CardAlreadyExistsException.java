package org.cartservice.exception;

public class CardAlreadyExistsException extends GlobalException {
    public CardAlreadyExistsException(String message) {
        super(message);
    }
}
