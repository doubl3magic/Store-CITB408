package model;

import java.time.LocalDate;

public class FoodProduct extends Product {
    private static final double PROFIT_MARGIN_FACTOR = 0.30;

    public FoodProduct(int id, String name, double deliveryPrice, LocalDate expiryDate, int qty) {
        super(id, name, deliveryPrice, expiryDate, qty);
    }

    @Override
    public double getPriceOnSale(LocalDate today, int daysBeforeExpiry, double reductionRate) {
        double baseAmount = deliveryPrice * (1 + PROFIT_MARGIN_FACTOR);

        if (isCloseToExpire(today, daysBeforeExpiry)) {
            baseAmount = baseAmount * (1.0 - reductionRate);
        }

        return baseAmount;
    }
}
