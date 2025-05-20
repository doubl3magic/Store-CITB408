package model;

import model.Cashier;
import model.SaleItem;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

public class Receipt implements Serializable {
    private static int ReceiptCounter = 0;
    private final int number;
    private final Cashier cashier;
    private final LocalDateTime dateTime;
    private final List<SaleItem> items;
    private final double totalAmount;

    public Receipt(Cashier cashier, List<SaleItem> items) {
        this.number = ++ReceiptCounter;
        this.cashier = cashier;
        this.dateTime = LocalDateTime.now();
        this.items = items;
        this.totalAmount = items.stream().mapToDouble(SaleItem::getTotalCost).sum();
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("======= Receipt №").append(number).append(" =======\n");
        sb.append("Cashier - ").append(cashier.getName()).append("\n");
        sb.append("Date - ").append(dateTime).append("\n");

        sb.append("------------------------");
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
