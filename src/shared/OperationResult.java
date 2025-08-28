package shared;

public class OperationResult {

    private final boolean isSuccess;
    private final String message;

    public OperationResult(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    // getters
    public boolean getIsSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("OperationResult{ succes=%s message='%s' }",isSuccess, message);
    }
}
