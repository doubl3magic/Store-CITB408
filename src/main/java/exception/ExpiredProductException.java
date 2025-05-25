package exception;

public class ExpiredProductException extends Exception {
    public ExpiredProductException() {
        super("The product has expired.");
    }
}