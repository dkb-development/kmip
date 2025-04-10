package com.kmip.server.service;

import com.kmip.server.core.exception.KmipException;
import com.kmip.server.protocol.message.KmipMessage;

/**
 * Interface for handling specific KMIP operations.
 */
public interface OperationHandler {

    /**
     * Handles a specific KMIP request operation.
     * 
     * @param requestHeader The request header structure.
     * @param requestPayload The request payload structure specific to the operation.
     * @return A KmipMessage representing the response payload (or null if no payload needed).
     * @throws KmipException if an error occurs during handling.
     */
    KmipMessage handle(KmipMessage requestHeader, KmipMessage requestPayload) throws KmipException;
    
    /**
     * Gets the KMIP Operation code this handler is responsible for.
     * 
     * @return The operation enumeration value.
     */
    // We'll use integer codes for now, could be an Enum later
    int getOperationCode(); 
}