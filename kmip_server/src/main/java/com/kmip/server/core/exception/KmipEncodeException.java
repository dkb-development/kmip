package com.kmip.server.core.exception;

/**
 * Exception thrown when there are KMIP message encoding errors.
 * 
 * This exception represents problems with encoding KMIP messages to binary data,
 * including malformed structures, invalid data types, buffer overflow,
 * or other encoding-related issues.
 * 
 * This is part of the codec layer and should be used for low-level encoding errors.
 */
public class KmipEncodeException extends KmipException {
    
    private static final long serialVersionUID = 1L;
    
    private final EncodeErrorType errorType;
    private final String tagString;
    private final int position;
    private final Object invalidValue;
    
    /**
     * Enumeration of encode error types for better categorization.
     */
    public enum EncodeErrorType {
        BUFFER_OVERFLOW("Buffer Overflow"),
        INVALID_STRUCTURE("Invalid Structure"),
        INVALID_TAG("Invalid Tag"),
        INVALID_TYPE("Invalid Type"),
        INVALID_VALUE("Invalid Value"),
        UNSUPPORTED_TYPE("Unsupported Type"),
        STRUCTURE_TOO_DEEP("Structure Too Deep"),
        TOO_MANY_FIELDS("Too Many Fields"),
        FIELD_ORDERING_VIOLATION("Field Ordering Violation"),
        ENCODE_TIMEOUT("Encode Timeout"),
        IO_ERROR("I/O Error"),
        UNKNOWN("Unknown Encode Error");
        
        private final String description;
        
        EncodeErrorType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Constructs a new KmipEncodeException with the specified detail message.
     * 
     * @param message the detail message
     */
    public KmipEncodeException(String message) {
        super(message);
        this.errorType = EncodeErrorType.UNKNOWN;
        this.tagString = null;
        this.position = -1;
        this.invalidValue = null;
    }
    
    /**
     * Constructs a new KmipEncodeException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public KmipEncodeException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = EncodeErrorType.UNKNOWN;
        this.tagString = null;
        this.position = -1;
        this.invalidValue = null;
    }
    
    /**
     * Constructs a new KmipEncodeException with error type and context.
     * 
     * @param errorType the type of encode error
     * @param message the detail message
     */
    public KmipEncodeException(EncodeErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.tagString = null;
        this.position = -1;
        this.invalidValue = null;
    }
    
    /**
     * Constructs a new KmipEncodeException with full context information.
     * 
     * @param errorType the type of encode error
     * @param message the detail message
     * @param tagString the tag being encoded when error occurred
     * @param position the position in the buffer where error occurred
     */
    public KmipEncodeException(EncodeErrorType errorType, String message, String tagString, int position) {
        super(message);
        this.errorType = errorType;
        this.tagString = tagString;
        this.position = position;
        this.invalidValue = null;
    }
    
    /**
     * Constructs a new KmipEncodeException with full context and invalid value.
     * 
     * @param errorType the type of encode error
     * @param message the detail message
     * @param tagString the tag being encoded when error occurred
     * @param position the position in the buffer where error occurred
     * @param invalidValue the value that caused the error
     */
    public KmipEncodeException(EncodeErrorType errorType, String message, String tagString, 
                             int position, Object invalidValue) {
        super(message);
        this.errorType = errorType;
        this.tagString = tagString;
        this.position = position;
        this.invalidValue = invalidValue;
    }
    
    /**
     * Gets the encode error type.
     * 
     * @return the error type
     */
    public EncodeErrorType getErrorType() {
        return errorType;
    }
    
    /**
     * Gets the tag string where the error occurred.
     * 
     * @return the tag string, or null if not available
     */
    public String getTagString() {
        return tagString;
    }
    
    /**
     * Gets the position in the buffer where the error occurred.
     * 
     * @return the position, or -1 if not available
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * Gets the invalid value that caused the error.
     * 
     * @return the invalid value, or null if not available
     */
    public Object getInvalidValue() {
        return invalidValue;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorType.getDescription()).append("] ");
        sb.append(super.getMessage());
        
        if (tagString != null) {
            sb.append(" (Tag: ").append(tagString).append(")");
        }
        
        if (position >= 0) {
            sb.append(" (Position: ").append(position).append(")");
        }
        
        if (invalidValue != null) {
            sb.append(" (Value: ").append(invalidValue.toString()).append(")");
        }
        
        return sb.toString();
    }
    
    /**
     * Creates an encode exception for buffer overflow.
     * 
     * @param currentSize the current buffer size
     * @param maxSize the maximum allowed size
     * @param tagString the tag being encoded
     * @return a new KmipEncodeException
     */
    public static KmipEncodeException bufferOverflow(int currentSize, int maxSize, String tagString) {
        return new KmipEncodeException(
            EncodeErrorType.BUFFER_OVERFLOW,
            String.format("Buffer overflow: current size %d bytes exceeds maximum %d bytes", 
                    currentSize, maxSize),
            tagString,
            -1
        );
    }
    
    /**
     * Creates an encode exception for invalid structure.
     * 
     * @param message the error message
     * @param tagString the tag being encoded
     * @return a new KmipEncodeException
     */
    public static KmipEncodeException invalidStructure(String message, String tagString) {
        return new KmipEncodeException(
            EncodeErrorType.INVALID_STRUCTURE,
            message,
            tagString,
            -1
        );
    }
    
    /**
     * Creates an encode exception for unsupported type.
     * 
     * @param value the unsupported value
     * @param tagString the tag being encoded
     * @return a new KmipEncodeException
     */
    public static KmipEncodeException unsupportedType(Object value, String tagString) {
        return new KmipEncodeException(
            EncodeErrorType.UNSUPPORTED_TYPE,
            String.format("Unsupported value type: %s", value.getClass().getName()),
            tagString,
            -1,
            value
        );
    }
    
    /**
     * Creates an encode exception for field ordering violation.
     * 
     * @param expectedTag the expected tag
     * @param actualTag the actual tag
     * @param structureType the structure type
     * @return a new KmipEncodeException
     */
    public static KmipEncodeException fieldOrderingViolation(String expectedTag, String actualTag, 
                                                           String structureType) {
        return new KmipEncodeException(
            EncodeErrorType.FIELD_ORDERING_VIOLATION,
            String.format("Field ordering violation in %s: expected %s, found %s", 
                    structureType, expectedTag, actualTag),
            actualTag,
            -1
        );
    }
}
