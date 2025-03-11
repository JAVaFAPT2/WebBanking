package account.fundtransfer.exception;



public class AccountStatusException extends GlobalException {
    public AccountStatusException(String message) {
        super(GlobalErrolCode.BAD_REQUEST, message);
    }

}
