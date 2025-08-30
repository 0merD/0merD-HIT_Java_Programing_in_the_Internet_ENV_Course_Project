package server;

public class CustomerNew extends CustomerAbstract{
    public CustomerNew(String fullName, String idNumber, String phoneNumber, CustomerTypeEnum customerType) {
        super(fullName, idNumber, phoneNumber, customerType);
    }

    @Override
    public void purchase(double amount) {

    }


}
