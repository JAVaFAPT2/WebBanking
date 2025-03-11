package account.fundtransfer.exception;

public class AccountClosingException extends GlobalException {
    public AccountClosingException(String message) {
        super(GlobalErrolCode.BAD_REQUEST, message);
    }
}
