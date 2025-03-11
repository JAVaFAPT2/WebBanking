package account.fundtransfer.exception;

public class InSufficientFunds extends GlobalException {
    public InSufficientFunds(String message) {
        super(message, GlobalErrolCode.NON_REQUEST);
    }
    public InSufficientFunds() {
        super("Need more funds", GlobalErrolCode.NON_REQUEST);

    }

}
