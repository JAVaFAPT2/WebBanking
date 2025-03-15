package org.loanservice.exception;

public class ResourceNotFound extends GlobalException{

    public ResourceNotFound(String loan, String mobileNumber, String number) {
        super("Resource not found on the server", GlobalErrorCode.NOT_FOUND);
    }

    public ResourceNotFound(String message) {
        super(message, GlobalErrorCode.NOT_FOUND);
    }
}
