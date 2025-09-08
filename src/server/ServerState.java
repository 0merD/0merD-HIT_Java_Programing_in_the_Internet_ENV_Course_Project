package server;

import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerState {
    private final Vector<SocketData> allConnections = new Vector<>();
    private final ConcurrentHashMap<SocketData, Queue<SocketData>> chatQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<SocketData, ChatSession> activeChatSessions = new ConcurrentHashMap<>();

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

    // Match by friendly name first; also support legacy id (ip:port)
    public SocketData findClientById(String idOrName) {
        for (SocketData sd : allConnections) {
            if (sd.getName() != null && sd.getName().equals(idOrName)) {
                return sd;
            }
        }
        for (SocketData sd : allConnections) {
            if (sd.getClientAddress().equals(idOrName)) {
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

    public void startSession(ChatSession session) {
        activeChatSessions.put(session.getClient1(), session);
        activeChatSessions.put(session.getClient2(), session);
    }

    public void addParticipantToSession(SocketData participant, ChatSession session) {
        activeChatSessions.put(participant, session);
    }

    public void endSession(ChatSession session) {
        session.endSession();
        session.getParticipants().forEach(activeChatSessions::remove);
    }

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