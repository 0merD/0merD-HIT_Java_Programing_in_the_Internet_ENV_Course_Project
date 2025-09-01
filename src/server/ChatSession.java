package server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Represents an active chat session between two or more clients.
 * Manages participants, chat history, and logging metadata.
 */
public class ChatSession {
    // These two are final and represent the original chat parties for logging purposes.
    final SocketData client1;
    final SocketData client2;

    // This vector holds all current participants, including any joined managers.
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