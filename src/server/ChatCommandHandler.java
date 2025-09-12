package server;

import server.enums.UserType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Vector;

public class ChatCommandHandler {
    private final ServerState serverState;

    public ChatCommandHandler(ServerState serverState) {
        this.serverState = serverState;
    }

    public void handle(ConnectedClient client, String line) {
        System.out.println("From client " + client.getClientAddress() + ": " + line);
        ChatSession session = client.getCurrentSession();

        if (session != null) {
            handleInChatCommands(client, session, line);
        } else {
            handleOutOfChatCommands(client, line);
        }
    }

    private void handleInChatCommands(ConnectedClient client, ChatSession session, String line) {
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

    private void handleOutOfChatCommands(ConnectedClient client, String line) {
        if (line.equalsIgnoreCase("list")) {
            sendAvailableClients(client);
        } else if (line.equalsIgnoreCase("listall")) {
            sendAllClients(client);
        } else if (line.startsWith("chat ")) {
            handleChatRequest(client, line.substring(5).trim());
        } else if (line.startsWith("join ")) {
            if (client.getUserType() == UserType.ShiftManager) {
                handleJoinRequest(client, line.substring(5).trim());
            } else {
                client.getOutputStream().println("Only ShiftManager can join an active chat.");
            }
        } else if (line.equalsIgnoreCase("yes") && serverState.findRequesterFor(client) != null) {
            acceptChatRequest(client);
        } else if (line.equalsIgnoreCase("no") && serverState.findRequesterFor(client) != null) {
            rejectChatRequest(client);
        } else {
            client.getOutputStream().println("Command not recognized.");
        }
    }

    public void handleDisconnection(ConnectedClient client) {
        ChatSession session = client.getCurrentSession();
        if (session != null) {
            if (session.getParticipants().size() <= 2) {
                endChatSession(session, true);
            } else {
                leaveChatSession(client, session);
            }
        }
    }

//    private void leaveChatSession(SocketData leaver, ChatSession session) {
//        session.removeParticipant(leaver);
//        leaver.setAvailable(true);
//        leaver.setCurrentSession(null);
//        leaver.setPendingRequestTo(null);
//        String leaveMessage = "Participant " + leaver.getName() + " has left the chat.";
//        session.getParticipants().forEach(member -> member.getOutputStream().println(leaveMessage));
//
//        // No need to remove/add from connections; user stays connected and available
//        processNextInQueue(leaver);
//    }

    // --- Modification --- for leaving chat session - disconnect if only one participant remaining.
    private void leaveChatSession(ConnectedClient leaver, ChatSession session) {
        session.removeParticipant(leaver);
        leaver.setAvailable(true);
        leaver.setCurrentSession(null);
        leaver.setPendingRequestTo(null);

        String leaveMessage = "Participant " + leaver.getName() + " has left the chat.";
        session.getParticipants().forEach(member -> member.getOutputStream().println(leaveMessage));

        // Check if only one participant remains
        if (session.getParticipants().size() == 1) {
            ConnectedClient remaining = session.getParticipants().get(0);
            remaining.getOutputStream().println("The other participant has disconnected. Chat session ending.");
            endChatSession(session, false); // false: no need to notify again
        }

        processNextInQueue(leaver);
    }

    private void endChatSession(ChatSession session, boolean notify) {
        ChatLogger.saveSessionToLog(session);
        Vector<ConnectedClient> membersToEnd = new Vector<>(session.getParticipants());

        serverState.endSession(session);

        for (ConnectedClient member : membersToEnd) {
            if (notify && member.getSocket().isConnected()) {
                member.getOutputStream().println("The chat session has ended. You are now available.");
            }
            member.setAvailable(true);
            member.setCurrentSession(null);
            member.setPendingRequestTo(null);
            processNextInQueue(member);
        }
    }

    private void handleChatRequest(ConnectedClient requester, String targetIdOrName) {
        ConnectedClient target = serverState.findClientById(targetIdOrName);

        if (target == null || target == requester) {
            requester.getOutputStream().println(target == null ? "Target not found." : "You cannot chat with yourself.");
            return;
        }

        if (target.isAvailable()) {
            requester.setAvailable(false);
            target.setAvailable(false);
            requester.setPendingRequestTo(target);
            target.getOutputStream().println("Client " + requester.getName() + " wants to chat with you. Reply 'yes' or 'no'.");
        } else {
            serverState.enqueueClient(target, requester);
            requester.getOutputStream().println("Client " + target.getName() + " is busy. You have been placed in queue position " + serverState.getQueueSize(target) + ".");
        }
    }


    private void handleJoinRequest(ConnectedClient manager, String targetIdOrName) {
        ConnectedClient target = serverState.findClientById(targetIdOrName);
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

        String joinMsg = "A manager (" + manager.getName() + ") has joined the chat.";
        manager.getOutputStream().println("You have joined the chat.");
        session.getParticipants().stream()
                .filter(member -> member != manager)
                .forEach(member -> member.getOutputStream().println(joinMsg));
    }

    private void acceptChatRequest(ConnectedClient replier) {
        ConnectedClient requester = serverState.findRequesterFor(replier);



        if (requester != null) {

            requester.setPendingRequestTo(null);

            ChatSession session = new ChatSession(requester, replier);

            requester.setInChatMode(true);
            replier.setInChatMode(true);

            requester.setCurrentSession(session);
            requester.setAvailable(false);

            replier.setCurrentSession(session);
            replier.setAvailable(false);

            serverState.startSession(session);

            String chatStartedMsg = "You are now in a private chat. Use 'goodbye' or 'savechat'.";
            requester.getOutputStream().println("Chat request accepted. " + chatStartedMsg);
            replier.getOutputStream().println("You accepted the request. " + chatStartedMsg);
        }
    }

    private void rejectChatRequest(ConnectedClient replier) {
        ConnectedClient requester = serverState.findRequesterFor(replier);
        if (requester != null) {
            requester.getOutputStream().println("Chat request rejected by " + replier.getName());
            requester.setAvailable(true);
            requester.setPendingRequestTo(null);

            replier.setAvailable(true);
            replier.getOutputStream().println("You rejected the chat request.");
            processNextInQueue(replier);
        }
    }

    private void processNextInQueue(ConnectedClient freedClient) {
        ConnectedClient nextRequester = serverState.dequeueClient(freedClient);
        if (nextRequester != null && serverState.getAllConnectedClients().contains(nextRequester)) {
            freedClient.setAvailable(false);
            nextRequester.setAvailable(false);
            nextRequester.setPendingRequestTo(freedClient);

            freedClient.getOutputStream().println("Client " + nextRequester.getName() + " from your queue wants to chat. Reply 'yes' or 'no'.");
            nextRequester.getOutputStream().println("Your request is now active. Waiting for " + freedClient.getName() + " to respond.");
        } else {
            // No queued requester; ensure freed client is available
            freedClient.setAvailable(true);
        }
    }

    private void sendPrivateMessage(ConnectedClient sender, String message) {
        ChatSession session = sender.getCurrentSession();
        if (session == null) return;
        session.appendMessage(sender, message);
        session.getParticipants().stream()
                .filter(receiver -> receiver != sender && receiver.getSocket().isConnected())
                .forEach(receiver -> receiver.getOutputStream().println(sender.getName() + ": " + message));
    }

//    private void sendAvailableClients(ConnectedClient requester) {
//        requester.getOutputStream().println("Available clients:");
//        serverState.getAllConnections().stream()
//                .filter(sd -> sd.isAvailable() && sd != requester)
//                .forEach(sd -> requester.getOutputStream().println(" - " + sd.getName()));
//    }

    private void sendAvailableClients(ConnectedClient requester) {
        List<ConnectedClient> available = serverState.getAllConnectedClients().stream()
                .filter(connectedClient -> connectedClient.isAvailable() && connectedClient != requester)
                .toList(); // Java 16+ (otherwise use .collect(Collectors.toList()))

        if (available.isEmpty()) {
            requester.getOutputStream().println("No other person is connected currently.");
        } else {
            requester.getOutputStream().println("Available clients:");
            available.forEach(sd -> requester.getOutputStream().println(" - " + sd.getName()));
        }
    }

//    private void sendAllClients(ConnectedClient requester) {
//        requester.getOutputStream().println("All connected clients:");
//        for (ConnectedClient sd : serverState.getAllConnections()) {
//            if (sd == requester) continue;
//            if (sd.isAvailable()) {
//                requester.getOutputStream().println(" - " + sd.getName() + " (available)");
//            } else {
//                ChatSession s = sd.getCurrentSession();
//                String status;
//                if (s != null) {
//                    String participants = s.getParticipants().stream()
//                            .map(ConnectedClient::getName)
//                            .collect(Collectors.joining(" <-> "));
//                    status = "in a chat: " + participants;
//                } else if (sd.getPendingRequestTo() != null) {
//                    status = "pending chat with " + sd.getPendingRequestTo().getName();
//                } else {
//                    status = "busy";
//                }
//                int queueSize = serverState.getQueueSize(sd);
//                requester.getOutputStream().println(" - " + sd.getName() + " (" + status + ", " + queueSize + " in queue)");
//            }
//        }
//    }

    private void sendAllClients(ConnectedClient requester) {
        // --- Active chats ---
        List<ChatSession> activeChats = serverState.getAllActiveChatSessionsWithoutDuplicates();

        if (activeChats.isEmpty()) {
            requester.getOutputStream().println("No active chats currently.");
        } else {
            requester.getOutputStream().println("Currently open chats:");
            for (ChatSession session : activeChats) {
                String participants = session.getParticipants().stream()
                        .map(ConnectedClient::getName)
                        .collect(Collectors.joining(" <-> "));
                requester.getOutputStream().println(" - " + participants);
            }
        }
    }

}