package shared;

public class CustomerReturning extends CustomerAbstract {

    public CustomerReturning(int id, String firstName, String lastName, String email, String phoneNumber, Enum<ClientType> clientType) {
        super(id, firstName, lastName, email, phoneNumber, clientType);
    }
}
