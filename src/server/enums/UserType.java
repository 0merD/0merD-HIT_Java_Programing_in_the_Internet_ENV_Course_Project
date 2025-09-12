package server.enums;

public enum UserType {
    Admin,
    ShiftManager,
    BasicWorker;


    public static UserType fromString(String str) {
        for (UserType type : values()) {
            if (type.name().equalsIgnoreCase(str)) {
                return type;
            }
        }

        return null;
    }
}
