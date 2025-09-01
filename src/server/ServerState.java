package server;

import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A thread-safe container for the server's shared state.
 * Manages all connected clients, active chat sessions, and waiting queues.
 */
public class ServerState {
    private final Vector<SocketData> allConnections = new Vector<>();
    private final ConcurrentHashMap<SocketData, Queue<SocketData>> chatQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<SocketData, ChatSession> activeChatSessions = new ConcurrentHashMap<>();

    // Client Management
    public void addClient(SocketData client) {
        allConnections.add(client);
    }

    public void removeClient(SocketData client) {
        allConnections.remove(client);
        chatQueues.remove(client);
        activeChatSessions.remove(client);
    }

    public Vector<SocketData> getAllConnections() {
        return allConnections;
    }

    public SocketData findClientById(String id) {
        for (SocketData sd : allConnections) {
            if (sd.getClientAddress().equals(id)) {
                return sd;
            }
        }
        return null;
    }

    public SocketData findRequesterFor(SocketData replier) {
        for (SocketData sd : allConnections) {
            if (sd.getPendingRequestTo() == replier) {
                return sd;
            }
        }
        return null;
    }

    // Chat Session Management
    public void startSession(ChatSession session) {
        activeChatSessions.put(session.getClient1(), session);
        activeChatSessions.put(session.getClient2(), session);
    }

    public void addParticipantToSession(SocketData participant, ChatSession session) {
        activeChatSessions.put(participant, session);
    }

    public void endSession(ChatSession session) {
        session.getParticipants().forEach(activeChatSessions::remove);
    }

    // Queue Management
    public void enqueueClient(SocketData target, SocketData requester) {
        chatQueues.putIfAbsent(target, new ConcurrentLinkedQueue<>());
        chatQueues.get(target).add(requester);
    }

    public SocketData dequeueClient(SocketData freedClient) {
        Queue<SocketData> queue = chatQueues.get(freedClient);
        return (queue != null) ? queue.poll() : null;
    }

    public int getQueueSize(SocketData client) {
        Queue<SocketData> queue = chatQueues.get(client);
        return (queue != null) ? queue.size() : 0;
    }
}