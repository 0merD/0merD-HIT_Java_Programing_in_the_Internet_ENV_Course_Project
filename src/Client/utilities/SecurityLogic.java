package client.utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityLogic {

    public static boolean isValidWorkerId(String workerId) {
        return workerId != null && !workerId.trim().isEmpty() && workerId.matches("\\d+");
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        return hasDigit && hasSpecial;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
