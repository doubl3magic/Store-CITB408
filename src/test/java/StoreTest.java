import model.*;
import exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class StoreTest {

    private Store store;
    private Cashier cashier;
    private List<String> createdFiles;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        store = new Store(0.15, 3); // 15% discount - 3 days to expiry
        cashier = new Cashier(1, "Mariya", 1000);
        store.registerCashier(cashier);

        store.stockProduct(new FoodProduct(100, "Waffle", 1.00, LocalDate.now().plusDays(2), 10));
        store.stockProduct(new NonFoodProduct(200, "Parfum", 5.00, LocalDate.now().plusDays(30), 5));

        createdFiles = new ArrayList<>();
    }

    @AfterEach
    public void cleanUp() {
        for (String filename : createdFiles) {
            try {
                Files.deleteIfExists(Path.of(filename));
            } catch (IOException e) {
                //
            }
        }

        int transactionCount = store.getTotalTransactionCount();
        for (int i = 1; i <= transactionCount + 5; i++) {
            try {
                Files.deleteIfExists(Path.of("receipt-" + i + ".txt"));
                Files.deleteIfExists(Path.of("receipt-" + i + ".ser"));
            } catch (IOException e) {
                //
            }
        }
    }

    @Test
    public void testSuccessfulTransaction() throws Exception {
        Map<Integer, Integer> basket = new HashMap<>();
        basket.put(100, 2);
        basket.put(200, 1);

        Receipt receipt = store.processTransaction(cashier, basket);

        createdFiles.add("receipt-" + receipt.getNumber() + ".txt");
        createdFiles.add("receipt-" + receipt.getNumber() + ".ser");

        assertNotNull(receipt);
        assertEquals(2, receipt.getItems().size());
        assertTrue(receipt.getTotalAmount() > 0);

        assertTrue(Files.exists(Path.of("receipt-" + receipt.getNumber() + ".txt")));
        assertTrue(Files.exists(Path.of("receipt-" + receipt.getNumber() + ".ser")));
    }

    @Test
    public void testOutOfStock() {
        Map<Integer, Integer> basket = new HashMap<>();
        basket.put(100, 20);

        OutOfStockException exception = assertThrows(OutOfStockException.class, () -> {
            store.processTransaction(cashier, basket);
        });

        assertTrue(exception.getMessage().contains("Waffle") ||
                exception.getMessage().contains("10"));
    }

    @Test
    public void testExpiredProduct() {
        store.stockProduct(new FoodProduct(300, "Cheese", 0.90, LocalDate.now().minusDays(1), 5));
        Map<Integer, Integer> basket = new HashMap<>();
        basket.put(300, 1);

        assertThrows(ExpiredProductException.class, () -> {
            store.processTransaction(cashier, basket);
        });
    }

    @Test
    public void testNonExistentProduct() {
        Map<Integer, Integer> basket = new HashMap<>();
        basket.put(999, 1);

        assertThrows(Exception.class, () -> {
            store.processTransaction(cashier, basket);
        });
    }

    @Test
    public void testComputeRevenueAndProfit() throws Exception {
        Map<Integer, Integer> basket = new HashMap<>();
        basket.put(100, 2);
        Receipt receipt = store.processTransaction(cashier, basket);

        createdFiles.add("receipt-" + receipt.getNumber() + ".txt");
        createdFiles.add("receipt-" + receipt.getNumber() + ".ser");

        assertTrue(store.computeTotalRevenue() > 0);
        assertTrue(store.computeNetProfit() <= store.computeTotalRevenue());

        double expectedRevenue = receipt.getTotalAmount();
        assertEquals(expectedRevenue, store.computeTotalRevenue(), 0.01);

        double expectedSalaries = store.computeStaffPayroll();
        assertTrue(expectedSalaries > 0);
    }

    @Test
    public void testFindExpiredItems() {
        store.stockProduct(new FoodProduct(301, "Italian Cheese", 3.50, LocalDate.now().minusDays(2), 3));
        List<Product> expired = store.findExpiredItems(LocalDate.now());

        assertFalse(expired.isEmpty());
        assertTrue(expired.stream().anyMatch(p -> p.getName().equals("Italian Cheese")));
    }

    @Test
    public void testFindLowStockItems() {
        List<Product> lowStock = store.findItemsRunningLow(5);
        assertTrue(lowStock.stream().anyMatch(p -> p.getQty() <= 5));

        List<Product> veryLowStock = store.findItemsRunningLow(2);
        assertTrue(veryLowStock.size() <= lowStock.size());
    }

    @Test
    public void testTransactionCount() throws Exception {
        int before = store.getTotalTransactionCount();

        Map<Integer, Integer> basket = new HashMap<>();
        basket.put(100, 1);
        Receipt receipt = store.processTransaction(cashier, basket);

        createdFiles.add("receipt-" + receipt.getNumber() + ".txt");
        createdFiles.add("receipt-" + receipt.getNumber() + ".ser");

        assertEquals(before + 1, store.getTotalTransactionCount());

        Map<Integer, Integer> basket2 = new HashMap<>();
        basket2.put(200, 1);
        Receipt receipt2 = store.processTransaction(cashier, basket2);

        createdFiles.add("receipt-" + receipt2.getNumber() + ".txt");
        createdFiles.add("receipt-" + receipt2.getNumber() + ".ser");

        assertEquals(before + 2, store.getTotalTransactionCount());
    }

    @Test
    public void testGetStoreInventory() {
        List<Product> inventory = store.getStoreInventory();
        assertEquals(2, inventory.size());

        inventory.clear();
        assertEquals(2, store.getStoreInventory().size());
    }

    @Test
    public void testGetEmployees() {
        List<Cashier> employees = store.getStoreEmployees();
        assertEquals(1, employees.size());
        assertEquals("Mariya", employees.get(0).getName());

        Cashier newCashier = new Cashier(2, "Ivan", 1200);
        store.registerCashier(newCashier);

        List<Cashier> updatedEmployees = store.getStoreEmployees();
        assertEquals(2, updatedEmployees.size());
    }

    @Test
    public void testMultipleTransactionsRevenue() throws Exception {
        double initialRevenue = store.computeTotalRevenue();

        Map<Integer, Integer> basket1 = new HashMap<>();
        basket1.put(100, 1);
        Receipt receipt1 = store.processTransaction(cashier, basket1);
        createdFiles.add("receipt-" + receipt1.getNumber() + ".txt");
        createdFiles.add("receipt-" + receipt1.getNumber() + ".ser");

        double revenueAfterFirst = store.computeTotalRevenue();
        assertTrue(revenueAfterFirst > initialRevenue);

        Map<Integer, Integer> basket2 = new HashMap<>();
        basket2.put(200, 1);
        Receipt receipt2 = store.processTransaction(cashier, basket2);
        createdFiles.add("receipt-" + receipt2.getNumber() + ".txt");
        createdFiles.add("receipt-" + receipt2.getNumber() + ".ser");

        double revenueAfterSecond = store.computeTotalRevenue();
        assertTrue(revenueAfterSecond > revenueAfterFirst);

        double expectedTotal = receipt1.getTotalAmount() + receipt2.getTotalAmount();
        assertEquals(expectedTotal, revenueAfterSecond, 0.01);
    }

    @Test
    public void testInventoryUpdateAfterTransaction() throws Exception {
        List<Product> initialInventory = store.getStoreInventory();
        Product waffle = initialInventory.stream()
                .filter(p -> p.getName().equals("Waffle"))
                .findFirst()
                .orElse(null);
        assertNotNull(waffle);
        int initialWaffleQty = waffle.getQty();

        Map<Integer, Integer> basket = new HashMap<>();
        basket.put(100, 3);
        Receipt receipt = store.processTransaction(cashier, basket);
        createdFiles.add("receipt-" + receipt.getNumber() + ".txt");
        createdFiles.add("receipt-" + receipt.getNumber() + ".ser");

        List<Product> updatedInventory = store.getStoreInventory();
        Product updatedWaffle = updatedInventory.stream()
                .filter(p -> p.getName().equals("Waffle"))
                .findFirst()
                .orElse(null);
        assertNotNull(updatedWaffle);
        assertEquals(initialWaffleQty - 3, updatedWaffle.getQty());
    }
}