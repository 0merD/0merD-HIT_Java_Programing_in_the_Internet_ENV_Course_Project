package server;

public abstract class CustomerAbstract {

    private String fullName;
    private String custId; // ת.ז
    private String phoneNumber;
    private CustomerTypeEnum customerType;


    public CustomerAbstract(String fullName, String idNumber, String phoneNumber, CustomerTypeEnum clientType) {
        this.fullName = fullName;
        this.custId = idNumber;
        this.phoneNumber = phoneNumber;
        this.customerType = clientType;
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

    public void setCustomerType(CustomerTypeEnum clientType) {
        this.customerType = clientType;
    }

    @Override
    public String toString() {
        return String.format("Customer{id=%s, name=%s, phone=%s, type=%s}",
                custId, fullName, phoneNumber, customerType);
    }
}
