package com.kmip.server.core.exception;

/**
 * Exception thrown when there are KMIP message parsing errors.
 * 
 * This exception represents problems with parsing KMIP messages from binary data,
 * including malformed TTLV structures, invalid data types, insufficient data,
 * or other parsing-related issues.
 * 
 * This is part of the codec layer and should be used for low-level parsing errors.
 */
public class KmipParseException extends KmipException {
    
    private static final long serialVersionUID = 1L;
    
    private final ParseErrorType errorType;
    private final String tagString;
    private final int position;
    private final byte[] rawData;
    
    /**
     * Enumeration of parse error types for better categorization.
     */
    public enum ParseErrorType {
        INSUFFICIENT_DATA("Insufficient Data"),
        INVALID_TTLV_HEADER("Invalid TTLV Header"),
        INVALID_TAG("Invalid Tag"),
        INVALID_TYPE("Invalid Type"),
        INVALID_LENGTH("Invalid Length"),
        INVALID_VALUE("Invalid Value"),
        INVALID_PADDING("Invalid Padding"),
        BUFFER_OVERFLOW("Buffer Overflow"),
        UNSUPPORTED_TYPE("Unsupported Type"),
        STRUCTURE_TOO_DEEP("Structure Too Deep"),
        TOO_MANY_FIELDS("Too Many Fields"),
        PARSE_TIMEOUT("Parse Timeout"),
        UNKNOWN("Unknown Parse Error");
        
        private final String description;
        
        ParseErrorType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Constructs a new KmipParseException with the specified detail message.
     * 
     * @param message the detail message
     */
    public KmipParseException(String message) {
        super(message);
        this.errorType = ParseErrorType.UNKNOWN;
        this.tagString = null;
        this.position = -1;
        this.rawData = null;
    }
    
    /**
     * Constructs a new KmipParseException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public KmipParseException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ParseErrorType.UNKNOWN;
        this.tagString = null;
        this.position = -1;
        this.rawData = null;
    }
    
    /**
     * Constructs a new KmipParseException with error type and context.
     * 
     * @param errorType the type of parse error
     * @param message the detail message
     */
    public KmipParseException(ParseErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.tagString = null;
        this.position = -1;
        this.rawData = null;
    }
    
    /**
     * Constructs a new KmipParseException with full context information.
     * 
     * @param errorType the type of parse error
     * @param message the detail message
     * @param tagString the tag being parsed when error occurred
     * @param position the position in the buffer where error occurred
     */
    public KmipParseException(ParseErrorType errorType, String message, String tagString, int position) {
        super(message);
        this.errorType = errorType;
        this.tagString = tagString;
        this.position = position;
        this.rawData = null;
    }
    
    /**
     * Constructs a new KmipParseException with full context and raw data.
     * 
     * @param errorType the type of parse error
     * @param message the detail message
     * @param tagString the tag being parsed when error occurred
     * @param position the position in the buffer where error occurred
     * @param rawData the raw data that caused the error
     */
    public KmipParseException(ParseErrorType errorType, String message, String tagString, 
                             int position, byte[] rawData) {
        super(message);
        this.errorType = errorType;
        this.tagString = tagString;
        this.position = position;
        this.rawData = rawData != null ? rawData.clone() : null;
    }
    
    /**
     * Gets the parse error type.
     * 
     * @return the error type
     */
    public ParseErrorType getErrorType() {
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
     * Gets the raw data that caused the error.
     * 
     * @return a copy of the raw data, or null if not available
     */
    public byte[] getRawData() {
        return rawData != null ? rawData.clone() : null;
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
        
        if (rawData != null) {
            sb.append(" (Data length: ").append(rawData.length).append(" bytes)");
        }
        
        return sb.toString();
    }
    
    /**
     * Creates a parse exception for insufficient data.
     * 
     * @param required the number of bytes required
     * @param available the number of bytes available
     * @param tagString the tag being parsed
     * @return a new KmipParseException
     */
    public static KmipParseException insufficientData(int required, int available, String tagString) {
        return new KmipParseException(
            ParseErrorType.INSUFFICIENT_DATA,
            String.format("Insufficient data: required %d bytes, available %d bytes", required, available),
            tagString,
            -1
        );
    }
    
    /**
     * Creates a parse exception for invalid TTLV header.
     * 
     * @param message the error message
     * @param position the position where the error occurred
     * @return a new KmipParseException
     */
    public static KmipParseException invalidTtlvHeader(String message, int position) {
        return new KmipParseException(
            ParseErrorType.INVALID_TTLV_HEADER,
            message,
            null,
            position
        );
    }
    
    /**
     * Creates a parse exception for unsupported type.
     * 
     * @param type the unsupported type code
     * @param tagString the tag being parsed
     * @return a new KmipParseException
     */
    public static KmipParseException unsupportedType(byte type, String tagString) {
        return new KmipParseException(
            ParseErrorType.UNSUPPORTED_TYPE,
            String.format("Unsupported KMIP type: 0x%02X", type),
            tagString,
            -1
        );
    }
}
