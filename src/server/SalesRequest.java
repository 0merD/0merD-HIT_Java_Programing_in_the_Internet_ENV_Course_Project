package server;

// Data Transfer Object
public class SalesRequest {

    private int branchNumber;
    private String productId;
    private int quantity;
    private CustomerAbstract customer;

    public void setBranchNumber(int branchNumber) {
        this.branchNumber = branchNumber;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCustomer(CustomerAbstract customer) {
        this.customer = customer;
    }

    public int getBranchNumber() {
        return branchNumber;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public CustomerAbstract getCustomer() {
        return customer;
    }
}
