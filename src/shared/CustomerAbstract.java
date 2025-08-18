package shared;

public class CustomerAbstract {
    int id;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    Enum<ClientType> clientType;

    public CustomerAbstract(int id, String firstName, String lastName, String email, String phoneNumber, Enum<ClientType> clientType) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.clientType = clientType;
    }


}
