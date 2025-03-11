package account.fundtransfer.exception;

public class InsufficientBalance extends GlobalException{

    public InsufficientBalance(String errorCode, String errorMessage) {
        super(GlobalErrolCode.NOT_ACCEPTABLE, errorMessage);
    }
}
