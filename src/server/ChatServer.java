//package server;
//
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.Date;
//
///**
// * The main server class.
// * Its sole responsibility is to listen for and accept incoming client connections.
// */
//public class ChatServer {
//    private static final int PORT = 7000;
//
//    public static void main(String[] args) throws IOException {
//        // Create a single state object to be shared among all client handlers
//        ServerState serverState = new ServerState();
//
//        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//            System.out.println(new Date() + " Server started on port " + PORT + ". Waiting for connections...");
//
//            while (true) {
//                Socket clientSocket = serverSocket.accept();
//                // Create a new handler for the client and run it in a new thread
//                ClientHandler clientHandler = new ClientHandler(clientSocket, serverState);
//                new Thread(clientHandler).start();
//            }
//        }
//    }
//}