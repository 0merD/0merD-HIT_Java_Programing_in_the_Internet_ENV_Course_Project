package server;

import server.customertypes.CustomerAbstract;

public class OrderDetails {
    private double totalPrice;
    private int quantity;
    private Product product;
    private CustomerAbstract customer;

    public OrderDetails(double totalPrice, int quantity, CustomerAbstract customer, Product product) {
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.customer = customer;
        this.product = product;
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

    public Product getProduct() {
        return product;
    }
}
