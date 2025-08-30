package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TempTestInventory {

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Path inventoryPath = Paths.get("resources", "telaviv.json");

        // 1. Try to load existing inventory from JSON
        Branch branch;
        File jsonFile = new File(inventoryPath.toString());
        if (jsonFile.exists()) {
            branch = mapper.readValue(jsonFile, Branch.class);
            System.out.println("Loaded branch from JSON.");
        } else {
            System.out.println("No branch found.");
            throw new IOException("No branch found.");
//            branch = new Branch("Downtown", 1, jsonFilePath);
//            System.out.println("Created new branch.");
        }

        // 2. Create some products
        Product tshirt = new Product("P1Shirt",  "TShirt Noam","T-Shirt", 29.99);
        Product jeans = new Product("P2Shirt", "Jeans Avraham", "Jeans", 59.99);

        // 3. Add products to branch inventory
//        branch.getInventory().addItem(tshirt, 10);
//        branch.getInventory().addItem(jeans, 5);

        // 4. Save the updated inventory to JSON
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, branch);
        System.out.println("Saved branch inventory to JSON.");

        // 5. Print inventory
        printInventory(branch);

        try{
            branch.getInventory().removeItem("P2Shirt", 2);
        }
        catch(Exception e){
            System.out.println("Failed to remove item from Inventory.");
        }


        System.out.println("\nAfter selling 3 T-Shirts:");
        printInventory(branch);

        // 7. Save changes again
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, branch);
        System.out.println("Updated branch inventory saved to JSON.");
    }

    private static void printInventory(Branch branch) {
        Branch.BranchInventory inventory  = branch.getInventory();
        Map<String, InventoryItem> itemsMap = inventory.getItemsMap();
        for (Map.Entry<String, InventoryItem> entry : itemsMap.entrySet()) {
            InventoryItem item = entry.getValue();
            System.out.println(item.toString());
        }
    }
}
