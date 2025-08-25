package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatManager {
    static Vector<SocketData> allConnections = new Vector<>();

    // map to hold queues of waiting clients
    private static final ConcurrentHashMap<SocketData, Queue<SocketData>> chatQueues = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        final ServerSocket server = new ServerSocket(7000);
        System.out.println(new Date() + " Server waiting for connections...");

        while (true) {
            Socket socket = server.accept();
            new Thread(() -> {
                SocketData currentSocketData = new SocketData(socket);
                allConnections.add(currentSocketData);
                System.out.println("Client " + currentSocketData.getClientAddress() + " connected at " + new Date());

                currentSocketData.getOutputStream().println("Welcome! Type 'list' to see available clients, or 'listall' to see all clients.");

                try {
                    String line;
                    while ((line = currentSocketData.getInputStream().readLine()) != null) {
                        handleClientInput(currentSocketData, line);
                    }
                } catch (Exception e) {
                    System.out.println("Client " + currentSocketData.getClientAddress() + " disconnected.");
                } finally {
                    allConnections.remove(currentSocketData);
                    chatQueues.remove(currentSocketData); // clean up queue if any
                    try {
                        currentSocketData.getSocket().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private static void handleClientInput(SocketData client, String line) {
        System.out.println(new Date() + " From client " + client.getClientAddress() + ": " + line);

        if (line.equalsIgnoreCase("goodbye")) {
            SocketData partner = client.getChatPartner();
            if (partner != null) {
                endChatSession(client, partner);
            } else {
                client.getOutputStream().println("You are not in a private chat.");
            }
            return;
        }

        SocketData partner = client.getChatPartner();
        boolean isChatActive = partner != null && partner.getChatPartner() == client;

        if (isChatActive) {
            sendPrivateMessage(client, partner, line);
        } else if (partner != null && !isChatActive) {
            client.getOutputStream().println("Waiting for " + partner.getClientAddress() + " to accept your chat request. Please wait.");
        } else if (line.equalsIgnoreCase("list")) {
            sendAvailableClients(client);
        } else if (line.equalsIgnoreCase("listall")) {
            sendAllClients(client);
        } else if (line.startsWith("chat ")) {
            String targetId = line.substring(5).trim();
            handleChatRequest(client, targetId);
        } else if (line.equalsIgnoreCase("yes") && hasPendingRequest(client)) {
            acceptChatRequest(client);
        } else if (line.equalsIgnoreCase("no") && hasPendingRequest(client)) {
            rejectChatRequest(client);
        } else {
            client.getOutputStream().println("Command not recognized. Use: 'list', 'listall', or 'chat <id>'.");
        }
    }

    private static void endChatSession(SocketData client1, SocketData client2) {
        client1.getOutputStream().println("You have ended the chat session. You are now available for new chats.");
        client2.getOutputStream().println("Your chat partner has ended the chat session. You are now available for new chats.");

        client1.setAvailable(true);
        client1.setChatPartner(null);
        client2.setAvailable(true);
        client2.setChatPartner(null);

        processNextInQueue(client1);
        processNextInQueue(client2);
    }

    private static void sendAvailableClients(SocketData requester) {
        requester.getOutputStream().println("Available clients:");
        for (SocketData sd : allConnections) {
            if (sd.isAvailable() && sd != requester) {
                requester.getOutputStream().println(" - " + sd.getClientAddress());
            }
        }
    }

    // NEW: show all clients including busy ones + queue info
    private static void sendAllClients(SocketData requester) {
        requester.getOutputStream().println("All connected clients:");
        for (SocketData sd : allConnections) {
            if (sd == requester) continue;

            if (sd.isAvailable()) {
                requester.getOutputStream().println(" - " + sd.getClientAddress() + " (available)");
            } else {
                Queue<SocketData> queue = chatQueues.get(sd);
                int queueSize = (queue != null) ? queue.size() : 0;
                requester.getOutputStream().println(" - " + sd.getClientAddress() + " (busy, " + queueSize + " in queue)");
            }
        }
    }

    private static void handleChatRequest(SocketData requester, String targetId) {
        SocketData target = null;
        for (SocketData sd : allConnections) {
            if (sd.getClientAddress().equals(targetId)) {
                target = sd;
                break;
            }
        }
        if (target == null) {
            requester.getOutputStream().println("Target not found.");
            return;
        }

        if (target.isAvailable()) {
            requester.setAvailable(false);
            target.setAvailable(false);
            requester.setChatPartner(target);
            target.getOutputStream().println("Client " + requester.getClientAddress() +
                    " wants to chat with you. Reply 'yes' or 'no'.");
        } else {
            chatQueues.putIfAbsent(target, new ConcurrentLinkedQueue<>());
            Queue<SocketData> queue = chatQueues.get(target);
            queue.add(requester);
            requester.getOutputStream().println("Client " + target.getClientAddress() +
                    " is busy. You have been placed in queue position " + queue.size() + ".");
        }
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
            processNextInQueue(replier);
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
            processNextInQueue(sender);
        }
    }

    private static void processNextInQueue(SocketData freedClient) {
        Queue<SocketData> queue = chatQueues.get(freedClient);
        if (queue != null) {
            SocketData nextRequester = queue.poll();
            if (nextRequester != null && allConnections.contains(nextRequester)) {
                freedClient.setAvailable(false);
                nextRequester.setAvailable(false);
                nextRequester.setChatPartner(freedClient);

                freedClient.getOutputStream().println("Client " + nextRequester.getClientAddress() +
                        " wants to chat with you. Reply 'yes' or 'no'.");
                nextRequester.getOutputStream().println("Your chat request has reached the front of the queue. Waiting for " +
                        freedClient.getClientAddress() + " to respond.");
            }
        }
    }
}
