package server;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogAction {

    private static final String USER_LOG_FILE = "resources/user_log.txt";
    private static final String CUSTOMER_LOG_FILE = "resources/customer_log.txt";

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
    public static void logCustomerAction(String action, String custId, String custType, String status) {
        String logEntry = String.format(
                "%s | Action: %s | CustomerID: %s | CustomerType: %s | Status: %s",
                timestamp(), action, custId, custType, status
        );
        writeLog(CUSTOMER_LOG_FILE, logEntry);
    }

    public static void logCustomerFailure(String action) {
        logCustomerAction(action, "N/A", "N/A", "FAILED");
    }
}