package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.BufferedWriter;

public class ChatManager {

    static Vector<SocketData> allConnections = new Vector<>();

    // map to hold queues of waiting clients
    private static final ConcurrentHashMap<SocketData, Queue<SocketData>> chatQueues = new ConcurrentHashMap<>();

    // Map to track active chat sessions and their logs
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

    // Private static class to manage chat session details
    private static class ChatSession {
        final SocketData client1;
        final SocketData client2;
        final Date startTime;
        Date endTime;
        final StringBuilder chatContent;
        volatile boolean saveChatLog;

        public ChatSession(SocketData client1, SocketData client2) {
            this.client1 = client1;
            this.client2 = client2;
            this.startTime = new Date();
            this.chatContent = new StringBuilder();
            this.saveChatLog = false;
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

        public Date getStartTime() {
            return startTime;
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
                currentSocketData.getOutputStream().println("Welcome! Type 'list' to see available clients, or 'listall' to see all clients.");

                try {
                    String line;
                    while ((line = currentSocketData.getInputStream().readLine()) != null) {
                        handleClientInput(currentSocketData, line);
                    }
                } catch (Exception e) {
                    System.out.println("Client " + currentSocketData.getClientAddress() + " disconnected.");
                    // Handle disconnections gracefully and save logs if flagged
                    SocketData partner = currentSocketData.getChatPartner();
                    if (partner != null) {
                        endChatSession(currentSocketData, partner, true);
                    }
                } finally {
                    allConnections.remove(currentSocketData);
                    chatQueues.remove(currentSocketData); // clean up queue if any

                    // Ensure chat log is saved if client disconnects
                    ChatSession session = activeChatSessions.remove(currentSocketData);
                    if (session != null) {
                        session.endSession();
                        // Always write metadata; include content only if saveChatLog was used
                        saveSessionToLog(session);
                        // Remove the mirrored entry if present
                        activeChatSessions.values().removeIf(s -> s == session);
                    }

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
                endChatSession(client, partner, false);
            } else {
                client.getOutputStream().println("You are not in a private chat.");
            }
            return;
        }

        SocketData partner = client.getChatPartner();
        boolean isChatActive = partner != null && partner.getChatPartner() == client;

        if (isChatActive) {
            // Handle the save chat command
            if (line.equalsIgnoreCase("savechat")) {
                ChatSession session = activeChatSessions.get(client);
                if (session != null) {
                    session.saveChatLog = true;
                    client.getOutputStream().println("This chat will be saved upon completion or disconnection.");
                    return;
                }
            }
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
            client.getOutputStream().println("Command not recognized. Use: 'list', 'listall', 'chat <id>', 'goodbye', or 'savechat' (in a private chat).");
        }
    }

    // End session method now takes a flag for disconnection
    private static void endChatSession(SocketData client1, SocketData client2, boolean isDisconnection) {
        client1.getOutputStream().println("You have ended the chat session. You are now available for new chats.");
        if (!isDisconnection) {
            client2.getOutputStream().println("Your chat partner has ended the chat session. You are now available for new chats.");
        }

        // End and save the chat metadata; include content if requested
        ChatSession session1 = activeChatSessions.remove(client1);
        if (session1 != null) {
            session1.endSession();
            saveSessionToLog(session1);
            // Remove the mirrored mapping if necessary
            activeChatSessions.values().removeIf(s -> s == session1);
        }
        activeChatSessions.remove(client2);

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

    // show all clients including busy ones + queue info
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

            // Create and store a new chat session
            ChatSession session = new ChatSession(requester, replier);
            activeChatSessions.put(requester, session);
            activeChatSessions.put(replier, session);
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
        // Log the message
        ChatSession session = activeChatSessions.get(sender);
        if (session != null) {
            session.appendMessage(sender, message);
        }

        if (receiver != null && receiver.getSocket().isConnected()) {
            receiver.getOutputStream().println("Message from: " + sender.getClientAddress() + "@ " + message);
        } else {
            sender.getOutputStream().println("Your chat partner has disconnected. The chat session is ended.");

            // end session gracefully on partner disconnection
            if (session != null) {
                session.endSession();
                saveSessionToLog(session); // always write metadata; content if flagged
                activeChatSessions.remove(sender);
                activeChatSessions.values().removeIf(s -> s == session);
            }

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

    /**
     * Append a session entry to the single log file.
     * Always writes metadata (participants, start, end).
     * If session.saveChatLog == true, includes the transcript contents too.
     */
    private static void saveSessionToLog(ChatSession session) {
        if (session.endTime == null) {
            session.endSession();
        }

        String timeFmt = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat df = new SimpleDateFormat(timeFmt);

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

        // Thread-safe append
        synchronized (LOG_LOCK) {
            try (BufferedWriter writer = Files.newBufferedWriter(
                    LOG_FILE, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                writer.write(entry.toString());
            } catch (IOException e) {
                System.err.println("Error writing chat log: " + e.getMessage());
            }
        }
    }

    // Replace leading "/" and colons and any unsafe characters to underscores for safety
    private static String sanitizeAddress(String addr) {
        if (addr == null) return "unknown";
        String s = addr;
        // remove leading slash often present on getInetAddress().toString()
        if (s.startsWith("/")) s = s.substring(1);
        // replace characters that are problematic in logs or filenames
        s = s.replace(':', '_');
        // collapse any other unsafe characters
        s = s.replaceAll("[^A-Za-z0-9._-]", "_");
        return s;
    }
}