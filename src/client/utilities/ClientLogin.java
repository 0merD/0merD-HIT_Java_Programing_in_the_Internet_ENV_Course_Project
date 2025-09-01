package client.utilities;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientLogin {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Socket socket = new Socket("localhost", 1234);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String workerId = "";

            // ✅ Get worker ID with SecurityLogic
            while (true) {
                System.out.print("Enter your Worker ID: ");
                workerId = scanner.nextLine().trim();

                if (!SecurityLogic.isValidWorkerId(workerId)) {
                    System.out.println("Worker ID must be a non-empty number. Try again.");
                    continue;
                }
                break;
            }

            // ✅ Get password with validation
            String password;
            while (true) {
                System.out.print("Enter your password: ");
                password = scanner.nextLine().trim();

                if (!SecurityLogic.isValidPassword(password)) {
                    System.out.println("Password must be at least 8 characters, include a digit and a special character.");
                    continue;
                }
                break;
            }

            // 🔐 Hash before sending
            String hashedPassword = SecurityLogic.hashPassword(password);

            // Send worker ID and hashed password to server
            out.println(workerId);
            out.println(hashedPassword);

            // Read response from server
            String response = in.readLine();
            if (response == null) {
                System.out.println("No response from server.");
                return;
            }

            System.out.println("Server response: " + response); // Debug line

            String[] parts = response.split(",");
            boolean success = Boolean.parseBoolean(parts[0]);

            if (!success) {
                System.out.println("Login failed. Invalid worker ID or password.");
            } else {
                String role = parts.length > 1 ? parts[1] : "Unknown";
                System.out.println("Login successful! Your role is: " + role);

                // Optional: Continue with authenticated session
                System.out.println("You can now send messages to the server. Type 'quit' to exit.");
                String message;
                while (!(message = scanner.nextLine()).equals("quit")) {
                    out.println(message);
                    String serverResponse = in.readLine();
                    if (serverResponse != null) {
                        System.out.println("Server: " + serverResponse);
                    }
                }
            }

        } catch (UnknownHostException e) {
            System.out.println("Could not resolve host: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }
}
