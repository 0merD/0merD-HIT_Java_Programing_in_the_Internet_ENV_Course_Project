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


    //Managers:
    CustomerManager customerManager = CustomerManager.getInstance();
    ProductsManager productsManager = new ProductsManager(new JsonProductsDataProvider());
    InventoryManager inventoryManager = InventoryManager.getInstance();
    ChatManager chatManager = new ChatManager();

    private User loggedInUser;

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

            loggedInUser =  authenticateClient(input, output);

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


//    // Operation handlers
//    private void handleAddUser(BufferedReader input, PrintWriter output) {
//
//        try {
//            //Todo: implement validations.
//            output.println("Enter username:");
//            String username = input.readLine();
//
//            output.println("Enter id:");
//            String id = input.readLine();
//
//            output.println("Enter password:");
//            String password = input.readLine();
//
//            output.println("Enter email:");
//            String email = input.readLine();
//
//            output.println("Enter phone number:");
//            String phone = input.readLine();
//
//            output.println("Enter account number:");
//            String accountNumber = input.readLine();
//
//            output.println("Enter branch number:");
//            int branchNumber = Integer.parseInt(input.readLine());
//
//            output.println("enter user type: admin/shiftmanager/basicworker - case insensitive");
//            String userTypeStr = input.readLine();
//            UserType userType = UserType.fromString(userTypeStr);
//
//            if (userType == null) {
//                output.println("Invalid user type.");
//            }
//
//            User newUser = new BasicWorker(username, id, password, email, phone, accountNumber, branchNumber, userType);
//
//            UserManager.getInstance().addUser(newUser);
//            output.println("User added successfully.");
//
//        } catch (IOException e) {
//            output.println("Failed to add user: " + e.getMessage());
//        } catch (NumberFormatException e) {
//            output.println("Invalid number entered for branch.");
//        }
//    }

    private void handleAddUser(BufferedReader input, PrintWriter output) {
        try {
            User newUser = ValidationsService.requestAndValidateUser(input, output);
            UserManager.getInstance().addUser(newUser);

            output.println("User added successfully.");

            // Log success
            LogAction.logUserAction("Add User",
                    newUser.getUsername(),
                    newUser.getUserType().toString(),
                    "SUCCESS"
            );

        } catch (IllegalArgumentException e) {
            output.println("Validation error: " + e.getMessage());

            // Log failure
            LogAction.logUserFailure("Add User");

        } catch (IOException e) {
            output.println("Failed to add user: " + e.getMessage());

            // Log failure
            LogAction.logUserFailure("Add User");
        }
    }

    //Todo: merge with Ilan's Validations Service.
//    private void RequestNewUserData() {
//        ValidationsService.
//    }

//    private void handleDeleteUser(BufferedReader input, PrintWriter output) {
//        //Todo: implement validations.
//        try {
//            output.println("Enter username of the user to delete:");
//            String username = input.readLine().trim();
//
//            User userToDelete = UserManager.getInstance().getUserByUserName(username);
//
//            if (userToDelete == null) {
//                output.println("User not found.");
//                return;
//            }
//            String userNameStrOfUserToDelete = userToDelete.getUsername();
//            UserManager.getInstance().deleteUser(userNameStrOfUserToDelete);
//            output.println("User deleted successfully.");
//
//        } catch (IOException e) {
//            output.println("Failed to delete user: " + e.getMessage());
//        }
//    }

    private void handleDeleteUser(BufferedReader input, PrintWriter output) {
        try {
            output.println("Enter username of the user to delete:");

            String username;
            try {
                username = ValidationsService.validateUsername(input.readLine());
            } catch (IllegalArgumentException e) {
                output.println("Validation error: " + e.getMessage());
                return;
            }

            User userToDelete = UserManager.getInstance().getUserByUserName(username);
            if (userToDelete == null) {
                output.println("User not found.");
                return;
            }

            UserManager.getInstance().deleteUser(userToDelete.getUsername());
            LogAction.logUserAction("Delete User", userToDelete.getUsername(), userToDelete.getUserType().toString(), "SUCCESS");
            output.println("User deleted successfully.");

        } catch (IOException e) {
            output.println("Failed to delete user: " + e.getMessage());
        }
    }

    private void handleModifyUserRole(BufferedReader input, PrintWriter output) {
        //Todo: implement validations.
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
            }

        } catch (IllegalArgumentException e) {
            output.println("Error: " + e.getMessage());
        } catch (IOException e) {
            output.println("IO Error: " + e.getMessage());
        }
    }

    private void handleViewBranchInventory(BufferedReader input, PrintWriter output) {
        int branchNumber = loggedInUser.getBranchNumber(); // get the user’s branch number

        output.println("Inventory for branch #" + branchNumber + ":");

        // Use the InventoryManager Singleton instance to get the inventory
        InventoryManager inventoryManager = InventoryManager.getInstance();
        List<InventoryItem> inventory = inventoryManager.getInventoryByCity(branchNumber);

        if (inventory.isEmpty()) {
            output.println("No inventory found for this branch.");
            return;
        }

        // Display the inventory items
        for (InventoryItem item : inventory) {
            Product p = ProductsCatalog.getProduct(item.getProductIdentifier());
            if (p == null) {
                output.println(String.format(
                        "Unknown product (%s) - Quantity: %d",
                        item.getProductIdentifier(),
                        item.getQuantity()
                ));
            } else {
                output.println(String.format(
                        "%s (%s) - Price: %.2f - Quantity: %d",
                        p.getName(),
                        p.getProductIdentifier(),
                        p.getPrice(),
                        item.getQuantity()
                ));
            }
        }

    }

