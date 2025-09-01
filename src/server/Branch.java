package server;

import java.util.HashMap;
import java.util.Map;



public class Branch {
    private String branchName;
    private int branchNumber;
    private String branchJsonFilePath;
    private BranchInventory inventory; // each branch has its own inventory

    public Branch() {
        // Jackson needs this to create an instance before setting fields
    }

    public Branch(String branchName, int branchNumber, String branchJsonFilePath) {
        this.branchName = branchName;
        this.branchNumber = branchNumber;
        this.branchJsonFilePath = branchJsonFilePath;
        this.inventory = new BranchInventory(); // initialize inventory
    }

    public BranchInventory getInventory() {
        return inventory;
    }

    // Nested BranchInventory class
    public class BranchInventory {
        private final Object lockAddRemoveProducts = new Object(); // each branch has it's own lock
        private Map<String, InventoryItem> items = new HashMap<>();

        public void addItem(Product product, int quantity) {
            String key = product.getProductStringIdentifier();

            synchronized (lockAddRemoveProducts) {
                InventoryItem existing = items.get(key);

                if (existing == null) {
                    // Product not in inventory yet, create new entry
                    items.put(key, new InventoryItem(product, quantity));
                } else {
                    // Product already exists, increase quantity
                    existing.addQuantity(quantity);
                }
            }
        }

        public boolean removeItem(String productId, int quantity) throws InventoryItem.InsufficientQuantityException {
            synchronized (lockAddRemoveProducts) {
                InventoryItem item = items.get(productId);

                if (item == null) {
                    return false; // item not found
                }

                // Attempt to remove quantity; will throw exception if not enough
                item.removeQuantity(quantity);
                return true;
            }
        }



        public InventoryItem getItem(String productId) {
            return items.get(productId);
        }

        public Map<String, InventoryItem> getItemsMap() {
            return items;
        }
    }
}
