package server;

import shared.UserType;

public class CustomerFactory {
    // Factory method to create a User
    public static CustomerAbstract createCustomer(String custId, String fullName, String phoneNumber, CustomerTypeEnum customerType) {

        switch (customerType) {
            case CustomerTypeEnum.New:
                return new CustomerNew(fullName, custId,  phoneNumber,  customerType);

            case CustomerTypeEnum.Returning:
                return new CustomerReturning(fullName, custId,  phoneNumber,  customerType);

            case CustomerTypeEnum.Vip:
                return new CustomerVip(fullName, custId,  phoneNumber,  customerType);

            default:
                throw new IllegalArgumentException("Unknown user type: " + customerType);
        }
    }

}
