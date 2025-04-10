package com.kmip.server.core.exception;

// Import the ResultReason enum defined in KmipRequestHandler
import com.kmip.server.operation.KmipRequestHandler.ResultReason;

public class KmipException extends Exception {

    private final ResultReason resultReason;

    // Constructors
    public KmipException(String message) {
        super(message);
        this.resultReason = null; // Default if no reason specified
    }

    public KmipException(String message, ResultReason reason) {
        super(message);
        this.resultReason = reason;
    }

    public KmipException(String message, Throwable cause) {
        super(message, cause);
        this.resultReason = null; // Default if no reason specified
    }
    
    public KmipException(String message, Throwable cause, ResultReason reason) {
        super(message, cause);
        this.resultReason = reason;
    }

    /**
     * Gets the KMIP Result Reason associated with this exception, if any.
     * 
     * @return The ResultReason enum, or null if none was set.
     */
    public ResultReason getResultReason() {
        return resultReason;
    }
} 