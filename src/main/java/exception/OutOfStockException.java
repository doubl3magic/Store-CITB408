package exception;

public class OutOfStockException extends Exception {

    public OutOfStockException(String productName, int available) {
        super(String.format("Not enough quantity of \"%s\". We have only %d available.",
                productName, available));
    }
}
