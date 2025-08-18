package server;

import shared.UserType;

public class BasicWorker extends User {
    public BasicWorker(String username, String password, String email, String phoneNumber, String accountNumber, int branchNumber, UserType userType) {
        super(username, password, email, phoneNumber, accountNumber, branchNumber, userType);
    }
}
