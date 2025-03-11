package account.accountservice.exception;

public class AccountClosingException extends GlobalException {
    public AccountClosingException(String message) {
        super(GlobalErrolCode.BAD_REQUEST, message);
    }
}
