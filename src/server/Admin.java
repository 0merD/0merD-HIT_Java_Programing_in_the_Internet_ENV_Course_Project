package server;

import server.utilities.UserFactory;
import shared.UserType;

public class Admin extends User {
    public Admin(String username, String password, String email, String phoneNumber, String accountNumber, int branchNumber, UserType userType) {
        super(username, password, email, phoneNumber, accountNumber, branchNumber, userType);
    }



    // Todo: add user
    public void addUser(UserType userType, String username, String password,
                        String email, String phoneNumber,
                        String accountNumber, int branchNumber)
    {
        UserFactory.createUser(userType, username, password, email, phoneNumber, accountNumber, branchNumber);
        //Todo: save user to json file.
    }

    public void deleteUser(int userId)
    {
        //Todo: delete user from json file.
    }

    // Todo: change user permissions
    public void changeUserPermission(UserType userType, int userId)
    {
        // Change user permissions in json file.
    }

    // Todo: Authenticate user.
}
