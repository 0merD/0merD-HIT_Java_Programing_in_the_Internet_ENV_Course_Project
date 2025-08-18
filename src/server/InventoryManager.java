package server;

public class InventoryManager {

    /**
     * Reduce stock when a purchase is made
     * @param productId the product being bought
     * @param quantity quantity purchased
     */
    public void reduceStock(int productId, int quantity) {
        // logic to reduce stock in database or in-memory map
    }

    /**
     * Increase stock (e.g., when restocking)
     * @param productId
     * @param quantity
     */
    public void increaseStock(int productId, int quantity) {
        // logic to increase stock
    }

    /**
     * Check if a product is available
     * @param productId
     * @param quantity desired quantity
     * @return true if enough stock
     */
    public boolean isAvailable(int productId, int quantity) {
        // check stock
        return true; // placeholder
    }

    /**
     * Get current stock quantity of a product
     * @param productId
     * @return stock quantity
     */
    public int getStock(int productId) {
        return 0; // placeholder
    }
}

