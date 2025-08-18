package server;

import server.utilities.UserFactory;
import shared.UserType;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class UserManager {
//    public static void main(String[] args) {
//        Path resourcePath = Paths.get("resources", "users.json"); // relative to project root
//        UserManager um = new UserManager(resourcePath.toString());
//
//        for (User userJson : um.users.values()) {
//            System.out.println(userJson);
//        }
//    }

    private Map<String, User> users = new HashMap<>(); // Add this for worker ID lookup

    public UserManager(String jsonFilePath) {
        loadUsersFromJson(jsonFilePath);
    }

    private void loadUsersFromJson(String jsonFilePath) {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(jsonFilePath)) {
            // Define the type: List<UserJson>
            Type userListType = new TypeToken<List<UserJson>>(){}.getType();

            List<UserJson> userList = gson.fromJson(reader, userListType);

            for (UserJson u : userList) {
                // Create the user using UserFactory
                User user = UserFactory.createUser(u.userType, u.id, u.password, u.email, u.phoneNumber, u.accountNumber, u.branchNumber);

                // Store the user in both maps
                if (user != null) {
                    users.put(u.id, user);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    // New method to get user by worker ID
    public User getUserByWorkerId(String workerId) {
        return users.get(workerId);
    }

    // Method to authenticate by worker ID with password
    public User authenticateByWorkerId(String workerId, String password) {
        User user = getUserByWorkerId(workerId);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }


    // Helper class for JSON mapping
    private static class UserJson {
        public String id;
        public String password;
        public String email;
        public String phoneNumber;
        public String accountNumber;
        public int branchNumber;
        public UserType userType;
    }
}