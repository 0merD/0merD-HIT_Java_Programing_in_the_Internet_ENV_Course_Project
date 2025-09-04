package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 1234;
    private static boolean isServerRunning = true;

    //Todo: find a way to remove this line.
    private static UserManager userManagerSingletonInstance = UserManager.getInstance(); // initializes the UserManager singleton Instance.

    public static void main(String[] args) throws IOException {
        try  (ServerSocket serverSocket = new ServerSocket(PORT))
        {
            System.out.printf("Server is listening on port %d Waiting for clients...%n", PORT);

            while (isServerRunning) {
                final Socket socket = serverSocket.accept(); // blocking

                System.out.printf("Accepted connection from [%s]", socket.getInetAddress());

                new ClientLoginHandler(socket).start();
            }
        }
    }
}