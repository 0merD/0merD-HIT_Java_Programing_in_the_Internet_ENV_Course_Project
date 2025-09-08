package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int APP_PORT = 1234;
    private static final int CHAT_PORT = 7000;
    private static boolean isServerRunning = true;

    //Todo: find a way to remove this line.
    private static UserManager userManagerSingletonInstance = UserManager.getInstance(); // initializes the UserManager singleton Instance.

    public static void main(String[] args) throws IOException {
        // Start chat server acceptor (merged ChatServer) on a dedicated thread
        Thread chatAcceptor = new Thread(() -> {
            ServerState serverState = new ServerState();
            try (ServerSocket chatServerSocket = new ServerSocket(CHAT_PORT)) {
                System.out.printf("Chat server started on port %d. Waiting for chat connections...%n", CHAT_PORT);
                while (true) {
                    Socket clientSocket = chatServerSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket, serverState);
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                System.err.println("Chat server stopped: " + e.getMessage());
            }
        }, "ChatAcceptor");
        chatAcceptor.setDaemon(true);
        chatAcceptor.start();

        // Original app server (login, etc.) remains on APP_PORT
        try  (ServerSocket serverSocket = new ServerSocket(APP_PORT)) {
            System.out.printf("App server is listening on port %d Waiting for clients...%n", APP_PORT);

            while (isServerRunning) {
                final Socket socket = serverSocket.accept(); // blocking
                System.out.printf("Accepted app connection from [%s]", socket.getInetAddress());
                new ClientLoginHandler(socket).start();
            }
        }
    }
}