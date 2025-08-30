package server;

// This class holds the data required to calculate discounts within the DiscountStrategyInterface
public class OrderDetails {
    private double totalPrice;
    private int quantity;
    private CustomerTypeEnum customerType;

    public OrderDetails(double totalPrice, int quantity, CustomerTypeEnum customerType) {
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.customerType = customerType;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public CustomerTypeEnum getCustomerType() {
        return customerType;
    }
}
