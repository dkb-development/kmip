package com.kmip.server.core.exception;

import com.kmip.server.protocol.tag.TagValueUtil;

/**
 * Exception thrown when a KMIP operation fails.
 */
public class KmipException extends RuntimeException {
    private final int resultReason;

    /**
     * Creates a new KmipException with the specified message.
     *
     * @param message The error message
     */
    public KmipException(String message) {
        super(message);
        this.resultReason = 0; // Default reason
    }

    /**
     * Creates a new KmipException with the specified message and result reason.
     *
     * @param message The error message
     * @param resultReason The result reason code
     */
    public KmipException(String message, int resultReason) {
        super(message);
        this.resultReason = resultReason;
    }

    /**
     * Creates a new KmipException with the specified message and cause.
     *
     * @param message The error message
     * @param cause The cause of the exception
     */
    public KmipException(String message, Throwable cause) {
        super(message, cause);
        this.resultReason = 0; // Default reason
    }

    /**
     * Creates a new KmipException with the specified message, cause, and result reason.
     *
     * @param message The error message
     * @param cause The cause of the exception
     * @param resultReason The result reason code
     */
    public KmipException(String message, Throwable cause, int resultReason) {
        super(message, cause);
        this.resultReason = resultReason;
    }

    /**
     * Gets the result reason code.
     *
     * @return The result reason code
     */
    public int getResultReason() {
        return resultReason;
    }

    /**
     * Gets the result reason name.
     *
     * @return The result reason name
     */
    public String getResultReasonName() {
        return TagValueUtil.getResultReasonName(resultReason);
    }
}
