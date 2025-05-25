package model;

import exception.ExpiredProductException;
import exception.OutOfStockException;
import model.Cashier;
import model.Product;
import model.Receipt;
import model.SaleItem;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Store {
    private final List<Product> productCatalog = new ArrayList<>();
    private final List<Cashier> employeeCashiers = new ArrayList<>();
    private final List<Receipt> transactionRecords = new ArrayList<>();
    private final double saleDiscountRate;
    private final int nearExpiryDays;

    public Store(double saleDiscountRate, int nearExpiryDays) {
        this.saleDiscountRate = saleDiscountRate;
        this.nearExpiryDays = nearExpiryDays;
    }

    public void registerCashier(Cashier employee) {
        employeeCashiers.add(employee);
    }

    public void stockProduct(Product item) {
        productCatalog.add(item);
    }

    public Receipt processTransaction(Cashier employee, Map<Integer, Integer> orderDetails)
            throws Exception {
        LocalDate currentDate = LocalDate.now();
        List<SaleItem> purchasedItems = new ArrayList<>();

        for (Map.Entry<Integer, Integer> orderEntry : orderDetails.entrySet()) {
            Product selectedProduct = locateProductById(orderEntry.getKey());
            int requestedAmount = orderEntry.getValue();

            if (selectedProduct == null || selectedProduct.isExpired(currentDate)) {
                throw new ExpiredProductException();
            }

            if (selectedProduct.getQty() < requestedAmount) {
                throw new OutOfStockException(selectedProduct.getName(), selectedProduct.getQty());
            }

            double finalPrice = selectedProduct.getPriceOnSale(currentDate, nearExpiryDays, saleDiscountRate);
            selectedProduct.decreaseQuantity(requestedAmount);
            purchasedItems.add(new SaleItem(selectedProduct, requestedAmount, finalPrice));
        }

        Receipt transactionReceipt = new Receipt(employee, purchasedItems);
        transactionReceipt.saveToFile();
        transactionReceipt.serialize();
        transactionRecords.add(transactionReceipt);
        return transactionReceipt;
    }

    private Product locateProductById(int productId) {
        return productCatalog.stream().filter(item -> item.getId() == productId).findFirst().orElse(null);
    }

    public double computeTotalRevenue() {
        return transactionRecords.stream().mapToDouble(Receipt::getTotalAmount).sum();
    }

    public double computeStaffPayroll() {
        return employeeCashiers.stream().mapToDouble(Cashier::getSalary).sum();
    }

    public double computeDeliveryCosts() {
        return productCatalog.stream().mapToDouble(item -> item.deliveryPrice * item.getQty()).sum();
    }

    public double computeNetProfit() {
        return computeTotalRevenue() - computeStaffPayroll() - computeDeliveryCosts();
    }

    public int getTotalTransactionCount() {
        return transactionRecords.size();
    }

    public List<Product> getStoreInventory() {
        return new ArrayList<>(productCatalog);
    }

    public List<Cashier> getStoreEmployees() {
        return new ArrayList<>(employeeCashiers);
    }

    public List<Receipt> getAllTransactions() {
        return new ArrayList<>(transactionRecords);
    }

    // Utility methods
    public List<Product> findExpiredItems(LocalDate checkDate) {
        return productCatalog.stream()
                .filter(item -> item.isExpired(checkDate))
                .collect(Collectors.toList());
    }

    public List<Product> findItemsRunningLow(int minimumStock) {
        return productCatalog.stream()
                .filter(item -> item.getQty() <= minimumStock)
                .collect(Collectors.toList());
    }
}