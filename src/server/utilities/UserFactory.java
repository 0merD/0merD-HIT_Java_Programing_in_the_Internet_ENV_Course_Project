package server.utilities;

import server.BasicWorker;
import server.ShiftManager;
import server.User;
import server.Admin;
import shared.UserType;

public class UserFactory {

    // Factory method to create a User
    public static User createUser(String username, String id, UserType userType,  String password,
                                  String email, String phoneNumber,
                                  String accountNumber, int branchNumber) {

        switch (userType) {
            case UserType.Admin:
                return new Admin(username, id, password, email, phoneNumber, accountNumber, branchNumber, userType);

            case UserType.ShiftManager:
                return new ShiftManager(username, id, password, email, phoneNumber, accountNumber, branchNumber, userType);

            case UserType.BasicWorker:
                return new BasicWorker(username,id, password, email, phoneNumber, accountNumber, branchNumber, userType);

            default:
                throw new IllegalArgumentException("Unknown user type: " + userType);
        }
    }
}
