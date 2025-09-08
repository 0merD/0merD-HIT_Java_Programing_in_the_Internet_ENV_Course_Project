package server;

import shared.UserType;

public abstract class User {
    private String username;
    private String id;
    private String password;
    private String email;
    private String phoneNumber;
    private String accountNumber;
    private int branchNumber;
    private UserType userType;

    // Constructor
    public User(String username,
                String id,
                String password,
                String email,
                String phoneNumber,
                String accountNumber,
                int branchNumber,
                UserType userType)
    {
        this.username = username;
        this.id = id;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.accountNumber = accountNumber;
        this.branchNumber = branchNumber;
        this.userType = userType;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public int getBranchNumber() { return branchNumber; }
    public void setBranchNumber(int branchNumber) { this.branchNumber = branchNumber; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    @Override
    public String toString() {
        return String.format("username: [%s] user type: [%s] Email: [%s]", username, userType, email);
    }

}
