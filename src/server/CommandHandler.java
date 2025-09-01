package server;

import java.util.Vector;

/**
 * Processes commands received from a client.
 * This class is stateless and operates on the shared ServerState.
 */
public class CommandHandler {
    private final ServerState serverState;

    public CommandHandler(ServerState serverState) {
        this.serverState = serverState;
    }

    public void handle(SocketData client, String line) {
        System.out.println("From client " + client.getClientAddress() + ": " + line);
        ChatSession session = client.getCurrentSession();

        if (session != null) {
            handleInChatCommands(client, session, line);
        } else {
            handleOutOfChatCommands(client, line);
        }
    }

    private void handleInChatCommands(SocketData client, ChatSession session, String line) {
        if (line.equalsIgnoreCase("goodbye")) {
            if (session.getParticipants().size() <= 2) {
                endChatSession(session, true);
            } else {
                leaveChatSession(client, session);
            }
        } else if (line.equalsIgnoreCase("savechat")) {
            session.saveChatLog = true;
            client.getOutputStream().println("This chat will be saved upon completion.");
        } else {
            sendPrivateMessage(client, line);
        }
    }

    public void handleOutOfChatCommands(SocketData client, String line) {
        if (line.equalsIgnoreCase("list")) {
            sendAvailableClients(client);
        } else if (line.equalsIgnoreCase("listall")) {
            sendAllClients(client);
        } else if (line.startsWith("chat ")) {
            handleChatRequest(client, line.substring(5).trim());
        } else if (line.startsWith("join ")) {
            if (true) { // For testing, anyone can join
                handleJoinRequest(client, line.substring(5).trim());
            }
        } else if (line.equalsIgnoreCase("yes") && serverState.findRequesterFor(client) != null) {
            acceptChatRequest(client);
        } else if (line.equalsIgnoreCase("no") && serverState.findRequesterFor(client) != null) {
            rejectChatRequest(client);
        } else {
            client.getOutputStream().println("Command not recognized. Use: 'list', 'listall', 'chat <id>', 'join <id>'.");
        }
    }

    public void handleDisconnection(SocketData client) {
        ChatSession session = client.getCurrentSession();
        if (session != null) {
            if (session.getParticipants().size() <= 2) {
                endChatSession(session, true);
            } else {
                leaveChatSession(client, session);
            }
        }
    }

    private void leaveChatSession(SocketData leaver, ChatSession session) {
        session.removeParticipant(leaver);
        leaver.setAvailable(true);
        leaver.setCurrentSession(null);
        serverState.removeClient(leaver); // This also removes from active sessions map
        serverState.addClient(leaver); // Re-add to connections list but without session
        leaver.getOutputStream().println("You have left the chat. You are now available.");

        String leaveMessage = "Participant " + leaver.getClientAddress() + " has left the chat.";
        session.getParticipants().forEach(member -> member.getOutputStream().println(leaveMessage));

        processNextInQueue(leaver);
    }

    private void endChatSession(ChatSession session, boolean notify) {
        ChatLogger.saveSessionToLog(session);
        Vector<SocketData> membersToEnd = new Vector<>(session.getParticipants());

        serverState.endSession(session);

        for (SocketData member : membersToEnd) {
            if (notify && member.getSocket().isConnected()) {
                member.getOutputStream().println("The chat session has ended. You are now available.");
            }
            member.setAvailable(true);
            member.setCurrentSession(null);
            processNextInQueue(member);
        }
    }

    private void handleChatRequest(SocketData requester, String targetId) {
        SocketData target = serverState.findClientById(targetId);
        if (target == null || target == requester) {
            requester.getOutputStream().println(target == null ? "Target not found." : "You cannot chat with yourself.");
            return;
        }

        if (target.isAvailable()) {
            requester.setAvailable(false);
            target.setAvailable(false);
            requester.setPendingRequestTo(target);
            target.getOutputStream().println("Client " + requester.getClientAddress() + " wants to chat with you. Reply 'yes' or 'no'.");
        } else {
            serverState.enqueueClient(target, requester);
            requester.getOutputStream().println("Client " + target.getClientAddress() + " is busy. You have been placed in queue position " + serverState.getQueueSize(target) + ".");
        }
    }

