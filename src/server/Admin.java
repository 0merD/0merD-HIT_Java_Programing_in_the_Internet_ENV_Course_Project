package server;

import server.utilities.UserFactory;
import shared.UserType;

public class Admin extends User {
    public Admin(String username, String id, String password, String email, String phoneNumber, String accountNumber, int branchNumber, UserType userType) {
        super(username, id,  password, email, phoneNumber, accountNumber, branchNumber, userType);
    }
}
