package server;

public class InventoryItem {
    private Product product;
    private int quantity;

    public InventoryItem() {}

    public InventoryItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
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

    public void removeQuantity(int amount) throws InsufficientQuantityException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to remove must be positive");
        }
        if (amount > quantity) {
            throw new InsufficientQuantityException(
                String.format("Cannot remove %d from inventory; only %d available", amount, quantity)
            );
        }
        quantity -= amount;
    }


    public static class InsufficientQuantityException extends Exception {
        public InsufficientQuantityException(String message) {
            super(message);
        }
    }
}
