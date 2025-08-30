package server;
import java.time.LocalDate;



public class Purchase {
    private LocalDate date;
    private int quantity;
    private double price;
    private int customerId;
     private String customerStatus;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.price = price;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerStatus() {
        return customerStatus;
    }

    public void setCustomerStatus(String customerStatus) {
        this.customerStatus = customerStatus;
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "date=" + date +
                ", quantity=" + quantity +
                ", price=" + price +
                ", customerId=" + customerId +
                ", customerStatus='" + customerStatus + '\'' +
                '}';
    }
}
