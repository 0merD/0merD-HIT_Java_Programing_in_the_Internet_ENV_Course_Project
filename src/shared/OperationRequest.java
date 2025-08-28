package shared;

import java.util.Map;

public class OperationRequest {

    private final OperationTypeEnum operationType;
    private final Map<String, String> mapDataRequested;

    public OperationRequest(OperationTypeEnum operationType, Map<String, String> mapDataRequested) {
        this.operationType = operationType;
        this.mapDataRequested = mapDataRequested;
    }

    // Getters
    public OperationTypeEnum getOperationType() {
        return operationType;
    }

    public Map<String, String> getPayload() {
        return mapDataRequested;
    }

    @Override
    public String toString() {
        return String.format("OperationRequest{ operationType=%s, payload=%s }",
                operationType, mapDataRequested);
    }
}

