package account.fundtransfer.exception;

public class AccountUpdateException extends GlobalException{
    public AccountUpdateException(String errorMessage, String notAcceptable) {
        super(GlobalErrolCode.NOT_ACCEPTABLE, errorMessage);
    }
}
