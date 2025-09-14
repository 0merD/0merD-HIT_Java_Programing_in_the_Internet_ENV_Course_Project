package server;

import server.customertypes.CustomerAbstract;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BusinessLogger {

    private static final String USER_LOG_FILE = "logs/user_log.txt";
    private static final String CUSTOMER_LOG_FILE = "logs/customer_log.txt";
    private static final String SALES_LOG_FILE = "logs/sales_log.txt";

    // Ensure directories exist
    static {
        try {
            Files.createDirectories(Paths.get("resources/inventories"));
        } catch (IOException e) {
            System.err.println("Failed to create log directories: " + e.getMessage());
        }
    }

    private static void writeLog(String logFile, String message) {
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(message + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Failed to write to " + logFile + ": " + e.getMessage());
        }
    }

    private static String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ---------- USER LOGS ----------
    public static void logUserAction(String action, String username, String userType, String status) {
        String logEntry = String.format(
                "%s | Action: %s | Username: %s | UserType: %s | Status: %s",
                timestamp(), action, username, userType, status
        );
        writeLog(USER_LOG_FILE, logEntry);
    }

    // Not in requirements but we still thought to include this in the submission.
    public static void logUserRoleChange(String username, String oldRole, String newRole, String status) {
        String logEntry = String.format(
                "%s | Action: Update User Role | Username: %s | OldType: %s | NewType: %s | Status: %s",
                timestamp(), username, oldRole, newRole, status
        );
        writeLog(USER_LOG_FILE, logEntry);
    }



    public static void logUserFailure(String action) {
        logUserAction(action, "N/A", "N/A", "FAILED");
    }

    // ---------- CUSTOMER LOGS ----------
    public static void logAddCustomer(String action, String custId, String custType, String status) {
        String logEntry = String.format(
                "%s | Action: %s | CustomerID: %s | CustomerType: %s | Status: %s",
                timestamp(), action, custId, custType, status
        );
        writeLog(CUSTOMER_LOG_FILE, logEntry);
    }

    public static void logCustomerFailure(String action) {
        logAddCustomer(action, "N/A", "N/A", "FAILED");
    }

    // ---------- SALES LOG ------------------
    public static void logSalesAction(CustomerAbstract customer, SalesResult salesResult, OrderDetails orderDetails) {
        String logEntry = String.format("%s | Customer Name: %s | Product Name: %s | Quantity: %s | Succesfull Sale: %s | Discount Applied: %s | Final Price: %s",
                timestamp(),
                customer.getFullName(),
                orderDetails.getProduct().getName(),
                orderDetails.getQuantity(),
                salesResult.isSuccess(),
                salesResult.getDiscountApplied(),
                salesResult.getFinalPrice());
        writeLog(SALES_LOG_FILE, logEntry);
    }
}