    private void handleJoinRequest(SocketData manager, String targetId) {
        SocketData target = serverState.findClientById(targetId);
        ChatSession session = (target != null) ? target.getCurrentSession() : null;

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
        serverState.addParticipantToSession(manager, session);

        String joinMsg = "A manager (" + manager.getClientAddress() + ") has joined the chat.";
        manager.getOutputStream().println("You have joined the chat.");
        session.getParticipants().stream()
                .filter(member -> member != manager)
                .forEach(member -> member.getOutputStream().println(joinMsg));
    }

    private void acceptChatRequest(SocketData replier) {
        SocketData requester = serverState.findRequesterFor(replier);
        if (requester != null) {
            requester.setPendingRequestTo(null);

            ChatSession session = new ChatSession(requester, replier);
            requester.setCurrentSession(session);
            replier.setCurrentSession(session);
            serverState.startSession(session);

            String chatStartedMsg = "You are now in a private chat. Use 'goodbye' or 'savechat'.";
            requester.getOutputStream().println("Chat request accepted. " + chatStartedMsg);
            replier.getOutputStream().println("You accepted the request. " + chatStartedMsg);
        }
    }

    private void rejectChatRequest(SocketData replier) {
        SocketData requester = serverState.findRequesterFor(replier);
        if (requester != null) {
            requester.getOutputStream().println("Chat request rejected by " + replier.getClientAddress());
            requester.setAvailable(true);
            requester.setPendingRequestTo(null);

            replier.setAvailable(true);
            replier.getOutputStream().println("You rejected the chat request.");
            processNextInQueue(replier);
        }
    }

    private void processNextInQueue(SocketData freedClient) {
        SocketData nextRequester = serverState.dequeueClient(freedClient);
        if (nextRequester != null && serverState.getAllConnections().contains(nextRequester)) {
            freedClient.setAvailable(false);
            nextRequester.setAvailable(false);
            nextRequester.setPendingRequestTo(freedClient);

            freedClient.getOutputStream().println("Client " + nextRequester.getClientAddress() + " from your queue wants to chat. Reply 'yes' or 'no'.");
            nextRequester.getOutputStream().println("Your request is now active. Waiting for " + freedClient.getClientAddress() + " to respond.");
        }
    }

    private void sendPrivateMessage(SocketData sender, String message) {
        ChatSession session = sender.getCurrentSession();
        if (session == null) return;
        session.appendMessage(sender, message);
        session.getParticipants().stream()
                .filter(receiver -> receiver != sender && receiver.getSocket().isConnected())
                .forEach(receiver -> receiver.getOutputStream().println(sender.getClientAddress() + ": " + message));
    }

    public void sendAvailableClients(SocketData requester) {
        requester.getOutputStream().println("Available clients:");
        serverState.getAllConnections().stream()
                .filter(sd -> sd.isAvailable() && sd != requester)
                .forEach(sd -> requester.getOutputStream().println(" - " + sd.getClientAddress()));
    }

    private void sendAllClients(SocketData requester) {
        requester.getOutputStream().println("All connected clients:");
        for (SocketData sd : serverState.getAllConnections()) {
            if (sd == requester) continue;
            if (sd.isAvailable()) {
                requester.getOutputStream().println(" - " + sd.getClientAddress() + " (available)");
            } else {
                int queueSize = serverState.getQueueSize(sd);
                String status = "in a chat";
                if (sd.getPendingRequestTo() != null) {
                    status = "pending chat with " + sd.getPendingRequestTo().getClientAddress();
                }
                requester.getOutputStream().println(" - " + sd.getClientAddress() + " (" + status + ", " + queueSize + " in queue)");
            }
        }
    }
}