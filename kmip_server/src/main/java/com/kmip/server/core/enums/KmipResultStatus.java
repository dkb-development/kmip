package com.kmip.server.core.enums;

/**
 * KMIP Result Status Enumeration.
 * 
 * This enum defines all possible result status values for KMIP operations
 * as specified in the KMIP 2.0 specification. The result status indicates
 * whether an operation completed successfully or failed.
 * 
 * Reference: KMIP 2.0 Specification, Table 407 (Result Status Enumeration)
 */
public enum KmipResultStatus {
    
    /**
     * Success - The operation completed successfully.
     */
    SUCCESS(0x000000, "Success"),
    
    /**
     * Operation Failed - The operation failed for an unspecified reason.
     */
    OPERATION_FAILED(0x000001, "Operation Failed"),
    
    /**
     * Operation Pending - The operation is pending and has not yet completed.
     */
    OPERATION_PENDING(0x000002, "Operation Pending"),
    
    /**
     * Operation Undone - The operation was undone (rolled back).
     */
    OPERATION_UNDONE(0x000003, "Operation Undone");
    
    private final int code;
    private final String displayName;
    
    /**
     * Constructor for KMIP Result Status enum.
     * 
     * @param code the status code
     * @param displayName the human-readable name
     */
    KmipResultStatus(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    /**
     * Gets the status code.
     * 
     * @return the status code
     */
    public int getCode() {
        return code;
    }
    
    /**
     * Gets the human-readable display name.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the status code as hexadecimal string.
     * 
     * @return the status code in hex format
     */
    public String getHexCode() {
        return String.format("0x%06X", code);
    }
    
    /**
     * Finds the result status by its code.
     * 
     * @param code the status code to look up
     * @return the corresponding KmipResultStatus, or null if not found
     */
    public static KmipResultStatus fromCode(int code) {
        for (KmipResultStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * Checks if this status indicates success.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    /**
     * Checks if this status indicates failure.
     * 
     * @return true if failed, false otherwise
     */
    public boolean isFailure() {
        return this == OPERATION_FAILED;
    }
    
    /**
     * Checks if this status indicates a pending operation.
     * 
     * @return true if pending, false otherwise
     */
    public boolean isPending() {
        return this == OPERATION_PENDING;
    }
    
    /**
     * Checks if this status indicates an undone operation.
     * 
     * @return true if undone, false otherwise
     */
    public boolean isUndone() {
        return this == OPERATION_UNDONE;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, getHexCode());
    }
}
