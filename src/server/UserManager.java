package server;

import server.utilities.UserFactory;
import shared.UserType;
import client.utilities.SecurityLogic;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class UserManager {

    private final Map<String, User> users = new HashMap<>(); // workerId -> User

    public UserManager(String jsonFilePath) {
        loadUsersFromJson(jsonFilePath);
    }

    private void loadUsersFromJson(String jsonFilePath) {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(jsonFilePath)) {
            Type userListType = new TypeToken<List<UserJson>>() {}.getType();
            List<UserJson> userList = gson.fromJson(reader, userListType);

            for (UserJson u : userList) {
                // Hash the plain-text password before storing
                String hashedPassword = SecurityLogic.hashPassword(u.password);

                // Create user with hashed password
                User user = UserFactory.createUser(
                        u.userType,
                        u.id,
                        hashedPassword,
                        u.email,
                        u.phoneNumber,
                        u.accountNumber,
                        u.branchNumber
                );

                if (user != null) {
                    users.put(u.id, user);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUserByWorkerId(String workerId) {
        return users.get(workerId);
    }

    public User authenticateByWorkerId(String workerId, String hashedPasswordFromClient) {
        User user = getUserByWorkerId(workerId);
        if (user != null && hashedPasswordFromClient.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    // Helper class for JSON mapping
    private static class UserJson {
        public String id;
        public String password; // plain text from JSON
        public String email;
        public String phoneNumber;
        public String accountNumber;
        public int branchNumber;
        public UserType userType;
    }
}
