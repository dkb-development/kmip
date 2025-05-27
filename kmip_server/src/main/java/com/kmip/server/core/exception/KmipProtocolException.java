package com.kmip.server.core.exception;

import com.kmip.server.core.enums.KmipOperationType;

/**
 * Exception thrown when there are KMIP protocol-level issues.
 * 
 * This exception represents problems with KMIP protocol compliance,
 * message structure, invalid operations, or protocol violations.
 * 
 * This is part of the protocol layer (Part-1) and should not contain
 * business logic related exceptions.
 */
public class KmipProtocolException extends KmipException {
    
    private static final long serialVersionUID = 1L;
    
    private final ProtocolErrorType errorType;
    private final KmipOperationType operationType;
    private final String protocolVersion;
    
    /**
     * Enumeration of protocol error types for better categorization.
     */
    public enum ProtocolErrorType {
        INVALID_MESSAGE_STRUCTURE("Invalid Message Structure"),
        UNSUPPORTED_OPERATION("Unsupported Operation"),
        INVALID_OPERATION_CODE("Invalid Operation Code"),
        MISSING_REQUIRED_FIELD("Missing Required Field"),
        INVALID_FIELD_VALUE("Invalid Field Value"),
        PROTOCOL_VERSION_MISMATCH("Protocol Version Mismatch"),
        INVALID_BATCH_COUNT("Invalid Batch Count"),
        INVALID_TAG("Invalid Tag"),
        INVALID_TYPE("Invalid Type"),
        INVALID_LENGTH("Invalid Length"),
        MALFORMED_REQUEST("Malformed Request"),
        MALFORMED_RESPONSE("Malformed Response"),
        UNSUPPORTED_PROTOCOL_VERSION("Unsupported Protocol Version"),
        UNKNOWN("Unknown Protocol Error");
        
        private final String description;
        
        ProtocolErrorType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Constructs a new KmipProtocolException with the specified detail message.
     * 
     * @param message the detail message
     */
    public KmipProtocolException(String message) {
        super(message);
        this.errorType = ProtocolErrorType.UNKNOWN;
        this.operationType = null;
        this.protocolVersion = null;
    }
    
    /**
     * Constructs a new KmipProtocolException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public KmipProtocolException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ProtocolErrorType.UNKNOWN;
        this.operationType = null;
        this.protocolVersion = null;
    }
    
    /**
     * Constructs a new KmipProtocolException with the specified error type and message.
     * 
     * @param errorType the type of protocol error
     * @param message the detail message
     */
    public KmipProtocolException(ProtocolErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.operationType = null;
        this.protocolVersion = null;
    }
    
    /**
     * Constructs a new KmipProtocolException with the specified error type, message, and cause.
     * 
     * @param errorType the type of protocol error
     * @param message the detail message
     * @param cause the cause
     */
    public KmipProtocolException(ProtocolErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.operationType = null;
        this.protocolVersion = null;
    }
    
    /**
     * Constructs a new KmipProtocolException with operation details.
     * 
     * @param errorType the type of protocol error
     * @param message the detail message
     * @param operationType the KMIP operation type
     */
    public KmipProtocolException(ProtocolErrorType errorType, String message, KmipOperationType operationType) {
        super(message);
        this.errorType = errorType;
        this.operationType = operationType;
        this.protocolVersion = null;
    }
    
    /**
     * Constructs a new KmipProtocolException with protocol version details.
     * 
     * @param errorType the type of protocol error
     * @param message the detail message
     * @param protocolVersion the protocol version
     */
    public KmipProtocolException(ProtocolErrorType errorType, String message, String protocolVersion) {
        super(message);
        this.errorType = errorType;
        this.operationType = null;
        this.protocolVersion = protocolVersion;
    }
    
    /**
     * Constructs a new KmipProtocolException with full details.
     * 
     * @param errorType the type of protocol error
     * @param message the detail message
     * @param operationType the KMIP operation type
     * @param protocolVersion the protocol version
     */
    public KmipProtocolException(ProtocolErrorType errorType, String message, 
                                 KmipOperationType operationType, String protocolVersion) {
        super(message);
        this.errorType = errorType;
        this.operationType = operationType;
        this.protocolVersion = protocolVersion;
    }
    
    /**
     * Gets the protocol error type.
     * 
     * @return the error type
     */
    public ProtocolErrorType getErrorType() {
        return errorType;
    }
    
    /**
     * Gets the KMIP operation type if available.
     * 
     * @return the operation type, or null if not available
     */
    public KmipOperationType getOperationType() {
        return operationType;
    }
    
    /**
     * Gets the protocol version if available.
     * 
     * @return the protocol version, or null if not available
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorType.getDescription()).append("] ");
        sb.append(super.getMessage());
        
        if (operationType != null) {
            sb.append(" (Operation: ").append(operationType.getDisplayName()).append(")");
        }
        
        if (protocolVersion != null) {
            sb.append(" (Protocol Version: ").append(protocolVersion).append(")");
        }
        
        return sb.toString();
    }
    
    /**
     * Creates an unsupported operation exception.
     * 
     * @param operationType the unsupported operation
     * @return a new KmipProtocolException
     */
    public static KmipProtocolException unsupportedOperation(KmipOperationType operationType) {
        return new KmipProtocolException(
            ProtocolErrorType.UNSUPPORTED_OPERATION,
            "Operation is not supported by this server",
            operationType
        );
    }
    
    /**
     * Creates an invalid operation code exception.
     * 
     * @param operationCode the invalid operation code
     * @return a new KmipProtocolException
     */
    public static KmipProtocolException invalidOperationCode(int operationCode) {
        return new KmipProtocolException(
            ProtocolErrorType.INVALID_OPERATION_CODE,
            "Invalid operation code: " + String.format("0x%06X", operationCode)
        );
    }
    
    /**
     * Creates a missing required field exception.
     * 
     * @param fieldName the name of the missing field
     * @param operationType the operation type
     * @return a new KmipProtocolException
     */
    public static KmipProtocolException missingRequiredField(String fieldName, KmipOperationType operationType) {
        return new KmipProtocolException(
            ProtocolErrorType.MISSING_REQUIRED_FIELD,
            "Missing required field: " + fieldName,
            operationType
        );
    }
    
    /**
     * Creates an invalid message structure exception.
     * 
     * @param details the details of the structural issue
     * @return a new KmipProtocolException
     */
    public static KmipProtocolException invalidMessageStructure(String details) {
        return new KmipProtocolException(
            ProtocolErrorType.INVALID_MESSAGE_STRUCTURE,
            "Invalid message structure: " + details
        );
    }
    
    /**
     * Creates a protocol version mismatch exception.
     * 
     * @param clientVersion the client's protocol version
     * @param serverVersion the server's protocol version
     * @return a new KmipProtocolException
     */
    public static KmipProtocolException protocolVersionMismatch(String clientVersion, String serverVersion) {
        return new KmipProtocolException(
            ProtocolErrorType.PROTOCOL_VERSION_MISMATCH,
            String.format("Protocol version mismatch - Client: %s, Server: %s", clientVersion, serverVersion)
        );
    }
}
