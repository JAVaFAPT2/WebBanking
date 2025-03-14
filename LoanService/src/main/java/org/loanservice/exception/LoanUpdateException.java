package org.loanservice.exception;

public class LoanUpdateException extends GlobalException{
    public LoanUpdateException(String errorCode, String message) {
        super(errorCode, message);
    }
}
