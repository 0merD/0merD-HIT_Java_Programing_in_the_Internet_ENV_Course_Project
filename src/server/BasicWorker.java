package server;

import server.enums.UserType;

public class BasicWorker extends User {
    public BasicWorker(String username, String id, String password, String email, String phoneNumber, String accountNumber, int branchNumber, UserType userType) {
        super(username,id, password, email, phoneNumber, accountNumber, branchNumber, userType);
    }
}
