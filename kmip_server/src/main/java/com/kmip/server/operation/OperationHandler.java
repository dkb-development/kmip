package com.kmip.server.operation;

import com.kmip.server.core.exception.KmipException;
import com.kmip.server.protocol.message.KmipMessage;

/**
 * Interface for handling KMIP operations.
 *
 * Implementations of this interface handle specific KMIP operations like Create, Get, etc.
 */
public interface OperationHandler {
    /**
     * Handles a KMIP operation.
     *
     * @param request The KMIP request message
     * @return The KMIP response message
     * @throws KmipException if an error occurs during handling
     */
    KmipMessage handle(KmipMessage request) throws KmipException;

    /**
     * Gets the operation code that this handler can handle.
     *
     * @return The operation code
     */
    int getOperationCode();
}
