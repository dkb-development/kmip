package com.kmip.server.core.enums;

/**
 * KMIP TTLV Type Enumeration.
 * 
 * This enum defines all KMIP TTLV (Tag-Type-Length-Value) type codes
 * as specified in the KMIP 2.0 specification. These types define the
 * data format for values in KMIP messages.
 * 
 * Reference: KMIP 2.0 Specification, Section 9.1 (TTLV Encoding)
 */
public enum KmipTtlvType {
    
    /**
     * Structure type - Contains nested TTLV items.
     * Used for complex objects and message structures.
     */
    STRUCTURE(0x01, "Structure"),
    
    /**
     * Integer type - 32-bit signed integer.
     * Used for numeric values, counts, and simple identifiers.
     */
    INTEGER(0x02, "Integer"),
    
    /**
     * Long Integer type - 64-bit signed integer.
     * Used for large numeric values and timestamps.
     */
    LONG_INTEGER(0x03, "Long Integer"),
    
    /**
     * Big Integer type - Variable length signed integer.
     * Used for very large numeric values that exceed 64-bit range.
     */
    BIG_INTEGER(0x04, "Big Integer"),
    
    /**
     * Enumeration type - 32-bit enumeration value.
     * Used for predefined constant values like operation types, algorithms, etc.
     */
    ENUMERATION(0x05, "Enumeration"),
    
    /**
     * Boolean type - Boolean value (true/false).
     * Encoded as 8 bytes with the last byte containing 0x00 (false) or 0x01 (true).
     */
    BOOLEAN(0x06, "Boolean"),
    
    /**
     * Text String type - UTF-8 encoded text string.
     * Used for human-readable text values.
     */
    TEXT_STRING(0x07, "Text String"),
    
    /**
     * Byte String type - Binary data.
     * Used for cryptographic keys, certificates, and other binary data.
     */
    BYTE_STRING(0x08, "Byte String"),
    
    /**
     * Date-Time type - Date and time value.
     * Encoded as 64-bit integer representing seconds since Unix epoch.
     */
    DATE_TIME(0x09, "Date-Time"),
    
    /**
     * Interval type - Time interval value.
     * Encoded as 32-bit integer representing seconds.
     */
    INTERVAL(0x0A, "Interval");
    
    private final byte code;
    private final String displayName;
    
    /**
     * Constructor for KMIP TTLV Type enum.
     * 
     * @param code the TTLV type code
     * @param displayName the human-readable name
     */
    KmipTtlvType(int code, String displayName) {
        this.code = (byte) code;
        this.displayName = displayName;
    }
    
    /**
     * Gets the TTLV type code.
     * 
     * @return the type code as byte
     */
    public byte getCode() {
        return code;
    }
    
    /**
     * Gets the TTLV type code as integer.
     * 
     * @return the type code as integer
     */
    public int getCodeAsInt() {
        return code & 0xFF;
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
     * Gets the type code as hexadecimal string.
     * 
     * @return the type code in hex format (e.g., "0x01")
     */
    public String getHexCode() {
        return String.format("0x%02X", code);
    }
    
    /**
     * Finds the TTLV type by its code.
     * 
     * @param code the type code to look up
     * @return the corresponding KmipTtlvType, or null if not found
     */
    public static KmipTtlvType fromCode(byte code) {
        for (KmipTtlvType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Finds the TTLV type by its integer code.
     * 
     * @param code the type code to look up
     * @return the corresponding KmipTtlvType, or null if not found
     */
    public static KmipTtlvType fromCode(int code) {
        return fromCode((byte) code);
    }
    
    /**
     * Checks if the given code represents a valid TTLV type.
     * 
     * @param code the type code to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidType(byte code) {
        return fromCode(code) != null;
    }
    
    /**
     * Checks if this type can contain nested structures.
     * 
     * @return true if this type can contain nested data
     */
    public boolean canContainNestedData() {
        return this == STRUCTURE;
    }
    
    /**
     * Checks if this type represents a numeric value.
     * 
     * @return true if this is a numeric type
     */
    public boolean isNumericType() {
        return this == INTEGER || this == LONG_INTEGER || this == BIG_INTEGER || this == ENUMERATION;
    }
    
    /**
     * Checks if this type represents textual data.
     * 
     * @return true if this is a text type
     */
    public boolean isTextType() {
        return this == TEXT_STRING;
    }
    
    /**
     * Checks if this type represents binary data.
     * 
     * @return true if this is a binary type
     */
    public boolean isBinaryType() {
        return this == BYTE_STRING;
    }
    
    /**
     * Checks if this type represents temporal data.
     * 
     * @return true if this is a time-related type
     */
    public boolean isTemporalType() {
        return this == DATE_TIME || this == INTERVAL;
    }
    
    /**
     * Gets the expected minimum length for this type in bytes.
     * 
     * @return minimum length, or -1 if variable length
     */
    public int getMinimumLength() {
        switch (this) {
            case INTEGER:
            case ENUMERATION:
            case INTERVAL:
                return 4;
            case LONG_INTEGER:
            case DATE_TIME:
            case BOOLEAN:
                return 8;
            case STRUCTURE:
            case TEXT_STRING:
            case BYTE_STRING:
            case BIG_INTEGER:
                return 0; // Variable length
            default:
                return -1;
        }
    }
    
    /**
     * Gets the expected alignment for this type in bytes.
     * All KMIP types require 8-byte alignment.
     * 
     * @return alignment requirement in bytes
     */
    public int getRequiredAlignment() {
        return 8; // All KMIP types require 8-byte alignment
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, getHexCode());
    }
}
