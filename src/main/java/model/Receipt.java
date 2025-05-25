package model;

import exception.InvalidReceiptException;
import model.Cashier;
import model.SaleItem;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Receipt implements Serializable {
    private static int ReceiptCounter = 0;
    private final int number;
    private final Cashier cashier;
    private final LocalDateTime dateTime;
    private final List<SaleItem> items;
    private final double totalAmount;

    public Receipt(Cashier cashier, List<SaleItem> items) throws InvalidReceiptException {
        validateReceiptData(cashier, items);

        this.number = ++ReceiptCounter;
        this.cashier = cashier;
        this.dateTime = LocalDateTime.now();
        this.items = new ArrayList<>(items); // Create defensive copy
        this.totalAmount = items.stream().mapToDouble(SaleItem::getTotalCost).sum();
    }

    private void validateReceiptData(Cashier cashier, List<SaleItem> items) throws InvalidReceiptException {
        if (cashier == null) {
            throw new InvalidReceiptException("Receipt cannot be created: Cashier is required and cannot be null");
        }

        if (items == null) {
            throw new InvalidReceiptException("Receipt cannot be created: Items list is required and cannot be null");
        }

        if (items.isEmpty()) {
            throw new InvalidReceiptException("Receipt cannot be created: At least one item must be purchased");
        }

        // Additional validation: check for null items in the list
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == null) {
                throw new InvalidReceiptException("Receipt cannot be created: Item at position " + i + " is null");
            }
        }
    }

    public int getNumber() { return number; }

    public void saveToFile() throws IOException {
        String fileName = "receipt-" + number + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println(this.toString());
        }
    }

    public void serialize() throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("receipt-" + number + ".ser"))) {
            out.writeObject(this);
        }
    }

    public static Receipt deserialize(int number) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("receipt-" + number + ".ser"))) {
            return (Receipt) in.readObject();
        }
    }

    public List<SaleItem> getItems() {
        return new ArrayList<>(items);
    }

    public Cashier getCashier() {
        return cashier;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("======= Receipt №").append(number).append(" =======\n");
        sb.append("Cashier - ").append(cashier.getName()).append("\n");
        sb.append("Date - ").append(dateTime).append("\n");

        sb.append("------------------------\n");
        sb.append("Bought Items: \n");
        for (SaleItem item : items) {
            sb.append("  ").append(item).append("\n");
        }

        sb.append(String.format("Grand Total: %.2f лв", totalAmount));
        return sb.toString();
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}