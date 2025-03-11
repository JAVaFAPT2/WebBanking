package account.accountservice.exception;

public class AccountNotFound  extends  GlobalException {
    public AccountNotFound(String message) {
        super(GlobalErrolCode.ACCOUNT_NOT_FOUND, message);
    }
    public AccountNotFound() {
        super("Account not found", GlobalErrolCode.ACCOUNT_NOT_FOUND);
    }
}
