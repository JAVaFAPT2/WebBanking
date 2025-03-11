package account.fundtransfer.exception;



public class ResourceNotFound extends GlobalException {
    public ResourceNotFound(String message) {
        super(GlobalErrolCode.ACCOUNT_NOT_FOUND, message);
    }
    public ResourceNotFound(String fundTransferNotFound, String accountNotFound) {
        super("Account not found", GlobalErrolCode.ACCOUNT_NOT_FOUND);
    }
}
