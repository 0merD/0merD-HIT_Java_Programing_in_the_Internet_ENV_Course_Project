package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 1234;
    private static boolean isServerRunning = true;
    private static ServerState serverState = new ServerState();
    private static UserManager userManagerSingletonInstance = UserManager.getInstance(); // initializes the UserManager singleton Instance.

    public static void main(String[] args) throws IOException {
        try  (ServerSocket serverSocket = new ServerSocket(PORT))
        {
            System.out.printf("Server is listening on port %d Waiting for clients...%n", PORT);

            while (isServerRunning) {
                final Socket clientSocket = serverSocket.accept(); // blocking
                System.out.printf("Accepted connection from [%s]", clientSocket.getInetAddress());

                new ClientLoginHandler(clientSocket,serverState).start();
            }
        }
    }
}