//    private void handleExecuteSale(BufferedReader input, PrintWriter output) {
//
//        //TODO: For simplicity currently implemented using try catch simple structure.
//        // Modify To correctly ask for this.
//        try {
//
//            // Pick a customer (for now, just first existing)
//            CustomerAbstract customer = CustomerManager.getInstance().getAllCustomers().get(1);
//
//            int branchNumber = loggedInUser.getBranchNumber();
//
//            output.println("Enter product ID:");
//            String productId = input.readLine();
//
//            output.println("Enter quantity:");
//            int quantity = Integer.parseInt(input.readLine());
//
//            SalesRequest request = new SalesRequest();
//            request.setBranchNumber(branchNumber);
//            request.setProductId(productId);
//            request.setQuantity(quantity);
//            request.setCustomer(customer);
//
//
//            SalesManager salesManager = SalesManager.getInstance();
//            SalesResult result = salesManager.processSale(request);
//
//            output.println("Sale success: " + result.isSuccess());
//            output.println("Message: " + result.getMessage());
//            output.println("Original price: " + result.getOriginalPrice());
//            output.println("Discount applied: " + result.getDiscountApplied());
//            output.println("Final price: " + result.getFinalPrice());
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    private void handleExecuteSale(BufferedReader input, PrintWriter output) {
        try {
            int branchNumber = loggedInUser.getBranchNumber();

            // Ask for customer
            output.println("Enter customer ID:");
            String customerId = input.readLine().trim();
            CustomerAbstract customer = CustomerManager.getInstance().getCustomerById(customerId);

            if (customer == null) {
                output.println("Customer not found.");
                return;
            }

            // Ask for product
            output.println("Enter product ID:");
            String productId = input.readLine().trim();

            // Ask for quantity
            output.println("Enter quantity:");
            String qtyStr = input.readLine().trim();
            int quantity;
            try {
                quantity = Integer.parseInt(qtyStr);
                if (quantity <= 0) {
                    output.println("Quantity must be positive.");
                    return;
                }
            } catch (NumberFormatException e) {
                output.println("Invalid quantity.");
                return;
            }

            // Build request
            SalesRequest request = new SalesRequest(branchNumber,productId,quantity,customer);

            // Process
            SalesResult result = SalesManager.getInstance().processSale(request);

            // Output
            output.println("Sale success: " + result.isSuccess());
            output.println("Message: " + result.getMessage());
            output.println("Original price: " + result.getOriginalPrice());
            output.println("Discount applied: " + result.getDiscountApplied());
            output.println("Final price: " + result.getFinalPrice());

        } catch (Exception e) {
            e.printStackTrace();
            output.println("Error while processing sale: " + e.getMessage());
        }
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
        productsManager.getAllProducts().forEach(product -> {
            output.println(String.format(
                    "%s %s- Price: %.2f",
                    product.getProductIdentifier(),
                    product.getName(),
                    product.getPrice()
            ));
        });
        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }
//
//    private void handleAddCustomer(BufferedReader input, PrintWriter output) {
//
//        try {
//            output.println("Enter customer full name:");
//            String fullName = input.readLine().trim();
//
//            output.println("Enter customer phone number:");
//            String phoneNumber = input.readLine().trim();
//
//            output.println("Enter customer type (New / Returning / Vip):");
//            String typeInput = input.readLine().trim();
//            CustomerTypeEnum customerType;
//            try {
//                customerType = CustomerTypeEnum.valueOf(typeInput);
//            } catch (IllegalArgumentException e) {
//                output.println("Invalid customer type. Aborting.");
//                return;
//            }
//
//            String custId = "C" + (CustomerManager.getInstance().getAllCustomers().size() + 1);
//
//            CustomerAbstract newCustomer = CustomerFactory.createCustomer(
//                    custId,
//                    fullName,
//                    phoneNumber,
//                    customerType,
//                    0 // new Customer hasn't spent anything yet.
//            );
//
//            customerManager.addCustomer(newCustomer);
//
//            output.println("Customer added successfully! ID: " + custId);
//
//
//            String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
//            output.println("Method: " + methodName);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            output.println("Failed to add customer: " + e.getMessage());
//        }
//
//    }

    private void handleAddCustomer(BufferedReader input, PrintWriter output) {

        try {

            CustomerAbstract newCustomer = ValidationsService.requestAndValidateCustomer(input, output);

            if (newCustomer != null) {
                customerManager.addCustomer(newCustomer);
                output.println("Customer added successfully! ID: " + newCustomer.getCustId());
            }

        } catch (Exception e) {
            e.printStackTrace();
            output.println("Failed to add customer: " + e.getMessage());
        }
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

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        output.println("Method: " + methodName);
    }


    private void handleLogOut(BufferedReader input, PrintWriter output) {
        //loggedInUsers.remove(currentUser.getUsername());
        loggedInUser = null;
        output.println("You have been logged out.");

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}