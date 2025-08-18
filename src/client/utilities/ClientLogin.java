package client.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
            String forbiddenChars = "[^0-9]";

            while (true) {
                System.out.print("Enter your Worker ID (9 digits): ");
                workerId = scanner.nextLine().trim();

                if (workerId.isEmpty()) {
                    System.out.println("Worker ID cannot be empty. Try again.");
                    continue;
                }

                if (workerId.length() != 9) {
                    System.out.println("Worker ID must be exactly 9 digits. Try again.");
                    continue;
                }

                if (workerId.matches(".*" + forbiddenChars + ".*")) {
                    System.out.println("Worker ID contains invalid characters. Only digits allowed.");
                    continue;
                }
                break;
            }

            out.println(workerId);

            String response = in.readLine();
            if (response == null) {
                System.out.println("No response from server.");
                return;
            }

            String[] parts = response.split(",");
            boolean success = Boolean.parseBoolean(parts[0]);

            if (!success) {
                System.out.println("Login failed. Invalid worker ID.");
            } else {
                String role = parts[1];
                System.out.println("Login successful! Your role is: " + role);
            }

        } catch (UnknownHostException e) {
            System.out.println("Could not resolve host: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }
}