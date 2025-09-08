package shared;

import java.util.EnumSet;

public enum OperationTypeEnum {
    // Admin only operations
    ADD_USER("1", "Add User", EnumSet.of(UserType.Admin)),
    DELETE_USER("2", "Delete User", EnumSet.of(UserType.Admin)),
    MODIFY_USER_ROLE("3", "Modify User Role", EnumSet.of(UserType.Admin)),

    // ShiftManager only Operations
    VIEW_CURRENT_OPEN_CHATS("4", "View Current Open Chats", EnumSet.of(UserType.ShiftManager)),
    JOIN_EXISTING_CHAT("5", "Join Existing Chat", EnumSet.of(UserType.ShiftManager)),

    // ShiftManager + BasicWorker operations
    VIEW_BRANCH_INVENTORY("6", "View Branch Inventory",  EnumSet.of(UserType.ShiftManager, UserType.BasicWorker)),
    ADD_CUSTOMER("7", "Add Customer", EnumSet.of(UserType.ShiftManager, UserType.BasicWorker)),
    VIEW_PRODUCT_PRICE("8", "View Product Price", EnumSet.of(UserType.ShiftManager, UserType.BasicWorker)),
    VIEW_ALL_CUSTOMERS("9", "View All Customers",  EnumSet.of(UserType.ShiftManager, UserType.BasicWorker)),
    EXECUTE_SALE("10", "Execute Sale", EnumSet.of(UserType.ShiftManager, UserType.BasicWorker)),

    // Logout All
    LOGOUT("11", "Logout", EnumSet.allOf(UserType.class)),

    // Chat operations (moved here; unique codes)
    VIEW_AVAILABLE_TO_CHAT("12", "View Available To Chat", EnumSet.of(UserType.ShiftManager, UserType.BasicWorker)),
    REQUEST_CHAT("13", "Request Chat", EnumSet.of(UserType.ShiftManager, UserType.BasicWorker)),
   // CHAT_SEND_MESSAGE("14", "Send Chat Message", EnumSet.of(UserType.ShiftManager, UserType.BasicWorker)),
   // CHAT_GOODBYE("15", "Leave/End Chat", EnumSet.of(UserType.ShiftManager, UserType.BasicWorker)),
    SAVE_CHAT("16", "Save Current Chat", EnumSet.of(UserType.ShiftManager, UserType.BasicWorker)),
    CHAT_INVITE_RESPONSE("17", "Respond Yes/No to Chat Invite", EnumSet.of(UserType.ShiftManager, UserType.BasicWorker));

    private final String code;
    private final String description;
    private final EnumSet<UserType> allowedRoles;

    OperationTypeEnum(String code, String description, EnumSet<UserType> allowedUserTypes) {
        this.code = code;
        this.description = description;
        this.allowedRoles = allowedUserTypes;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public EnumSet<UserType> getRequiredUserType() {
        return allowedRoles;
    }

    public static OperationTypeEnum getOperationType(String code) {
        for (OperationTypeEnum operationType : OperationTypeEnum.values()) {
            if (operationType.getCode().equals(code)) {
                return operationType;
            }
        }
        return null;
    }
}