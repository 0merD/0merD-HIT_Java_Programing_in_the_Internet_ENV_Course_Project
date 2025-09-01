package server;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

/**
 * Manages a single client connection in its own thread.
 * Listens for incoming messages and passes them to the CommandHandler.
 */
public class ClientHandler implements Runnable {
    private final SocketData clientData;
    private final ServerState serverState;
    private final CommandHandler commandHandler;

    public ClientHandler(Socket socket, ServerState serverState) {
        this.clientData = new SocketData(socket);
        this.serverState = serverState;
        this.commandHandler = new CommandHandler(serverState);
    }

    @Override
    public void run() {
        try {
            serverState.addClient(clientData);
            System.out.println("Client " + clientData.getClientAddress() + " connected at " + new Date());
            clientData.getOutputStream().println("Welcome! Use 'list', 'listall', 'chat <id>', or 'join <id>'.");

            String line;
            while ((line = clientData.getInputStream().readLine()) != null) {
                commandHandler.handle(clientData, line);
            }
        } catch (IOException e) {
            System.out.println("Client " + clientData.getClientAddress() + " disconnected unexpectedly.");
        } finally {
            System.out.println("Cleaning up connection for " + clientData.getClientAddress());
            commandHandler.handleDisconnection(clientData);
            serverState.removeClient(clientData);
            try {
                clientData.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}