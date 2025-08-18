package shared;

public class CustomerNew extends CustomerAbstract{

    public CustomerNew(int id, String firstName, String lastName, String email, String phoneNumber, Enum<ClientType> clientType) {
        super(id, firstName, lastName, email, phoneNumber, clientType);
    }
}
