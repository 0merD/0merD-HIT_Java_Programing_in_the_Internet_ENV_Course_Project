package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryManager {
    private final String INVENTORY_DIR = "resources/inventories/";

    // <BranchNumber, BranchFileName>
    private static final Map<Integer, String> branchNumberToCity = new HashMap<>();

    private static InventoryManager instance; // Singleton Instance

    // lock objects
    private final Map<String, Object> fileLocks = new ConcurrentHashMap<>();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Initialize the branches and the concurrency locks
    private InventoryManager() {
        branchNumberToCity.put(1, "telaviv");
        branchNumberToCity.put(2, "haifa");
        branchNumberToCity.put(3, "jerusalem");

        for (Map.Entry<Integer, String> entry : branchNumberToCity.entrySet()) {
            String city = entry.getValue();
            fileLocks.put(city, new Object());
        }
    }

    // Synchronized here prevents creating two instances of this class.
    public static synchronized InventoryManager getInstance() {
        if (instance == null) {
            instance = new InventoryManager();
        }
        return instance;
    }

    public List<InventoryItem> getInventoryByCity(Integer branchNumber) {
        String cityName = branchNumberToCity.get(branchNumber);
        File file = new File(INVENTORY_DIR + cityName + ".json");

        if (!file.exists()) {
            throw new RuntimeException("Inventory file not found: " + file.getPath());
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<InventoryItem>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading inventory file: " + file.getPath(), e);
        }
    }

    public void reduceStock(Integer branchNumber, String productId, int qty)
            throws IOException, InventoryItem.InsufficientQuantityException, IllegalArgumentException {

        if (!ProductsCatalog.existsProductIdentifier(productId)) {
            throw new IllegalArgumentException(productId + " doesn't exist");
        }

        String city = branchNumberToCity.get(branchNumber);

        if (city == null) {
            throw new IllegalArgumentException("Unknown branch number: " + branchNumber);
        }

        String path = INVENTORY_DIR + city + ".json";

        synchronized (getBranchLock(city)) {
            List<InventoryItem> inventory = getInventoryByCity(branchNumber);

            for (InventoryItem item : inventory) {
                if (item.getProductIdentifier().equals(productId)) {

                    // should i wrap this with try - catch?
                    // This may throw IllegalArgumentException or InsufficientQuantityException
                    item.reduceQuantity(qty);

                    try (FileWriter writer = new FileWriter(path)) {
                        gson.toJson(inventory, writer);
                    }

                    return;
                }
            }
        }
    }



    public boolean hasSufficientStock(Integer branchNumber, String productId, int qty) {
        boolean hasSufficientStock = false;
        List<InventoryItem> inventory = getInventoryByCity(branchNumber);

        for (InventoryItem item : inventory) {
            if (item.getProductIdentifier().equals(productId)) {
                hasSufficientStock =  item.getQuantity() >= qty;
                break;
            }
        }

        return hasSufficientStock;
    }

    public void addStock(Integer branchNumber, String productId, int qty) throws IOException {
        if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive");

        String city = branchNumberToCity.get(branchNumber);
        String path = getBranchFilePath(branchNumber);

        synchronized (getBranchLock(city)) {
            List<InventoryItem> inventory = getInventoryByCity(branchNumber);
            boolean found = false;

            for (InventoryItem item : inventory) {
                if (item.getProductIdentifier().equals(productId)) {
                    item.addQuantity(qty);
                    found = true;
                    break;
                }
            }

            if (!found) {
                inventory.add(new InventoryItem(productId, qty));
            }

            try (FileWriter writer = new FileWriter(path)) {
                gson.toJson(inventory, writer);
            }
        }
    }

    private Object getBranchLock(String city) {
        return fileLocks.get(city);
    }

    private String getBranchFilePath(Integer branchNumber) {
        return  INVENTORY_DIR + branchNumberToCity.get(branchNumber);
    }
}

