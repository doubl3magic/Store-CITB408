package model;

import java.time.LocalDate;

public class NonFoodProduct extends Product {
    private static final double PROFIT_MARGIN_FACTOR = 0.50;

    public NonFoodProduct(int id, String name, double deliveryPrice, LocalDate expiryDate, int qty) {
        super(id, name, deliveryPrice, expiryDate, qty);
    }

    @Override
    public double getPriceOnSale(LocalDate currentDate, int daysBeforeExpiry, double discountPercent) {
        double price = deliveryPrice * (1 + PROFIT_MARGIN_FACTOR);
        if (isCloseToExpire(currentDate, daysBeforeExpiry)) {
            price *= (1 - discountPercent);
        }
        return price;
    }
}
