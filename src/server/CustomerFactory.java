package server;

import shared.UserType;

public class CustomerFactory {
    // Factory method to create a User
    public static CustomerAbstract createCustomer(
            String fullName,
            String custId,
            String phoneNumber,
            CustomerTypeEnum customerType,
            double totalSpent) {

        switch (customerType) {
            case CustomerTypeEnum.New:
                return new CustomerNew(fullName, custId,  phoneNumber,  customerType, totalSpent);

            case CustomerTypeEnum.Returning:
                return new CustomerReturning(fullName, custId,  phoneNumber,  customerType, totalSpent);

            case CustomerTypeEnum.Vip:
                return new CustomerVip(fullName, custId,  phoneNumber,  customerType, totalSpent);

            default:
                throw new IllegalArgumentException("Unknown user type: " + customerType);
        }
    }
}
