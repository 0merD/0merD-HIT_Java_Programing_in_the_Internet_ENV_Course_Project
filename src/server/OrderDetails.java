package server;

// This class holds the data required to calculate discounts within the DiscountStrategyInterface
public class OrderDetails {
    private double totalPrice;
    private int quantity;
    private CustomerAbstract customer;

    public OrderDetails(double totalPrice, int quantity, CustomerAbstract customer) {
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.customer = customer;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public CustomerAbstract getCustomer() {
        return customer;
    }
}
