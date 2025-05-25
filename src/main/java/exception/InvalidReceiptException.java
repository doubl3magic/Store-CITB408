package exception;

public class InvalidReceiptException extends Exception {
    public InvalidReceiptException() {
        super();
    }

    public InvalidReceiptException(String message) {
        super(message);
    }

    public InvalidReceiptException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidReceiptException(Throwable cause) {
        super(cause);
    }
}