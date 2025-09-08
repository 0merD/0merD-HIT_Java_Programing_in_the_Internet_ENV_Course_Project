package server;

import shared.UserType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ValidationsService {

    public static CustomerAbstract requestAndValidateCustomer(BufferedReader input, PrintWriter output) throws IOException {
        try {
            output.println("Enter customer full name:");
            String fullName = validateCustomerName(input.readLine());

            output.println("Enter customer phone number:");
            String phoneNumber = validateTelephoneNumber(input.readLine());

            output.println("Enter customer type (New / Returning / Vip):");
            String typeInput = input.readLine().trim();
            CustomerTypeEnum customerType;
            try {
                customerType = CustomerTypeEnum.valueOf(typeInput);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid customer type.");
            }

            String custId = "C" + (CustomerManager.getInstance().getAllCustomers().size() + 1);

            return CustomerFactory.createCustomer(custId, fullName, phoneNumber, customerType,0);

        } catch (IllegalArgumentException e) {
            output.println("Validation error: " + e.getMessage());
            return null;
        }
    }

    public static User requestAndValidateUser(BufferedReader input, PrintWriter output) throws IOException {
        try {
            output.println("Enter username:");
            String username = validateUsername(input.readLine());

            output.println("Enter id:");
            String id = validateId(input.readLine());

            output.println("Enter password:");
            String password = validatePassword(input.readLine());

            output.println("Enter email:");
            String email = validateEmail(input.readLine());

            output.println("Enter phone number:");
            String phone = validateTelephoneNumber(input.readLine());

            output.println("Enter account number:");
            String accountNumber = input.readLine();

            output.println("Enter branch number:");
            int branchNumber = validateBranchNumber(input.readLine());

            output.println("Enter user type: admin/shiftmanager/basicworker");
            UserType userType = UserType.fromString(input.readLine());

            if (userType == null) {
                output.println("Invalid user type entered.");
                return null;
            }

            return UserFactory.createUser(username, id, userType, password, email, phone, accountNumber, branchNumber);

        } catch (IllegalArgumentException e) {
            output.println("Validation error: " + e.getMessage());
            return null;
        }
    }

    public static String validateUsername(String username) throws IllegalArgumentException {
        if (username == null || username.trim().isEmpty()) throw new IllegalArgumentException("Username cannot be empty.");
        if (username.length() < 3) throw new IllegalArgumentException("Username must be at least 3 characters long.");
        return username.trim();
    }

    public static String validateCustomerName(String name) throws IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Customer name cannot be empty.");
        if (name.length() < 3) throw new IllegalArgumentException("Customer name must be at least 3 characters long.");
        return name.trim();
    }

    public static String validateId(String s) throws IllegalArgumentException {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty.");
        }
        if (!s.matches("\\d{9}")) {
            throw new IllegalArgumentException("ID must be exactly 9 digits.");
        }

        // Check against all existing users
        for (User user : UserManager.getInstance().getAllUsers()) {
            if (user.getId().equals(s.trim())) {
                throw new IllegalArgumentException("ID already exists: " + s);
            }
        }

        return s.trim();
    }
    public static String validatePassword(String password) throws IllegalArgumentException {
        if (password == null) throw new IllegalArgumentException("Password cannot be null.");
        if (password.length() < 8) throw new IllegalArgumentException("Password must be at least 8 characters.");
        if (!password.matches(".*[A-Z].*")) throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
        if (!password.matches(".*[0-9].*")) throw new IllegalArgumentException("Password must contain at least one digit.");
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) throw new IllegalArgumentException("Password must contain at least one special character.");
        return password;
    }

    public static String validateEmail(String email) throws IllegalArgumentException {
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("Invalid email format.");
        return email;
    }

    public static String validateTelephoneNumber(String phone) throws IllegalArgumentException {
        if (phone == null || phone.trim().isEmpty()) throw new IllegalArgumentException("Phone number cannot be empty.");
        phone = phone.trim();
        if (phone.length() != 10) throw new IllegalArgumentException("Phone number must be exactly 10 digits.");
        if (!phone.startsWith("05")) throw new IllegalArgumentException("Phone number must start with 05.");
        if (!phone.matches("\\d{10}")) throw new IllegalArgumentException("Phone number must contain only digits.");
        return phone;
    }

    public static int validateBranchNumber(String branchStr) throws IllegalArgumentException {
        try {
            int branch = Integer.parseInt(branchStr);
            if (branch <= 0) throw new IllegalArgumentException("Branch number must be positive.");
            return branch;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Branch number must be a valid number.");
        }
    }
}
