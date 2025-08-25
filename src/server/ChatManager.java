package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Vector;

public class ChatManager {
    static Vector<SocketData> allConnections = new Vector<>();

    public static void main(String[] args) throws IOException {
        final ServerSocket server = new ServerSocket(7000);
        System.out.println(new Date() + " Server waiting for connections...");

        while (true) {
            Socket socket = server.accept();
            new Thread(() -> {
                SocketData currentSocketData = new SocketData(socket);
                allConnections.add(currentSocketData);
                System.out.println("Client " + currentSocketData.getClientAddress() + " connected at " + new Date());

                currentSocketData.getOutputStream().println("Welcome! Type 'list' to see available clients.");

                try {
                    String line;
                    while ((line = currentSocketData.getInputStream().readLine()) != null
                            && !line.equals("goodbye")) {
                        handleClientInput(currentSocketData, line);
                    }
                } catch (Exception e) {
                    System.out.println("Client " + currentSocketData.getClientAddress() + " disconnected.");
                } finally {
                    allConnections.remove(currentSocketData);
                    try {
                        currentSocketData.getSocket().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * MODIFIED METHOD
     * This method now checks for a reciprocal chat partnership before forwarding messages.
     */
    private static void handleClientInput(SocketData client, String line) {
        System.out.println(new Date() + " From client " + client.getClientAddress() + ": " + line);

        SocketData partner = client.getChatPartner();
        // A chat is only active if the partnership is reciprocal.
        boolean isChatActive = partner != null && partner.getChatPartner() == client;

        if (isChatActive) {
            // If in a fully established chat, forward the message to the partner.
            sendPrivateMessage(client, partner, line);
        } else if (partner != null && !isChatActive) {
            // If the client has a partner but the chat is not reciprocal, it means the request is pending.
            client.getOutputStream().println("Waiting for " + partner.getClientAddress() + " to accept your chat request. Please wait.");
        } else if (line.equalsIgnoreCase("list")) {
            sendAvailableClients(client);
        } else if (line.startsWith("chat ")) {
            String targetId = line.substring(5).trim();
            handleChatRequest(client, targetId);
        } else if (line.equalsIgnoreCase("yes") && hasPendingRequest(client)) {
            acceptChatRequest(client);
        } else if (line.equalsIgnoreCase("no") && hasPendingRequest(client)) {
            rejectChatRequest(client);
        } else {
            // Fallback for unrecognized commands
            client.getOutputStream().println("Command not recognized. Type 'list' or 'chat <id>'.");
        }
    }

    private static void sendAvailableClients(SocketData requester) {
        requester.getOutputStream().println("Available clients:");
        for (SocketData sd : allConnections) {
            if (sd.isAvailable() && sd != requester) {
                requester.getOutputStream().println(" - " + sd.getClientAddress());
            }
        }
    }

    private static void handleChatRequest(SocketData requester, String targetId) {
        SocketData target = null;
        for (SocketData sd : allConnections) {
            if (sd.getClientAddress().equals(targetId) && sd.isAvailable()) {
                target = sd;
                break;
            }
        }
        if (target == null) {
            requester.getOutputStream().println("Target not found or unavailable.");
            return;
        }

        // Set the state for the requester and target
        requester.setAvailable(false);
        target.setAvailable(false);
        requester.setChatPartner(target);
        // We will set the target's partner to the requester only upon acceptance

        target.getOutputStream().println("Client " + requester.getClientAddress() +
                " wants to chat with you. Reply 'yes' or 'no'.");
    }

    private static boolean hasPendingRequest(SocketData client) {
        for (SocketData sd : allConnections) {
            if (sd.getChatPartner() == client) {
                return true;
            }
        }
        return false;
    }

    private static void acceptChatRequest(SocketData replier) {
        SocketData requester = findRequesterFor(replier);
        if (requester != null) {
            replier.setChatPartner(requester);
            requester.getOutputStream().println("Chat request accepted. You are now in a private chat with " + replier.getClientAddress());
            replier.getOutputStream().println("You are now in a private chat with " + requester.getClientAddress());
        } else {
            replier.getOutputStream().println("You don't have a pending chat request.");
        }
    }

    private static void rejectChatRequest(SocketData replier) {
        SocketData requester = findRequesterFor(replier);
        if (requester != null) {
            requester.getOutputStream().println("Chat request rejected by " + replier.getClientAddress());
            requester.setAvailable(true);
            requester.setChatPartner(null);
            replier.setAvailable(true);
            replier.setChatPartner(null);
            replier.getOutputStream().println("You rejected the chat request.");
        } else {
            replier.getOutputStream().println("You don't have a pending chat request.");
        }
    }

    private static SocketData findRequesterFor(SocketData replier) {
        for (SocketData sd : allConnections) {
            if (sd.getChatPartner() == replier) {
                return sd;
            }
        }
        return null;
    }

    private static void sendPrivateMessage(SocketData sender, SocketData receiver, String message) {
        if (receiver != null && receiver.getSocket().isConnected()) {
            receiver.getOutputStream().println("Message from: " + sender.getClientAddress() + "@ " + message);
        } else {
            sender.getOutputStream().println("Your chat partner has disconnected. The chat session is ended.");
            sender.setAvailable(true);
            sender.setChatPartner(null);
        }
    }
}