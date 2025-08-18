//package server;
//
//import java.io.*;
//import java.net.Socket;
//import java.util.List;
//
//public class ClientInstanceHandler implements Runnable {
//    private Socket clientSocket;
//    private UserManager userManager;
//
//    public ClientHandler(Socket clientSocket, UserManager userManager) {
//        this.clientSocket = clientSocket;
//        this.userManager = userManager;
//    }
//
//    @Override
//    public void run() {
//        try (
//                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
//        ) {
//            out.println("Enter username:");
//            String username = in.readLine();
//            out.println("Enter password:");
//            String password = in.readLine();
//
//            User user = userManager.authenticate(username, password);
//            if (user != null) {
//                out.println("Authentication successful! Your allowed operations:");
//                List<String> operations = getOperationsForUser(user);
//                operations.forEach(out::println);
//            } else {
//                out.println("Authentication failed.");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private List<String> getOperationsForUser(User user) {
//        switch (user.getUserType()) {
//            case ADMIN:
//                return List.of("Add Customer", "Delete Customer", "View Inventory");
//            case CUSTOMER:
//                return List.of("View Products", "Place Order");
//            default:
//                return List.of();
//        }
//    }
//}
