package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatManager {

    static Vector<SocketData> allConnections = new Vector<>();

    // map to hold queues of waiting clients
    private static final ConcurrentHashMap<SocketData, Queue<SocketData>> chatQueues = new ConcurrentHashMap<>();

    // Map to track active chat sessions. Key is a participant, value is the session object.
    private static final ConcurrentHashMap<SocketData, ChatSession> activeChatSessions = new ConcurrentHashMap<>();

    // Paths for logging
    private static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir"));
    private static final Path LOGS_DIR = PROJECT_ROOT.resolve("logs");
    private static final Path LOG_FILE = LOGS_DIR.resolve("chat_log.txt");
    private static final Object LOG_LOCK = new Object(); // guard concurrent file writes

    static {
        try {
            if (!Files.exists(LOGS_DIR)) {
                Files.createDirectories(LOGS_DIR);
                System.out.println("Created logs directory: " + LOGS_DIR.toAbsolutePath());
            }
            if (!Files.exists(LOG_FILE)) {
                Files.createFile(LOG_FILE);
                System.out.println("Created log file: " + LOG_FILE.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
        }
    }

    // --- MODIFIED ChatSession CLASS ---
    public static class ChatSession {
        // These two are final and represent the original chat parties for logging
        final SocketData client1;
        final SocketData client2;
        // This vector holds all current participants, including any joined managers
        private final Vector<SocketData> participants = new Vector<>();
        final Date startTime;
        Date endTime;
        final StringBuilder chatContent;
        volatile boolean saveChatLog;

        public ChatSession(SocketData client1, SocketData client2) {
            this.client1 = client1;
            this.client2 = client2;
            this.participants.add(client1);
            this.participants.add(client2);
            this.startTime = new Date();
            this.chatContent = new StringBuilder();
            this.saveChatLog = false;
        }

        public void addParticipant(SocketData member) {
            if (!participants.contains(member)) {
                participants.add(member);
            }
        }

        // --- NEW METHOD ---
        public void removeParticipant(SocketData member) {
            participants.remove(member);
        }

        public Vector<SocketData> getParticipants() {
            return participants;
        }

        public void appendMessage(SocketData sender, String message) {
            String logEntry = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new Date()) + " " + sender.getClientAddress() + ": " + message + "\n";
            this.chatContent.append(logEntry);
        }

        public void endSession() {
            this.endTime = new Date();
        }

        public SocketData getClient1() {
            return client1;
        }

        public SocketData getClient2() {
            return client2;
        }
    }

    public static void main(String[] args) throws IOException {
        final ServerSocket server = new ServerSocket(7000);
        System.out.println(new Date() + " Server waiting for connections...");

        while (true) {
            Socket socket = server.accept();
            new Thread(() -> {
                SocketData currentSocketData = new SocketData(socket);
                allConnections.add(currentSocketData);

                System.out.println("Client " + currentSocketData.getClientAddress() + " connected at " + new Date());
                currentSocketData.getOutputStream().println("Welcome! Use 'list', 'listall', 'chat <id>', or 'join <id>'.");

                try {
                    String line;
                    while ((line = currentSocketData.getInputStream().readLine()) != null) {
                        handleClientInput(currentSocketData, line);
                    }
                } catch (Exception e) {
                    System.out.println("Client " + currentSocketData.getClientAddress() + " disconnected.");
                    handleDisconnection(currentSocketData);
                } finally {
                    allConnections.remove(currentSocketData);
                    chatQueues.remove(currentSocketData);
                    try {
                        currentSocketData.getSocket().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private static void handleDisconnection(SocketData client) {
        ChatSession session = client.getCurrentSession();
        if (session != null) {
            // A participant disconnected, treat it as them leaving the chat.
            if (session.getParticipants().size() <= 2) {
                // If it was a 2-person chat, the session ends for the other person too.
                endChatSession(session, true);
            } else {
                // If it was a multi-person chat, the session continues.
                leaveChatSession(client, session);
            }
        }
    }

    // --- MODIFIED handleClientInput ---
    private static void handleClientInput(SocketData client, String line) {
        System.out.println(new Date() + " From client " + client.getClientAddress() + ": " + line);
        ChatSession session = client.getCurrentSession();

        if (session != null) { // Client is in an active chat
            if (line.equalsIgnoreCase("goodbye")) {
                // If 2 or fewer people are in the chat, "goodbye" ends the session for everyone.
                if (session.getParticipants().size() <= 2) {
                    endChatSession(session, true);
                } else {
                    // If more than 2 people are in the chat, the user just leaves.
                    leaveChatSession(client, session);
                }
            } else if (line.equalsIgnoreCase("savechat")) {
                session.saveChatLog = true;
                client.getOutputStream().println("This chat will be saved upon completion.");
            } else {
                sendPrivateMessage(client, line);
            }
        } else { // Client is not in a chat
            if (line.equalsIgnoreCase("list")) {
                sendAvailableClients(client);
            } else if (line.equalsIgnoreCase("listall")) {
                sendAllClients(client);
            } else if (line.startsWith("chat ")) {
                String targetId = line.substring(5).trim();
                handleChatRequest(client, targetId);
            } else if (line.startsWith("join ")) {
                if (true) { // User's requirement: if(true) for testing
                    String targetId = line.substring(5).trim();
                    handleJoinRequest(client, targetId);
                }
            } else if (line.equalsIgnoreCase("yes") && findRequesterFor(client) != null) {
                acceptChatRequest(client);
            } else if (line.equalsIgnoreCase("no") && findRequesterFor(client) != null) {
                rejectChatRequest(client);
            } else {
                client.getOutputStream().println("Command not recognized. Use: 'list', 'listall', 'chat <id>', 'join <id>'.");
            }
        }
    }

    // --- NEW leaveChatSession METHOD ---
    /**
     * Handles a single client leaving a multi-person chat session.
     * The session continues for the remaining participants.
     * @param leaver The client that is leaving.
     * @param session The session they are leaving from.
     */
    private static void leaveChatSession(SocketData leaver, ChatSession session) {
        // Remove the client from the session and clean up their state
        session.removeParticipant(leaver);
        leaver.setAvailable(true);
        leaver.setCurrentSession(null);
        activeChatSessions.remove(leaver);
        leaver.getOutputStream().println("You have left the chat. You are now available.");

        // Notify the remaining participants
        String leaveMessage = "Participant " + leaver.getClientAddress() + " has left the chat.";
        for (SocketData member : session.getParticipants()) {
            member.getOutputStream().println(leaveMessage);
        }

        processNextInQueue(leaver);
    }

    private static void endChatSession(ChatSession session, boolean notify) {
        session.endSession();
        saveSessionToLog(session);

        // Make a copy to avoid ConcurrentModificationException if a member disconnects during iteration
        Vector<SocketData> membersToEnd = new Vector<>(session.getParticipants());

        for (SocketData member : membersToEnd) {
            if (notify && member.getSocket().isConnected()) {
                member.getOutputStream().println("The chat session has ended. You are now available for new chats.");
            }
            member.setAvailable(true);
            member.setCurrentSession(null);
            activeChatSessions.remove(member);
            processNextInQueue(member);
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

    private static void sendAllClients(SocketData requester) {
        requester.getOutputStream().println("All connected clients:");
        for (SocketData sd : allConnections) {
            if (sd == requester) continue;
            if (sd.isAvailable()) {
                requester.getOutputStream().println(" - " + sd.getClientAddress() + " (available)");
            } else {
                Queue<SocketData> queue = chatQueues.get(sd);
                int queueSize = (queue != null) ? queue.size() : 0;
                String status = "busy";
                if (sd.getCurrentSession() != null) {
                    status = "in a chat";
                } else if (sd.getPendingRequestTo() != null) {
                    status = "pending chat with " + sd.getPendingRequestTo().getClientAddress();
                }
                requester.getOutputStream().println(" - " + sd.getClientAddress() + " (" + status + ", " + queueSize + " in queue)");
            }
        }
    }

    private static void handleChatRequest(SocketData requester, String targetId) {
        SocketData target = findClientById(targetId);
        if (target == null) {
            requester.getOutputStream().println("Target not found.");
            return;
        }
        if (target == requester) {
            requester.getOutputStream().println("You cannot chat with yourself.");
            return;
        }

        if (target.isAvailable()) {
            requester.setAvailable(false);
            target.setAvailable(false);
            requester.setPendingRequestTo(target);
            target.getOutputStream().println("Client " + requester.getClientAddress() + " wants to chat with you. Reply 'yes' or 'no'.");
        } else {
            chatQueues.putIfAbsent(target, new ConcurrentLinkedQueue<>());
            Queue<SocketData> queue = chatQueues.get(target);
            queue.add(requester);
            requester.getOutputStream().println("Client " + target.getClientAddress() + " is busy. You have been placed in queue position " + queue.size() + ".");
        }
    }

    private static void handleJoinRequest(SocketData manager, String targetId) {
        SocketData target = findClientById(targetId);
        if (target == null) {
            manager.getOutputStream().println("Target client not found.");
            return;
        }
        ChatSession session = target.getCurrentSession();
        if (session == null) {
            manager.getOutputStream().println("Target client is not in an active chat.");
            return;
        }
        if (session.getParticipants().contains(manager)) {
            manager.getOutputStream().println("You are already in this chat.");
            return;
        }

        session.addParticipant(manager);
        manager.setCurrentSession(session);
        manager.setAvailable(false);
        activeChatSessions.put(manager, session);

        String joinMsg = "A manager (" + manager.getClientAddress() + ") has joined the chat.";
        manager.getOutputStream().println("You have joined the chat.");

        for (SocketData member : session.getParticipants()) {
            if (member != manager) {
                member.getOutputStream().println(joinMsg);
            }
        }
    }

    private static void acceptChatRequest(SocketData replier) {
        SocketData requester = findRequesterFor(replier);
        if (requester != null) {
            requester.setPendingRequestTo(null);

            ChatSession session = new ChatSession(requester, replier);
            requester.setCurrentSession(session);
            replier.setCurrentSession(session);
            activeChatSessions.put(requester, session);
            activeChatSessions.put(replier, session);

            String chatStartedMsg = "You are now in a private chat. Use 'goodbye' to exit, 'savechat' to save the log.";
            requester.getOutputStream().println("Chat request accepted by " + replier.getClientAddress() + ". " + chatStartedMsg);
            replier.getOutputStream().println("You accepted the chat request. " + chatStartedMsg);
        } else {
            replier.getOutputStream().println("You don't have a pending chat request.");
        }
    }

    private static void rejectChatRequest(SocketData replier) {
        SocketData requester = findRequesterFor(replier);
        if (requester != null) {
            requester.getOutputStream().println("Chat request rejected by " + replier.getClientAddress());
            requester.setAvailable(true);
            requester.setPendingRequestTo(null);

            replier.setAvailable(true);
            replier.getOutputStream().println("You rejected the chat request.");

            processNextInQueue(replier);
        } else {
            replier.getOutputStream().println("You don't have a pending chat request.");
        }
    }

    private static SocketData findRequesterFor(SocketData replier) {
        for (SocketData sd : allConnections) {
            if (sd.getPendingRequestTo() == replier) {
                return sd;
            }
        }
        return null;
    }

    private static SocketData findClientById(String id) {
        for (SocketData sd : allConnections) {
            if (sd.getClientAddress().equals(id)) {
                return sd;
            }
        }
        return null;
    }

    private static void sendPrivateMessage(SocketData sender, String message) {
        ChatSession session = sender.getCurrentSession();
        if (session == null) return;

        session.appendMessage(sender, message);

        for (SocketData receiver : session.getParticipants()) {
            if (receiver != sender && receiver.getSocket().isConnected()) {
                receiver.getOutputStream().println(sender.getClientAddress() + ": " + message);
            }
        }
    }

    private static void processNextInQueue(SocketData freedClient) {
        Queue<SocketData> queue = chatQueues.get(freedClient);
        if (queue != null) {
            SocketData nextRequester = queue.poll();
            if (nextRequester != null && allConnections.contains(nextRequester)) {
                freedClient.setAvailable(false);
                nextRequester.setAvailable(false);
                nextRequester.setPendingRequestTo(freedClient);

                freedClient.getOutputStream().println("Client " + nextRequester.getClientAddress() + " from your queue wants to chat. Reply 'yes' or 'no'.");
                nextRequester.getOutputStream().println("Your chat request is now active. Waiting for " + freedClient.getClientAddress() + " to respond.");
            }
        }
    }

    private static void saveSessionToLog(ChatSession session) {
        if (session.endTime == null) session.endSession();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String c1 = sanitizeAddress(session.getClient1().getClientAddress());
        String c2 = sanitizeAddress(session.getClient2().getClientAddress());

        StringBuilder entry = new StringBuilder();
        entry.append("=== Chat Session ===\n");
        entry.append("Participants: ").append(c1).append(" <-> ").append(c2).append("\n");
        entry.append("Start: ").append(df.format(session.startTime)).append("\n");
        entry.append("End:   ").append(df.format(session.endTime)).append("\n");
        entry.append("SavedContent: ").append(session.saveChatLog ? "yes" : "no").append("\n");
        if (session.saveChatLog) {
            entry.append("--- Transcript Start ---\n");
            entry.append(session.chatContent);
            entry.append("--- Transcript End ---\n");
        }
        entry.append("\n");

        synchronized (LOG_LOCK) {
            try (BufferedWriter writer = Files.newBufferedWriter(LOG_FILE, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                writer.write(entry.toString());
            } catch (IOException e) {
                System.err.println("Error writing chat log: " + e.getMessage());
            }
        }
    }

    private static String sanitizeAddress(String addr) {
        if (addr == null) return "unknown";
        String s = addr.startsWith("/") ? addr.substring(1) : addr;
        return s.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}