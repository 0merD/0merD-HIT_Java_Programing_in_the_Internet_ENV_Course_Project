package shared;

public class CustomerVIP extends CustomerAbstract {


    public CustomerVIP(int id, String firstName, String lastName, String email, String phoneNumber, Enum<ClientType> clientType) {
        super(id, firstName, lastName, email, phoneNumber, clientType);
    }


}
