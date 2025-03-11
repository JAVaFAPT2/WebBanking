package account.accountservice.exception;

public class GlobalErrolCode {
    private GlobalErrolCode() {}
    public static final String ACCOUNT_NOT_FOUND = "400";
    public static final String ACCOUNT_ALREADY_EXIST = "401";
    public static final String NON_REQUEST = "405";
    public static final String BAD_REQUEST = "402";
}
