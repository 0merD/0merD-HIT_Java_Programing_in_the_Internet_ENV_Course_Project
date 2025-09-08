package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int APP_PORT = 1234;
    private static boolean isServerRunning = true;

    //Todo: find a way to remove this line.
    private static UserManager userManagerSingletonInstance = UserManager.getInstance(); // initializes the UserManager singleton Instance.

    public static void main(String[] args) throws IOException {
        // Single ServerState for all clients to share chat presence/sessions
        ServerState serverState = new ServerState();

        try  (ServerSocket serverSocket = new ServerSocket(APP_PORT)) {
            System.out.printf("Unified server is listening on port %d. Waiting for clients...%n", APP_PORT);

            while (isServerRunning) {
                final Socket socket = serverSocket.accept(); // blocking
                System.out.printf("Accepted connection from [%s]%n", socket.getInetAddress());
                new ClientLoginHandler(socket, serverState).start();
            }
        }
    }
}