package account.fundtransfer.exception;

public class GlobalException extends RuntimeException {
    private final String errorCode;

    private final String errorMessage;

    public GlobalException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }
    public String getErrorMessage() {
        return errorMessage;
    }


}
