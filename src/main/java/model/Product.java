package model;
import java.io.Serializable;
import java.time.LocalDate;

public abstract class Product implements Serializable {
    protected int id;
    protected String name;
    protected double deliveryPrice;
    protected LocalDate expiryDate;
    protected int qty;

    public Product(int id, String name, double deliveryPrice, LocalDate expiryDate, int qty) {
        this.id = id;
        this.name = name;
        this.deliveryPrice = deliveryPrice;
        this.expiryDate = expiryDate;
        this.qty = qty;
    }

    public abstract double getPriceOnSale(LocalDate currentDate, int thresholdDays, double discountPercent);

    public boolean isExpired(LocalDate currentDate) {
        return currentDate.isAfter(expiryDate);
    }

    public boolean isCloseToExpire(LocalDate currentDate, int thresholdDays) {
        return expiryDate.minusDays(thresholdDays).isBefore(currentDate) && !isExpired(currentDate);
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public LocalDate getExpiryDate() { return expiryDate; }

    public int getQty() { return qty; }

    public void decreaseQuantity(int amount) {
        if (qty < amount) {
            throw new IllegalArgumentException("Insufficient quantity.");
        }
        qty -= amount;
    }
}
