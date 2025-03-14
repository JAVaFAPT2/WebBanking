package org.loanservice.exception;

public class InsufficientLiquidity extends GlobalException{
    public InsufficientLiquidity(String errorCode, String message) {
        super(errorCode, message);
    }
}
