package server;

import java.util.Map;

public abstract class CustomerAbstract {

    private String fullName;
    private String custId; // "C001"
    private String phoneNumber;
    private CustomerTypeEnum customerType;
    private double totalSpent;

    protected transient CustomerDiscountsRegistry customerDiscountsRegistry; // transient = avoid serializing to json.

    public CustomerAbstract(String fullName, String idNumber, String phoneNumber, CustomerTypeEnum clientType, double totalSpent) {
        this.fullName = fullName;
        this.custId = idNumber;
        this.phoneNumber = phoneNumber;
        this.customerType = clientType;
        this.totalSpent = totalSpent;
        this.customerDiscountsRegistry = new CustomerDiscountsRegistry();
        customerDiscountsRegistry.addStrategy("No Discount", new DiscountStrategyNoDiscount());
    }


    public abstract double applyBestDiscount(OrderDetails orderDetails);

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public CustomerTypeEnum getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerTypeEnum customerType) {
        this.customerType = customerType;
    }

    public CustomerDiscountsRegistry getCustomerDiscountsRegistry() {
        return customerDiscountsRegistry;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public void addSpent(double amount) {
        setTotalSpent(getTotalSpent() + amount);
        CustomerManager.promoteCustomer(this); // central promotion logic
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    @Override
    public String toString() {
        return String.format("Customer{id=%s, name=%s, phone=%s, type=%s}",
                custId, fullName, phoneNumber, customerType);
    }


}
