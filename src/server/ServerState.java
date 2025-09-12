package server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class ServerState {

    private final Vector<ConnectedClient> allConnectedClients = new Vector<>();
    private final ConcurrentHashMap<ConnectedClient, Queue<ConnectedClient>> chatQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ConnectedClient, ChatSession> activeChatSessions = new ConcurrentHashMap<>();

    public void addClient(ConnectedClient client) {
        allConnectedClients.add(client);
    }

    public void removeClient(ConnectedClient client) {
        allConnectedClients.remove(client);
        chatQueues.remove(client);
        activeChatSessions.remove(client);
    }

    public Vector<ConnectedClient> getAllConnectedClients() {
        return allConnectedClients;
    }

    // Match by friendly name first; also support legacy id (ip:port)
    public ConnectedClient findClientById(String idOrName) {
        for (ConnectedClient sd : allConnectedClients) {
            if (sd.getName() != null && sd.getName().equals(idOrName)) {
                return sd;
            }
        }
        for (ConnectedClient sd : allConnectedClients) {
            if (sd.getClientAddress().equals(idOrName)) {
                return sd;
            }
        }
        return null;
    }

    public ConnectedClient findRequesterFor(ConnectedClient replier) {
        for (ConnectedClient sd : allConnectedClients) {
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

    public void addParticipantToSession(ConnectedClient participant, ChatSession session) {
        activeChatSessions.put(participant, session);
    }

    public void endSession(ChatSession session) {
        session.endSession();
        session.getParticipants().forEach(activeChatSessions::remove);
    }

    public void enqueueClient(ConnectedClient target, ConnectedClient requester) {
        chatQueues.putIfAbsent(target, new ConcurrentLinkedQueue<>());
        chatQueues.get(target).add(requester);
    }

    public ConnectedClient dequeueClient(ConnectedClient freedClient) {
        Queue<ConnectedClient> queue = chatQueues.get(freedClient);
        return (queue != null) ? queue.poll() : null;
    }

    public int getQueueSize(ConnectedClient client) {
        Queue<ConnectedClient> queue = chatQueues.get(client);
        return (queue != null) ? queue.size() : 0;
    }


    /**
     * Returns a list of all currently active chat sessions,
     * ensuring that each session appears only once.
     */
    public List<ChatSession> getAllActiveChatSessionsWithoutDuplicates() {
        Set<ChatSession> chatSessionsNoDuplicates = new HashSet<>();
        chatSessionsNoDuplicates.addAll(activeChatSessions.values());

        return new ArrayList<>(chatSessionsNoDuplicates);
    }
}