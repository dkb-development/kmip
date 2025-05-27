package com.kmip.server.core.exception;

/**
 * Exception thrown when there are transport-layer issues in the KMIP server.
 * 
 * This exception represents problems with the underlying network transport,
 * SSL/TLS connections, socket operations, or other transport-related issues.
 * 
 * This is part of the protocol layer (Part-1) and should not contain
 * business logic related exceptions.
 */
public class KmipTransportException extends KmipException {
    
    private static final long serialVersionUID = 1L;
    
    private final TransportErrorType errorType;
    private final String remoteAddress;
    private final Integer remotePort;
    
    /**
     * Enumeration of transport error types for better categorization.
     */
    public enum TransportErrorType {
        CONNECTION_FAILED("Connection Failed"),
        CONNECTION_TIMEOUT("Connection Timeout"),
        CONNECTION_RESET("Connection Reset"),
        SSL_HANDSHAKE_FAILED("SSL Handshake Failed"),
        SOCKET_ERROR("Socket Error"),
        IO_ERROR("I/O Error"),
        PROTOCOL_VIOLATION("Protocol Violation"),
        MESSAGE_TOO_LARGE("Message Too Large"),
        INVALID_MESSAGE_FORMAT("Invalid Message Format"),
        ENCODING_ERROR("Encoding Error"),
        DECODING_ERROR("Decoding Error"),
        UNKNOWN("Unknown Transport Error");
        
        private final String description;
        
        TransportErrorType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Constructs a new KmipTransportException with the specified detail message.
     * 
     * @param message the detail message
     */
    public KmipTransportException(String message) {
        super(message);
        this.errorType = TransportErrorType.UNKNOWN;
        this.remoteAddress = null;
        this.remotePort = null;
    }
    
    /**
     * Constructs a new KmipTransportException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public KmipTransportException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = TransportErrorType.UNKNOWN;
        this.remoteAddress = null;
        this.remotePort = null;
    }
    
    /**
     * Constructs a new KmipTransportException with the specified error type and message.
     * 
     * @param errorType the type of transport error
     * @param message the detail message
     */
    public KmipTransportException(TransportErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.remoteAddress = null;
        this.remotePort = null;
    }
    
    /**
     * Constructs a new KmipTransportException with the specified error type, message, and cause.
     * 
     * @param errorType the type of transport error
     * @param message the detail message
     * @param cause the cause
     */
    public KmipTransportException(TransportErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.remoteAddress = null;
        this.remotePort = null;
    }
    
    /**
     * Constructs a new KmipTransportException with connection details.
     * 
     * @param errorType the type of transport error
     * @param message the detail message
     * @param remoteAddress the remote client address
     * @param remotePort the remote client port
     */
    public KmipTransportException(TransportErrorType errorType, String message, 
                                  String remoteAddress, Integer remotePort) {
        super(message);
        this.errorType = errorType;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }
    
    /**
     * Constructs a new KmipTransportException with connection details and cause.
     * 
     * @param errorType the type of transport error
     * @param message the detail message
     * @param cause the cause
     * @param remoteAddress the remote client address
     * @param remotePort the remote client port
     */
    public KmipTransportException(TransportErrorType errorType, String message, Throwable cause,
                                  String remoteAddress, Integer remotePort) {
        super(message, cause);
        this.errorType = errorType;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }
    
    /**
     * Gets the transport error type.
     * 
     * @return the error type
     */
    public TransportErrorType getErrorType() {
        return errorType;
    }
    
    /**
     * Gets the remote client address if available.
     * 
     * @return the remote address, or null if not available
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }
    
    /**
     * Gets the remote client port if available.
     * 
     * @return the remote port, or null if not available
     */
    public Integer getRemotePort() {
        return remotePort;
    }
    
    /**
     * Checks if this exception has connection details.
     * 
     * @return true if remote address and port are available, false otherwise
     */
    public boolean hasConnectionDetails() {
        return remoteAddress != null && remotePort != null;
    }
    
    /**
     * Gets a formatted string with connection details.
     * 
     * @return formatted connection string, or empty string if no details available
     */
    public String getConnectionDetails() {
        if (hasConnectionDetails()) {
            return String.format("%s:%d", remoteAddress, remotePort);
        }
        return "";
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorType.getDescription()).append("] ");
        sb.append(super.getMessage());
        
        if (hasConnectionDetails()) {
            sb.append(" (Client: ").append(getConnectionDetails()).append(")");
        }
        
        return sb.toString();
    }
    
    /**
     * Creates a connection failed exception.
     * 
     * @param remoteAddress the remote address
     * @param remotePort the remote port
     * @param cause the underlying cause
     * @return a new KmipTransportException
     */
    public static KmipTransportException connectionFailed(String remoteAddress, Integer remotePort, Throwable cause) {
        return new KmipTransportException(
            TransportErrorType.CONNECTION_FAILED,
            "Failed to establish connection",
            cause,
            remoteAddress,
            remotePort
        );
    }
    
    /**
     * Creates an SSL handshake failed exception.
     * 
     * @param remoteAddress the remote address
     * @param remotePort the remote port
     * @param cause the underlying cause
     * @return a new KmipTransportException
     */
    public static KmipTransportException sslHandshakeFailed(String remoteAddress, Integer remotePort, Throwable cause) {
        return new KmipTransportException(
            TransportErrorType.SSL_HANDSHAKE_FAILED,
            "SSL handshake failed",
            cause,
            remoteAddress,
            remotePort
        );
    }
    
    /**
     * Creates an encoding error exception.
     * 
     * @param message the error message
     * @param cause the underlying cause
     * @return a new KmipTransportException
     */
    public static KmipTransportException encodingError(String message, Throwable cause) {
        return new KmipTransportException(TransportErrorType.ENCODING_ERROR, message, cause);
    }
    
    /**
     * Creates a decoding error exception.
     * 
     * @param message the error message
     * @param cause the underlying cause
     * @return a new KmipTransportException
     */
    public static KmipTransportException decodingError(String message, Throwable cause) {
        return new KmipTransportException(TransportErrorType.DECODING_ERROR, message, cause);
    }
}
