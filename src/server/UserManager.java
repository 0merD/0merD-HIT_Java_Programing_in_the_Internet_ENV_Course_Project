package server;

import com.google.gson.GsonBuilder;
import shared.UserType;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public class UserManager {

    // Path to users.json
    private static final Path JSON_FILE_PATH =  Paths.get("resources", "users.json");
    // Singleton
    private static UserManager instance;

    // <Key, Value> = <username, User>
    private Map<String, User> users = new HashMap<>();

    private final Object lockUsersFile = new Object();


    private UserManager() {
        loadUsersFromJson(JSON_FILE_PATH.toString());
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }

        return instance;
    }

    public boolean addUser(User user) throws IllegalArgumentException, IOException {
        if (user == null || user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Invalid user object or username.");
        }
        if (user.getUserType() == null) {
            throw new IllegalArgumentException("User role cannot be null.");
        }

        synchronized (lockUsersFile) {
            if (users.containsKey(user.getUsername())) {
                throw new IllegalArgumentException("User with username '" + user.getUsername() + "' already exists.");
            }

            users.put(user.getUsername(), user);

            try {
                saveUsersToJson();
                return true;
            } catch (IOException e) {
                users.remove(user.getUsername()); // rollback
                throw new IOException("Failed to save user to JSON.", e);
            }
        }
    }

    public boolean deleteUser(String username) throws IllegalArgumentException, IOException {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Invalid username.");
        }

        synchronized (lockUsersFile) {
            User removed = users.remove(username);
            if (removed == null) {
                throw new IllegalArgumentException("User with username '" + username + "' does not exist.");
            }

            try {
                saveUsersToJson(); // persist the change
                return true;
            } catch (IOException e) {
                users.put(username, removed); // rollback
                throw new IOException("Failed to save users to JSON.", e);
            }
        }
    }


    public boolean modifyUserRole(String username, UserType newRole) throws IllegalArgumentException, IOException {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Invalid username.");
        }
        if (newRole == null) {
            throw new IllegalArgumentException("User role cannot be null.");
        }

        synchronized (lockUsersFile) {
            User user = users.get(username);
            if (user == null) {
                throw new IllegalArgumentException("User with username '" + username + "' does not exist.");
            }

            UserType oldRole = user.getUserType();
            user.setUserType(newRole);

            try {
                saveUsersToJson(); // persist the change
                return true;
            } catch (IOException e) {
                user.setUserType(oldRole); // rollback
                throw new IOException("Failed to save users to JSON.", e);
            }
        }
    }

    private void loadUsersFromJson(String jsonFilePath) {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(jsonFilePath)) {

            Type userListType = new TypeToken<List<UserJson>>(){}.getType();

            List<UserJson> userList = gson.fromJson(reader, userListType);

            for (UserJson u : userList) {
                User user = UserFactory.createUser(
                        u.username,
                        u.id,
                        u.userType,
                        u.password,
                        u.email,
                        u.phoneNumber,
                        u.accountNumber,
                        u.branchNumber);
                if (user != null) {
                    users.put(u.username, user);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUsersToJson() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        List<UserJson> userList = users.values().stream()
                .map(UserJson::new)
                .collect(Collectors.toList());

        try (FileWriter writer = new FileWriter(JSON_FILE_PATH.toString())) {
            gson.toJson(userList, writer);
        } catch (IOException e) {
            System.out.println("Failed to save users to JSON.");
            e.printStackTrace();
        }
    }

    public static boolean authenticate(String username, String password) {
        boolean authenticated = true;
        User user = instance.getUserByUserName(username);

        if (user == null)
        {
            authenticated =  false;
        }
        else if (!user.getPassword().equals(password)) {
            authenticated = false;
        }

        return authenticated;
    }

    public User getUserByUserName(String username) {
        return users.get(username); // returns null if key not in Map
    }

    public List<User> getAllUsers() {
        synchronized (lockUsersFile) {
            // Return a copy of the user list to avoid external modification
            return new ArrayList<>(users.values());

        }
    }

    // Helper class for JSON mapping
    private static class UserJson {
        public String username;
        public String id;
        public String password;
        public String email;
        public String phoneNumber;
        public String accountNumber;
        public int branchNumber;
        public UserType userType;

        public UserJson(User user) {
            this.username = user.getUsername();
            this.id = user.getId();
            this.password = user.getPassword();
            this.email = user.getEmail();
            this.phoneNumber = user.getPhoneNumber();
            this.accountNumber = user.getAccountNumber();
            this.branchNumber = user.getBranchNumber();
            this.userType = user.getUserType();
        }

        // default constructor needed for Gson
        public UserJson() {}
    }
}

