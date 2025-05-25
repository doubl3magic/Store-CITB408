import exception.InvalidReceiptException;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReceiptTest {
    private Receipt receipt;
    private Cashier testCashier;
    private List<SaleItem> testItems;
    private SaleItem item1;
    private SaleItem item2;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() throws InvalidReceiptException {
        testCashier = new Cashier(1, "Мария Петрова", 2500.0);

        Product product1 = new FoodProduct(101, "Кисело мляко", 2.50,
                LocalDate.now().plusDays(7), 15);
        Product product2 = new NonFoodProduct(102, "Препарат за миене", 4.80,
                LocalDate.now().plusDays(365), 8);

        item1 = new SaleItem(product1, 3, 2.25);
        item2 = new SaleItem(product2, 1, 4.80);

        testItems = Arrays.asList(item1, item2);
        receipt = new Receipt(testCashier, testItems);
    }

    @AfterEach
    public void cleanUp() {
        try {
            Files.deleteIfExists(Path.of("receipt-" + receipt.getNumber() + ".txt"));
            Files.deleteIfExists(Path.of("receipt-" + receipt.getNumber() + ".ser"));
        } catch (IOException e) {
            //
        }
    }

    @Test
    public void testReceiptCreation() {
        assertNotNull(receipt);
        assertTrue(receipt.getNumber() > 0);
        assertEquals(testCashier, receipt.getCashier());
        assertNotNull(receipt.getItems());
        assertEquals(2, receipt.getItems().size());
    }

    @Test
    public void testTotalAmountCalculation() {
        double expectedTotal = 6.75 + 4.80; // item1: 3*2.25 + item2: 1*4.80
        assertEquals(expectedTotal, receipt.getTotalAmount(), 0.01);
    }

    @Test
    public void testUniqueReceiptNumbers() throws InvalidReceiptException {
        Receipt receipt2 = new Receipt(testCashier, testItems);
        Receipt receipt3 = new Receipt(testCashier, testItems);

        assertNotEquals(receipt.getNumber(), receipt2.getNumber());
        assertNotEquals(receipt2.getNumber(), receipt3.getNumber());
        assertTrue(receipt2.getNumber() > receipt.getNumber());
        assertTrue(receipt3.getNumber() > receipt2.getNumber());
    }

    @Test
    public void testGetItemsReturnsDefensiveCopy() {
        List<SaleItem> items = receipt.getItems();
        int originalSize = items.size();

        items.clear();

        assertEquals(2, receipt.getItems().size());
        assertNotEquals(originalSize, items.size());
    }

    @Test
    public void testSingleItemReceipt() throws InvalidReceiptException {
        List<SaleItem> singleItem = Collections.singletonList(item1);
        Receipt singleItemReceipt = new Receipt(testCashier, singleItem);

        assertEquals(6.75, singleItemReceipt.getTotalAmount(), 0.01);
        assertEquals(1, singleItemReceipt.getItems().size());
    }

    @Test
    public void testNullCashierThrowsException() {
        InvalidReceiptException exception = assertThrows(InvalidReceiptException.class, () -> {
            new Receipt(null, testItems);
        });
        assertTrue(exception.getMessage().contains("Cashier is required"));
    }

    @Test
    public void testNullItemsListThrowsException() {
        InvalidReceiptException exception = assertThrows(InvalidReceiptException.class, () -> {
            new Receipt(testCashier, null);
        });
        assertTrue(exception.getMessage().contains("Items list is required"));
    }

    @Test
    public void testEmptyItemsListThrowsException() {
        InvalidReceiptException exception = assertThrows(InvalidReceiptException.class, () -> {
            new Receipt(testCashier, Collections.emptyList());
        });
        assertTrue(exception.getMessage().contains("At least one item must be purchased"));
    }

    @Test
    public void testNullItemInListThrowsException() {
        List<SaleItem> itemsWithNull = Arrays.asList(item1, null, item2);
        InvalidReceiptException exception = assertThrows(InvalidReceiptException.class, () -> {
            new Receipt(testCashier, itemsWithNull);
        });
        assertTrue(exception.getMessage().contains("Item at position 1 is null"));
    }

    @Test
    public void testSaveToFile() throws IOException {
        receipt.saveToFile();

        String expectedFileName = "receipt-" + receipt.getNumber() + ".txt";
        File savedFile = new File(expectedFileName);

        assertTrue(savedFile.exists());
        assertTrue(savedFile.length() > 0);


        String fileContent = Files.readString(savedFile.toPath());
        assertTrue(fileContent.contains("Receipt №" + receipt.getNumber()));
        assertTrue(fileContent.contains(testCashier.getName()));
        assertTrue(fileContent.contains("Grand Total"));
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        receipt.serialize();

        String expectedFileName = "receipt-" + receipt.getNumber() + ".ser";
        File serializedFile = new File(expectedFileName);

        assertTrue(serializedFile.exists());

        Receipt deserializedReceipt = Receipt.deserialize(receipt.getNumber());

        assertNotNull(deserializedReceipt);
        assertEquals(receipt.getNumber(), deserializedReceipt.getNumber());
        assertEquals(receipt.getTotalAmount(), deserializedReceipt.getTotalAmount(), 0.01);
        assertEquals(receipt.getCashier().getName(), deserializedReceipt.getCashier().getName());
        assertEquals(receipt.getItems().size(), deserializedReceipt.getItems().size());
    }

    @Test
    public void testDeserializeNonExistentReceipt() {
        assertThrows(FileNotFoundException.class, () -> {
            Receipt.deserialize(99999);
        });
    }

    @Test
    public void testToStringFormat() {
        String receiptString = receipt.toString();

        assertNotNull(receiptString);
        assertTrue(receiptString.contains("Receipt №" + receipt.getNumber()));
        assertTrue(receiptString.contains("Cashier - " + testCashier.getName()));
        assertTrue(receiptString.contains("Bought Items:"));
        assertTrue(receiptString.contains("Grand Total:"));
        assertTrue(receiptString.contains(String.format("%.2f лв", receipt.getTotalAmount())));
    }

    @Test
    public void testReceiptDateTime() throws InvalidReceiptException {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        Receipt newReceipt = new Receipt(testCashier, testItems);
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        String receiptString = newReceipt.toString();
        assertTrue(receiptString.contains("Date -"));
    }

    @Test
    public void testLargeReceiptWithManyItems() throws InvalidReceiptException {
        List<SaleItem> manyItems = Arrays.asList(
                new SaleItem(new FoodProduct(1, "Item1", 1.0, LocalDate.now().plusDays(1), 10), 5, 1.0),
                new SaleItem(new FoodProduct(2, "Item2", 2.0, LocalDate.now().plusDays(1), 10), 3, 2.0),
                new SaleItem(new FoodProduct(3, "Item3", 3.0, LocalDate.now().plusDays(1), 10), 2, 3.0),
                new SaleItem(new FoodProduct(4, "Item4", 4.0, LocalDate.now().plusDays(1), 10), 1, 4.0)
        );

        Receipt largeReceipt = new Receipt(testCashier, manyItems);

        double expectedTotal = 5*1.0 + 3*2.0 + 2*3.0 + 1*4.0;
        assertEquals(expectedTotal, largeReceipt.getTotalAmount(), 0.01);
        assertEquals(4, largeReceipt.getItems().size());
    }

    @Test
    public void testZeroTotalAmount() throws InvalidReceiptException {
        SaleItem zeroItem = new SaleItem(
                new FoodProduct(999, "Free Item", 0.0, LocalDate.now().plusDays(1), 1),
                1, 0.0
        );

        Receipt zeroReceipt = new Receipt(testCashier, Collections.singletonList(zeroItem));
        assertEquals(0.0, zeroReceipt.getTotalAmount(), 0.01);
    }
}