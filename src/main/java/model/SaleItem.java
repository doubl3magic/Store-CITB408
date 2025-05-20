package model;

import java.io.Serializable;

public class SaleItem implements Serializable {
    private final Product product;
    private final int qty;
    private final double unitPrice;

    public SaleItem(Product product, int qty, double unitPrice) {
        this.product = product;
        this.qty = qty;
        this.unitPrice = unitPrice;
    }
    public double getTotalCost() {
        return unitPrice * qty;
    }

    @Override
    public String toString() {
        return String.format("%s x%d * %.2f leva - %.2f leva", product.getName(), qty, unitPrice, getTotalCost());
    }
}