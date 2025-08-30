package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class InventoryManager {
    // <BranchNumber, BranchFileName>
    private static final Map<Integer, String> branchNumberToCity = new HashMap<>();

    private static InventoryManager instance; // Singleton Instance

    // <BranchNumber, List<InventoryItems>
    private final Map<Integer, List<InventoryItem>> inventoryCache = new HashMap<>();

    private final Map<String, Object> fileLocks = new HashMap<>(); // Have a separate lock for each branch json file.

    private final String INVENTORY_DIR = "resources/inventories/";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private InventoryManager() {
        // was put here for simplicity.
        branchNumberToCity.put(1, "telaviv");
        branchNumberToCity.put(2, "haifa");
    }

    public static synchronized InventoryManager getInstance() {
        if (instance == null) {
            instance = new InventoryManager();
        }
        return instance;
    }

    public List<InventoryItem> getInventoryByCity(Integer branchNumber) {
        String cityName = branchNumberToCity.get(branchNumber);
        File file = new File(INVENTORY_DIR + cityName + ".json");
        if (!file.exists()) return new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<InventoryItem>>(){}.getType();

            return gson.fromJson(reader, listType);

        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // TODO: Delete Main for testing purposes only
    public static void main(String[] args) {
        InventoryManager inventoryManager = InventoryManager.getInstance();

        // Example: test Tel Aviv branch
        System.out.println("=== Tel Aviv Branch Inventory ===");
        List<InventoryItem> telAvivInventory = inventoryManager.getInventoryByCity(1);
        if (telAvivInventory.isEmpty()) {
            System.out.println("No inventory found.");
        } else {
            for (InventoryItem item : telAvivInventory) {
                System.out.printf("%s - %d units\n", item.getProduct().getName(), item.getQuantity());
            }
        }

        // Example: test Haifa branch
        System.out.println("\n=== Haifa Branch Inventory ===");
        List<InventoryItem> haifaInventory = inventoryManager.getInventoryByCity(2);
        if (haifaInventory.isEmpty()) {
            System.out.println("No inventory found.");
        } else {
            for (InventoryItem item : haifaInventory) {
                System.out.printf("%s - %d units\n", item.getProduct().getName(), item.getQuantity());
            }
        }
    }

}

