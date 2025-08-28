package server;

import jdk.dynalink.Operation;
import shared.OperationTypeEnum;
import shared.UserType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ClientLoginHandler extends Thread {
    private final Socket socket;

    // Note to myself - like Action in C#
    private final Map<OperationTypeEnum, BiConsumer<BufferedReader, PrintWriter>> operationHandlersMap = new EnumMap<>(OperationTypeEnum.class);


    public ClientLoginHandler(Socket socket) {
        this.socket = socket;
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
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        ) {

            User loggedInUser =  authenticateClient(input, output);

            if (loggedInUser == null) {
                output.println("Authentication failed.");
                socket.close();
                return;
            }

            runUserSession(loggedInUser, input, output);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
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

        if (!UserManager.authenticate(username, password)) {
            output.println("Authentication failed.");
            System.out.println("Client authentication failed.");
            return null;
        }

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
    private void handleOperation(OperationTypeEnum operation,BufferedReader input, PrintWriter output) {
        BiConsumer<BufferedReader, PrintWriter> handler = operationHandlersMap.get(operation);
        if (handler != null) {
            handler.accept(input, output);
        } else {
            output.println("Unknown operation.");
        }
    }


    // Operation handlers
    private void handleAddUser(BufferedReader input, PrintWriter output) {
        // Todo: Replace Temporary user with requesting User Data from Client.
        User tempUser = new BasicWorker("basic", "23", "1234", "basic@gmail.com", "0547738844", "9876", 2, UserType.BasicWorker);

        try {
            UserManager.getInstance().addUser(tempUser);
            output.println("User added successfully.");
        } catch (IOException e) {
            output.println("Failed to add user: " + e.getMessage());
        }
    }

    private void handleDeleteUser(BufferedReader input, PrintWriter output) {
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }

    private void handleModifyUserRole(BufferedReader input, PrintWriter output) {
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }

    private void handleViewBranchInventory(BufferedReader input, PrintWriter output) {
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }

    private void handleExecuteSale(BufferedReader input, PrintWriter output) {
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }

    private void handleRequestChat(BufferedReader input, PrintWriter output) {
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }

    private void handleViewAvailableToChat(BufferedReader input, PrintWriter output) {
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }

    private void handleViewProductPrice(BufferedReader input, PrintWriter output) {
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }

    private void handleAddCustomer(BufferedReader input, PrintWriter output) {
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }

    private void handleJoinExistingChat(BufferedReader input, PrintWriter output) {
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }

    private void handleViewCurrentOpenChats(BufferedReader input, PrintWriter output) {
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }

    private void handleViewAllCustomers(BufferedReader input, PrintWriter output) {
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }


    private void handleLogOut(BufferedReader input, PrintWriter output) {
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }

}