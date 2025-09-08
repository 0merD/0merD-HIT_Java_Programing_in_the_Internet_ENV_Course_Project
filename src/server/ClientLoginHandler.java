package server;

import shared.OperationTypeEnum;
import shared.UserType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ClientLoginHandler extends Thread {
    private final Socket socket;

    // Note to myself - like Action in C#
    private final Map<OperationTypeEnum, BiConsumer<BufferedReader, PrintWriter>> operationHandlersMap = new EnumMap<>(OperationTypeEnum.class);

    // Managers:
    CustomerManager customerManager = CustomerManager.getInstance();
    ProductsManager productsManager = new ProductsManager(new JsonProductsDataProvider());
    InventoryManager inventoryManager = InventoryManager.getInstance();

    // Chat runtime bridge
    private final ServerState serverState;
    private final CommandHandler chatCommandHandler;
    private final SocketData socketData;

    private User loggedInUser;
    private volatile boolean inChatMode = false; // chat session input mode

    public ClientLoginHandler(Socket socket, ServerState serverState) {
        this.socket = socket;
        this.serverState = serverState;
        this.chatCommandHandler = new CommandHandler(serverState);
        this.socketData = new SocketData(socket);

        initOperationHandlers();
    }

    private void initOperationHandlers() {
        operationHandlersMap.put(OperationTypeEnum.ADD_USER, this::handleAddUser);
        operationHandlersMap.put(OperationTypeEnum.DELETE_USER, this::handleDeleteUser);
        operationHandlersMap.put(OperationTypeEnum.MODIFY_USER_ROLE, this::handleModifyUserRole);

        operationHandlersMap.put(OperationTypeEnum.VIEW_CURRENT_OPEN_CHATS, this::handleViewCurrentOpenChats);
        operationHandlersMap.put(OperationTypeEnum.JOIN_EXISTING_CHAT, this::handleJoinExistingChat);

        operationHandlersMap.put(OperationTypeEnum.VIEW_AVAILABLE_TO_CHAT, this::handleViewAvailableToChat);
        operationHandlersMap.put(OperationTypeEnum.REQUEST_CHAT, this::handleRequestChat);
       // operationHandlersMap.put(OperationTypeEnum.CHAT_SEND_MESSAGE, this::handleChatSendMessage);
      //  operationHandlersMap.put(OperationTypeEnum.CHAT_GOODBYE, this::handleChatGoodbye);
        operationHandlersMap.put(OperationTypeEnum.SAVE_CHAT, this::handleSaveChat);
        operationHandlersMap.put(OperationTypeEnum.CHAT_INVITE_RESPONSE, this::handleChatInviteResponse);

        operationHandlersMap.put(OperationTypeEnum.VIEW_BRANCH_INVENTORY, this::handleViewBranchInventory);
        operationHandlersMap.put(OperationTypeEnum.VIEW_ALL_CUSTOMERS, this::handleViewAllCustomers);
        operationHandlersMap.put(OperationTypeEnum.VIEW_PRODUCT_PRICE, this::handleViewProductPrice);
        operationHandlersMap.put(OperationTypeEnum.ADD_CUSTOMER, this::handleAddCustomer);
        operationHandlersMap.put(OperationTypeEnum.EXECUTE_SALE, this::handleExecuteSale);
        operationHandlersMap.put(OperationTypeEnum.LOGOUT, this::handleLogOut);
    }

    public void run() {
        try (
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true)
        ) {
            loggedInUser = authenticateClient(input, output);
            if (loggedInUser == null) {
                output.println("Authentication failed.");
                socket.close();
                return;
            }

            // Enrich SocketData with authenticated user info and register in ServerState
            applyAuthenticatedUserToSocketData(loggedInUser);
            serverState.addClient(socketData);

            runUserSession(loggedInUser, input, output);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            // ensure cleanup in chat layer
            try {
                chatCommandHandler.handleDisconnection(socketData);
                serverState.removeClient(socketData);
            } catch (Exception ignored) { }
        }
    }

    private void applyAuthenticatedUserToSocketData(User user) {
        if (user != null) {
            socketData.setUserType(user.getUserType());
            socketData.setBranchNumber(user.getBranchNumber());
            String branchName = InventoryManager.getInstance().getBranchCityByNumber(user.getBranchNumber());
            String displayName = user.getUsername() + "@" + (branchName != null ? branchName : "unknown");
            socketData.setName(displayName);
        } else {
            socketData.setUserType(UserType.BasicWorker);
        }
    }

    private void runUserSession(User loggedInUser, BufferedReader input, PrintWriter output) throws IOException {
        UserType loggedInUserType = loggedInUser.getUserType();
        output.println(String.format("Welcome %s. You are Logged in as %s", loggedInUser, loggedInUserType));

        OperationTypeEnum[] allowedOps = Arrays.stream(OperationTypeEnum.values())
                .filter(op -> op.getRequiredUserType().contains(loggedInUserType))
                .toArray(OperationTypeEnum[]::new);

        boolean isUserMakingRequests = true;

        while (isUserMakingRequests) {
            // If chat mode is active, run chat loop (free text) until user leaves chat
            if (inChatMode) {
                runChatLoop(input, output);
                continue;
            }

            OperationTypeEnum selectedOperation = selectOperation(input, output, allowedOps);

            if (selectedOperation == null) {
                continue; // prompt again
            }

            handleOperation(selectedOperation, input, output); // perform the requested operation

            if (selectedOperation == OperationTypeEnum.LOGOUT) {
                isUserMakingRequests = false;
            }
        }
    }

    private User authenticateClient(BufferedReader input, PrintWriter output) throws IOException {
        output.println("Enter username:");
        String username = input.readLine();
        output.println("Enter password:");
        String password = input.readLine();

        // Ensure singleton is initialized before static authenticate() call to avoid NPE inside UserManager
        UserManager.getInstance();

        if (!UserManager.authenticate(username, password)) {
            output.println("Authentication failed.");
            System.out.println("Client authentication failed.");
            return null;
        }

        output.println(String.format("Welcome username: [%s] user type: [%s] Email: [%s]. You are Logged in as %s",
                username,
                UserManager.getInstance().getUserByUserName(username).getUserType(),
                UserManager.getInstance().getUserByUserName(username).getEmail(),
                UserManager.getInstance().getUserByUserName(username).getUserType()));

        return UserManager.getInstance().getUserByUserName(username);
    }

    private OperationTypeEnum selectOperation(BufferedReader input, PrintWriter output, OperationTypeEnum[] allowedOps) throws IOException {
        output.println("Select an operation: (enter number)");

        for (int i = 0; i < allowedOps.length; i++) {
            output.println((i + 1) + ". " + allowedOps[i].getDescription());
        }

        try {
            int choice = Integer.parseInt(input.readLine());
            return allowedOps[choice - 1];
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            output.println("Please enter a valid operation number.");
            return null;
        }
    }

    // Generic operation handler to run the selected operation handler
    private void handleOperation(OperationTypeEnum operation, BufferedReader input, PrintWriter output) {
        BiConsumer<BufferedReader, PrintWriter> handler = operationHandlersMap.get(operation);
        if (handler != null) {
            handler.accept(input, output);
        } else {
            output.println("Unknown operation.");
        }
    }

    // Chat-mode loop: send raw lines to CommandHandler until "goodbye"
    private void runChatLoop(BufferedReader input, PrintWriter output) throws IOException {
        output.println("You are now in chat mode. Type your message, or 'goodbye' to leave chat or chat mode, or 'savechat' to save.");
        while (inChatMode) {
            String line = input.readLine();
            if (line == null) {
                break; // socket closing
            }
            String trimmed = line.trim();
            // Delegate to existing chat command processor
            chatCommandHandler.handle(socketData, trimmed);
            if ("goodbye".equalsIgnoreCase(trimmed)) {
                // CommandHandler will end session and make user available; reflect locally
                inChatMode = false;
            }
        }
        output.println("Exited chat mode.");
    }

    // ===== Chat operations mapped to CommandHandler semantics =====

    private void handleViewAvailableToChat(BufferedReader input, PrintWriter output) {
        // maps to "list"
        chatCommandHandler.handle(socketData, "list");
    }

    private void handleViewCurrentOpenChats(BufferedReader input, PrintWriter output) {
        // maps to "listall"
        chatCommandHandler.handle(socketData, "listall");
    }

    private void handleRequestChat(BufferedReader input, PrintWriter output) {
        try {
            output.println("Enter target client name (username@branch):");
            String targetName = input.readLine().trim();
            chatCommandHandler.handle(socketData, "chat " + targetName);
            // Enable chat mode so user can type immediately; CommandHandler will confirm/queue
            inChatMode = true;
            output.println("Waiting for participant to join... You can start typing messages.");
        } catch (IOException e) {
            output.println("Failed to request chat: " + e.getMessage());
        }
    }

    private void handleJoinExistingChat(BufferedReader input, PrintWriter output) {
        try {
            output.println("Enter a participant (username@branch) of the active chat to join:");
            String joinTarget = input.readLine().trim();
            chatCommandHandler.handle(socketData, "join " + joinTarget);
            inChatMode = true;
            output.println("Joining chat... You can start typing messages.");
        } catch (IOException e) {
            output.println("Failed to join chat: " + e.getMessage());
        }
    }

    private void handleChatSendMessage(BufferedReader input, PrintWriter output) {
        try {
            if (!inChatMode) {
                output.println("You are not in a chat. Use 'Request Chat' or 'Respond to Chat Invite' first.");
                return;
            }
            output.println("Enter message to send:");
            String msg = input.readLine();
            chatCommandHandler.handle(socketData, msg);
        } catch (IOException e) {
            output.println("Failed to send message: " + e.getMessage());
        }
    }

    private void handleChatGoodbye(BufferedReader input, PrintWriter output) {
        chatCommandHandler.handle(socketData, "goodbye");
        inChatMode = false;
    }

    private void handleSaveChat(BufferedReader input, PrintWriter output) {
        chatCommandHandler.handle(socketData, "savechat");
    }

    private void handleChatInviteResponse(BufferedReader input, PrintWriter output) {
        try {
            output.println("Respond to chat invite (yes/no):");
            String response = input.readLine().trim().toLowerCase();
            if (!"yes".equals(response) && !"no".equals(response)) {
                output.println("Please answer 'yes' or 'no'.");
                return;
            }
            chatCommandHandler.handle(socketData, response);
            if ("yes".equals(response)) {
                inChatMode = true;
                output.println("Chat established. You can start typing messages.");
            }
        } catch (IOException e) {
            output.println("Failed to respond to invite: " + e.getMessage());
        }
    }

    // ===== Business operations =====

    private void handleViewBranchInventory(BufferedReader input, PrintWriter output) {
        int branchNumber = loggedInUser.getBranchNumber();
        output.println("Inventory for branch #" + branchNumber + ":");
        List<InventoryItem> inventory = InventoryManager.getInstance().getInventoryByCity(branchNumber);
        if (inventory.isEmpty()) {
            output.println("No inventory found for this branch.");
            return;
        }
        for (InventoryItem item : inventory) {
            Product p = ProductsCatalog.getProduct(item.getProductIdentifier());
            if (p == null) {
                output.println(String.format("Unknown product (%s) - Quantity: %d", item.getProductIdentifier(), item.getQuantity()));
            } else {
                output.println(String.format("%s (%s) - Price: %.2f - Quantity: %d", p.getName(), p.getProductIdentifier(), p.getPrice(), item.getQuantity()));
            }
        }
    }

    private void handleExecuteSale(BufferedReader input, PrintWriter output) {
        try {
            CustomerAbstract customer = CustomerManager.getInstance().getAllCustomers().get(1);
            int branchNumber = loggedInUser.getBranchNumber();
            output.println("Enter product ID:");
            String productId = input.readLine();
            output.println("Enter quantity:");
            int quantity = Integer.parseInt(input.readLine());

            SalesRequest request = new SalesRequest();
            request.setBranchNumber(branchNumber);
            request.setProductId(productId);
            request.setQuantity(quantity);
            request.setCustomer(customer);

            SalesManager salesManager = SalesManager.getInstance();
            SalesResult result = salesManager.processSale(request);

            output.println("Sale success: " + result.isSuccess());
            output.println("Message: " + result.getMessage());
            output.println("Original price: " + result.getOriginalPrice());
            output.println("Discount applied: " + result.getDiscountApplied());
            output.println("Final price: " + result.getFinalPrice());
        } catch (Exception e) {
            output.println("Failed to execute sale: " + e.getMessage());
        }
    }

    private void handleViewProductPrice(BufferedReader input, PrintWriter output) {
        productsManager.getAllProducts().forEach(product ->
                output.println(String.format("%s %s- Price: %.2f",
                        product.getProductIdentifier(),
                        product.getName(),
                        product.getPrice()))
        );
    }

    private void handleAddCustomer(BufferedReader input, PrintWriter output) {
        try {
            output.println("Enter customer full name:");
            String fullName = input.readLine().trim();

            output.println("Enter customer phone number:");
            String phoneNumber = input.readLine().trim();

            output.println("Enter customer type (New / Returning / Vip):");
            String typeInput = input.readLine().trim();
            CustomerTypeEnum customerType;
            try {
                customerType = CustomerTypeEnum.valueOf(typeInput);
            } catch (IllegalArgumentException e) {
                output.println("Invalid customer type. Aborting.");
                return;
            }

            String custId = "C" + (CustomerManager.getInstance().getAllCustomers().size() + 1);

            CustomerAbstract newCustomer = CustomerFactory.createCustomer(
                    custId, fullName, phoneNumber, customerType, 0
            );

            customerManager.addCustomer(newCustomer);
            output.println("Customer added successfully! ID: " + custId);
        } catch (Exception e) {
            output.println("Failed to add customer: " + e.getMessage());
        }
    }

    private void handleAddUser(BufferedReader input, PrintWriter output) {
        try {
            output.println("Enter new user's username:");
            String username = input.readLine().trim();

            output.println("Enter password:");
            String password = input.readLine().trim();

            output.println("Enter email:");
            String email = input.readLine().trim();

            output.println("Enter phone number:");
            String phoneNumber = input.readLine().trim();

            output.println("Enter account number:");
            String accountNumber = input.readLine().trim();

            output.println("Enter branch number (integer):");
            int branchNumber;
            try {
                branchNumber = Integer.parseInt(input.readLine().trim());
            } catch (NumberFormatException ex) {
                output.println("Invalid branch number. Aborting.");
                return;
            }

            output.println("Enter user type (admin/shiftmanager/basicworker) - case insensitive:");
            String roleStr = input.readLine().trim().toLowerCase();
            UserType userType = UserType.fromString(roleStr);
            if (userType == null) {
                output.println("Invalid user type. Aborting.");
                return;
            }

            // Generate a simple unique ID. Replace with your policy if needed.
            String id = "U" + System.currentTimeMillis();

            User newUser = UserFactory.createUser(
                    username,
                    id,
                    userType,
                    password,
                    email,
                    phoneNumber,
                    accountNumber,
                    branchNumber
            );

            boolean added = UserManager.getInstance().addUser(newUser);
            if (added) {
                output.println("User added successfully.");
            } else {
                output.println("Failed to add user for an unknown reason.");
            }
        } catch (IllegalArgumentException e) {
            output.println("Error: " + e.getMessage());
        } catch (IOException e) {
            output.println("IO Error: " + e.getMessage());
        }
    }

    private void handleDeleteUser(BufferedReader input, PrintWriter output) {
        try {
            output.println("Enter username of the user to delete:");
            String username = input.readLine().trim();
            User userToDelete = UserManager.getInstance().getUserByUserName(username);
            if (userToDelete == null) {
                output.println("User not found.");
                return;
            }
            UserManager.getInstance().deleteUser(userToDelete.getUsername());
            output.println("User deleted successfully.");
        } catch (IOException e) {
            output.println("Failed to delete user: " + e.getMessage());
        }
    }

    private void handleModifyUserRole(BufferedReader input, PrintWriter output) {
        try {
            output.println("Enter the username of the user to modify:");
            String username = input.readLine();
            output.println("Enter new user type (admin/shiftmanager/basicworker) - case insensitive:");
            String roleStr = input.readLine().trim().toLowerCase();
            UserType newRole = UserType.fromString(roleStr);
            if (newRole == null) {
                output.println("Invalid user type.");
                return;
            }
            boolean success = UserManager.getInstance().modifyUserRole(username, newRole);
            if (success) {
                output.println("User role updated successfully.");
            } else {
                output.println("Failed to update role.");
            }
        } catch (IllegalArgumentException e) {
            output.println("Error: " + e.getMessage());
        } catch (IOException e) {
            output.println("IO Error: " + e.getMessage());
        }
    }

    private void handleViewAllCustomers(BufferedReader input, PrintWriter output) {
        output.println("=== All Customers ===");
        List<CustomerAbstract> customers = CustomerManager.getInstance().getAllCustomers();
        if (customers.isEmpty()) {
            output.println("No customers found.");
            return;
        }
        for (CustomerAbstract customer : customers) {
            output.println(String.format(
                    "Name: %s, ID: %s, Phone: %s, Type: %s",
                    customer.getFullName(),
                    customer.getCustId(),
                    customer.getPhoneNumber(),
                    customer.getCustomerType()
            ));
        }
    }

    private void handleLogOut(BufferedReader input, PrintWriter output) {
        loggedInUser = null;
        inChatMode = false;
        output.println("You have been logged out.");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}