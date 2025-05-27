package com.kmip.server.protocol.handler;

import com.kmip.server.core.exception.KmipProtocolException;
import com.kmip.server.core.exception.KmipTransportException;
import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.transport.tcp.model.ClientConnection;

/**
 * Interface for handling KMIP protocol operations.
 * 
 * This interface defines the contract between the transport layer (Part-1)
 * and the business logic layer (Part-2). The transport layer handles
 * network communication, message parsing/encoding, while the protocol
 * handler implements the actual KMIP operation logic.
 * 
 * This separation allows:
 * - Independent development of protocol and business logic
 * - Easy testing of business logic without network concerns
 * - Protocol version upgrades without affecting business logic
 * - Multiple transport implementations (TCP, HTTP, etc.)
 */
public interface KmipProtocolHandler {
    
    /**
     * Processes a KMIP request message and returns a response message.
     * 
     * This is the main entry point for processing KMIP operations.
     * The transport layer calls this method after successfully parsing
     * the incoming message.
     * 
     * @param requestMessage the parsed KMIP request message
     * @param clientConnection the client connection information
     * @return the KMIP response message to be sent back to the client
     * @throws KmipProtocolException if there's a protocol-level error
     */
    KmipMessage processRequest(KmipMessage requestMessage, ClientConnection clientConnection) 
            throws KmipProtocolException;
    
    /**
     * Validates a KMIP request message for protocol compliance.
     * 
     * This method performs protocol-level validation such as:
     * - Required fields presence
     * - Field value validity
     * - Message structure compliance
     * - Protocol version compatibility
     * 
     * @param requestMessage the request message to validate
     * @throws KmipProtocolException if the message is invalid
     */
    void validateRequest(KmipMessage requestMessage) throws KmipProtocolException;
    
    /**
     * Gets the supported KMIP protocol versions.
     * 
     * @return array of supported protocol version strings
     */
    String[] getSupportedProtocolVersions();
    
    /**
     * Checks if a specific operation is supported.
     * 
     * @param operationCode the KMIP operation code
     * @return true if the operation is supported, false otherwise
     */
    boolean isOperationSupported(int operationCode);
    
    /**
     * Gets the maximum message size that can be processed.
     * 
     * @return maximum message size in bytes
     */
    int getMaxMessageSize();
    
    /**
     * Handles protocol-level errors and creates appropriate error responses.
     * 
     * @param exception the protocol exception that occurred
     * @param originalRequest the original request that caused the error (may be null)
     * @param clientConnection the client connection information
     * @return an error response message
     */
    KmipMessage handleProtocolError(KmipProtocolException exception, 
                                   KmipMessage originalRequest, 
                                   ClientConnection clientConnection);
    
    /**
     * Performs any necessary cleanup when a client connection is closed.
     * 
     * @param clientConnection the client connection that was closed
     */
    void onConnectionClosed(ClientConnection clientConnection);
    
    /**
     * Performs any necessary initialization when a new client connects.
     * 
     * @param clientConnection the new client connection
     */
    void onConnectionEstablished(ClientConnection clientConnection);
    
    /**
     * Gets protocol handler statistics and metrics.
     * 
     * @return a map of statistics and metrics
     */
    java.util.Map<String, Object> getStatistics();
}
