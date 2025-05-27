package com.kmip.server.core.exception;

// Import the global KMIP result reason enum
import com.kmip.server.core.enums.KmipResultReason;

public class KmipException extends Exception {

    private final KmipResultReason resultReason;

    // Constructors
    public KmipException(String message) {
        super(message);
        this.resultReason = null; // Default if no reason specified
    }

    public KmipException(String message, KmipResultReason reason) {
        super(message);
        this.resultReason = reason;
    }

    public KmipException(String message, Throwable cause) {
        super(message, cause);
        this.resultReason = null; // Default if no reason specified
    }

    public KmipException(String message, Throwable cause, KmipResultReason reason) {
        super(message, cause);
        this.resultReason = reason;
    }

    /**
     * Gets the KMIP Result Reason associated with this exception, if any.
     *
     * @return The KmipResultReason enum, or null if none was set.
     */
    public KmipResultReason getResultReason() {
        return resultReason;
    }
}