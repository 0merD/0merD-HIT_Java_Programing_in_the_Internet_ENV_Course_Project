package server;

public class InventoryItem {
    private String productIdentifier;
    private int quantity;

    public InventoryItem() {}

    public InventoryItem(String productIdentifier, int quantity) {

        if (!ProductsCatalog.existsProductIdentifier(productIdentifier)) {
            throw new IllegalArgumentException("Invalid product identifier: " + productIdentifier);
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        this.productIdentifier = productIdentifier;
        this.quantity = quantity;
    }

    public String getProductIdentifier() {
        return productIdentifier;
    }

    public Product getProduct() {
        return ProductsCatalog.getProduct(productIdentifier);
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive");
        }

        quantity += amount;
    }

    public void reduceQuantity(int qtyToReduce) throws InsufficientQuantityException {

        if (qtyToReduce <= 0) {
            throw new IllegalArgumentException("Amount to remove must be positive");
        }

        if (qtyToReduce > quantity) {
            throw new InsufficientQuantityException(
                String.format("Cannot remove %d from inventory; only %d available", qtyToReduce, quantity)
            );
        }
        quantity -= qtyToReduce;
    }


    public static class InsufficientQuantityException extends Exception {
        public InsufficientQuantityException(String message) {
            super(message);
        }
    }
}
