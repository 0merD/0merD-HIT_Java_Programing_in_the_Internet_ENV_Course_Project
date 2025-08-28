package shared;

public abstract class CustomerAbstract {
    int id;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    ClientTypeEnum clientType;

    public CustomerAbstract(int id, String firstName, String lastName, String email, String phoneNumber, ClientTypeEnum clientType) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.clientType = clientType;
    }

    public abstract void makePurchase();
}
