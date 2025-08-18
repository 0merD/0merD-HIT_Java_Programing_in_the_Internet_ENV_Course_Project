package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    public static void main(String[] args) throws IOException {

        Path resourcePath = Paths.get("resources", "users.json"); // relative to project root
        UserManager um = new UserManager(resourcePath.toString());

        int port = 1234;
        final ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started. Waiting for clients...");

        // Infinite loop: server always waiting for connections
        while (true) {
            final Socket socket = serverSocket.accept();
            new Thread(new Runnable() {
                public void run() {
                    handleClient(socket, um);
                }
            }).start();
        }
    }

    private static void handleClient(Socket clientSocket, UserManager userManager) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            // Read the user ID sent by client
            String userId = in.readLine();
            System.out.println("Received user ID: " + userId);

            // Read the password sent by client
            String password = in.readLine();
            System.out.println("Received password for user: " + userId);

            // Authenticate with both user ID and password
            User user = userManager.authenticateByWorkerId(userId, password);

            if (user != null) {
                // Send success response with role
                String role = user.getUserType().toString(); // Assuming getUserType() exists
                out.println("true," + role);
                System.out.println("Authentication successful for user ID: " + userId + ", Role: " + role);

                // Now you can continue with authenticated session
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received from client (" + userId + "): " + message);
                    out.println("Echo: " + message);
                }
            } else {
                // Send failure response
                out.println("false,");
                System.out.println("Authentication failed for user ID: " + userId);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client connection closed.");
            } catch (IOException ignored) {}
        }
    }
}