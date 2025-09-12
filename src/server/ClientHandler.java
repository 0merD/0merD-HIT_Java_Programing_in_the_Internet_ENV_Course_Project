//package server;
//
//import server.enums.UserType;
//
//import java.io.IOException;
//import java.net.Socket;
//import java.util.Date;
//
//public class ClientHandler implements Runnable {
//    private final SocketData clientData;
//    private final ServerState serverState;
//    private final CommandHandler commandHandler;
//
//    public ClientHandler(Socket socket, ServerState serverState) {
//        this.clientData = new SocketData(socket);
//        this.serverState = serverState;
//        this.commandHandler = new CommandHandler(serverState);
//    }
//
//    @Override
//    public void run() {
//        try {
//            // Expect handshake: "HELLO <username> <password>"
//            String firstLine = clientData.getInputStream().readLine();
//            if (firstLine == null || !firstLine.startsWith("HELLO ")) {
//                clientData.getOutputStream().println("Invalid handshake.");
//                clientData.getSocket().close();
//                return;
//            }
//
//            String[] parts = firstLine.split("\\s+", 3);
//            if (parts.length < 3) {
//                clientData.getOutputStream().println("Invalid handshake format.");
//                clientData.getSocket().close();
//                return;
//            }
//
//            String username = parts[1];
//            String password = parts[2];
//
//            // Authenticate using existing user store
//            if (!UserManager.authenticate(username, password)) {
//                clientData.getOutputStream().println("Authentication failed.");
//                clientData.getSocket().close();
//                return;
//            }
//
//            // Resolve user and set metadata
//            User user = UserManager.getInstance().getUserByUserName(username);
//            if (user != null) {
//                clientData.setUserType(user.getUserType());
//                clientData.setBranchNumber(user.getBranchNumber());
//                String branchName = InventoryManager.getInstance().getBranchCityByNumber(user.getBranchNumber());
//                String displayName = username + "@" + (branchName != null ? branchName : "unknown");
//                clientData.setName(displayName);
//            } else {
//                clientData.setUserType(UserType.BasicWorker);
//            }
//
//            serverState.addClient(clientData);
//            System.out.println("Client " + clientData.getClientAddress() + " connected at " + new Date());
//
//            clientData.getOutputStream().println("Welcome " + clientData.getName() + "!");
//            clientData.getOutputStream().println("Use the client menu to send commands. Only 'goodbye' is text-based.");
//
//            String line;
//            while ((line = clientData.getInputStream().readLine()) != null) {
//                commandHandler.handle(clientData, line);
//            }
//        } catch (IOException e) {
//            System.out.println("Client " + clientData.getClientAddress() + " disconnected unexpectedly.");
//        } finally {
//            System.out.println("Cleaning up connection for " + clientData.getClientAddress());
//            commandHandler.handleDisconnection(clientData);
//            serverState.removeClient(clientData);
//            try {
//                clientData.getSocket().close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}