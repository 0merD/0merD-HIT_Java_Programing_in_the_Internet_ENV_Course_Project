package shared;

public class CustomerReturning extends CustomerAbstract {

    public CustomerReturning(int id, String firstName, String lastName, String email, String phoneNumber, ClientTypeEnum clientType) {
        super(id, firstName, lastName, email, phoneNumber, clientType);
    }

    @Override
    public void makePurchase() {
    }


}
