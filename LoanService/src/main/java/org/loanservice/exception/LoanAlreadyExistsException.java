package org.loanservice.exception;

public class LoanAlreadyExistsException extends GlobalException{
    public LoanAlreadyExistsException(String s) {
        super("Loan already exists", GlobalErrorCode.CONFLICT);
    }
}
