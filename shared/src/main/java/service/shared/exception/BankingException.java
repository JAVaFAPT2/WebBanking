package service.shared.exception;

public class BankingException extends RuntimeException {

    /**
     * Constructs a new BankingException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method)
     */
    public BankingException(String message) {
        super(message);
    }

    /**
     * Constructs a new BankingException with the specified detail message
     * and cause.
     *
     * @param message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method)
     * @param cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method)
     */
    public BankingException(String message, Throwable cause) {
        super(message, cause);
    }
}