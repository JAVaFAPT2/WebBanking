package account.accountservice.exception;

public class AccountConflict extends GlobalException {
    public AccountConflict(String message) {
        super(GlobalErrolCode.ACCOUNT_ALREADY_EXIST, message);
    }
    public AccountConflict() {
        super("Account already exist", GlobalErrolCode.ACCOUNT_ALREADY_EXIST);
    }
}
