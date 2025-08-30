package server;

//Todo: Delete this class

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TempTestTwoBranchesInventories {

    public static void main(String[] args) {
        Path inventoryPathTelaviv = Paths.get("resources", "telaviv.json");
        Path inventoryPathHaifa = Paths.get("resources", "haifa.json");
        // Create branches with JSON file paths
        Branch telAvivBranch = new Branch("Tel Aviv", 1, inventoryPathTelaviv.toAbsolutePath().toString());
        Branch haifaBranch = new Branch("Haifa", 2, inventoryPathHaifa.toAbsolutePath().toString());

        // Sample products
        // Sample clothing products
        Product tshirt = new Product("tshirt001", "T-Shirt", "Cotton unisex T-shirt", 25.0);
        Product jeans = new Product("jeans001", "Jeans", "Blue denim jeans", 50.0);


        // Add items to Tel Aviv branch
        telAvivBranch.getInventory().addItem(tshirt, 50);
        telAvivBranch.getInventory().addItem(jeans, 50);

//        // Add items to Haifa branch
//        haifaBranch.getInventory().addItem(tshirt, 200);
//        haifaBranch.getInventory().addItem(jeans, 75);

        System.out.println("Initial inventories:");
        printInventory(telAvivBranch);
        printInventory(haifaBranch);

        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Thread 1: Remove 30 T-Shirts from Tel Aviv
        executor.submit(() -> {
            try {
                boolean removed = telAvivBranch.getInventory().removeItem("tshirt001", 30);
                System.out.println("Tel Aviv: Removed 30 T-Shirts? " + removed);
            } catch (InventoryItem.InsufficientQuantityException e) {
                System.out.println("Tel Aviv: Not enough T-Shirts to remove!");
            }
        });

        // Thread 2: Remove 50 Jeans from Tel Aviv
        executor.submit(() -> {
            try {
                boolean removed = telAvivBranch.getInventory().removeItem("jeans001", 50);
                System.out.println("Tel Aviv: Removed 50 Jeans? " + removed);
            } catch (InventoryItem.InsufficientQuantityException e) {
                System.out.println("Tel Aviv: Not enough Jeans to remove!");
            }
        });

        // Thread 3: Remove 30 T-Shirts from Tel Aviv
        executor.submit(() -> {
            try {
                boolean removed = telAvivBranch.getInventory().removeItem("tshirt001", 30);
                System.out.println("Tel Aviv: Removed 30 T-Shirts? " + removed);
            } catch (InventoryItem.InsufficientQuantityException e) {
                System.out.println("Tel Aviv: Not enough T-Shirts to remove!");
            }
        });

        // Thread 4: Remove 40 Jeans from Tel Aviv
        executor.submit(() -> {
            try {
                boolean removed = telAvivBranch.getInventory().removeItem("jeans001", 40);
                System.out.println("Tel Aviv: Removed 40 Jeans? " + removed);
            } catch (InventoryItem.InsufficientQuantityException e) {
                System.out.println("Tel Aviv: Not enough Jeans to remove!");
            }
        });

        // Shutdown executor and wait for all tasks to finish
        executor.shutdown();
        while (!executor.isTerminated()) {
            // wait
        }

        System.out.println("\nInventory after concurrent removals:");
        printInventory(telAvivBranch);

//        // Thread 3: Remove 100 bananas from Haifa
//        executor.submit(() -> {
//            try {
//                boolean removed = haifaBranch.getInventory().removeItem("jeans001", 100);
//                System.out.println("Haifa: Removed 100 jeans001? " + removed);
//            } catch (InventoryItem.InsufficientQuantityException e) {
//                System.out.println("Haifa: Not enough jeans001 to remove!");
//            }
//        });
//
//        // Thread 4: Remove 50 bananas from Haifa
//        executor.submit(() -> {
//            try {
//                boolean removed = haifaBranch.getInventory().removeItem("jeans001", 50);
//                System.out.println("Haifa: Removed 50 jeans001? " + removed);
//            } catch (InventoryItem.InsufficientQuantityException e) {
//                System.out.println("Haifa: Not enough jeans001 to remove!");
//            }
//        });

        // Shutdown executor and wait for tasks to finish
        executor.shutdown();
        while (!executor.isTerminated()) {
            // wait
        }

        System.out.println("\nInventories after concurrent removals:");
        printInventory(telAvivBranch);
        printInventory(haifaBranch);
    }

    private static void printInventory(Branch branch) {
        System.out.println("Branch " + branch.getInventory() + " items:");
        branch.getInventory().getItemsMap().forEach((id, item) -> {
            System.out.println("  " + item.getProduct().getName() + ": " + item.getQuantity());
        });
    }
}
