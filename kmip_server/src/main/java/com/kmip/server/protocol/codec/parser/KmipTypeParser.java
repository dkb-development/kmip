package com.kmip.server.protocol.codec.parser;

import java.nio.ByteBuffer;
import com.kmip.server.core.exception.KmipParseException;

/**
 * Interface for parsing specific KMIP data types.
 * 
 * This interface defines the contract for parsing different KMIP data types
 * from binary data. Each KMIP type (Structure, Integer, Enumeration, etc.)
 * should have its own implementation of this interface.
 * 
 * This design allows for:
 * - Easy extension of new KMIP types
 * - Type-specific parsing logic
 * - Better separation of concerns
 * - Testability of individual type parsers
 */
public interface KmipTypeParser {
    
    /**
     * Gets the KMIP type code that this parser handles.
     * 
     * @return the KMIP type code (e.g., 0x01 for Structure, 0x02 for Integer)
     */
    byte getTypeCode();
    
    /**
     * Gets a human-readable name for this type.
     * 
     * @return the type name (e.g., "Structure", "Integer", "Enumeration")
     */
    String getTypeName();
    
    /**
     * Parses a value of this type from the given buffer.
     * 
     * @param valueBuffer the buffer containing the value data
     * @param valueLength the length of the value in bytes
     * @param tagString the tag string for context and error reporting
     * @param context the parsing context for additional information
     * @return the parsed value as an appropriate Java object
     * @throws KmipParseException if the value cannot be parsed
     */
    Object parseValue(ByteBuffer valueBuffer, int valueLength, String tagString, ParseContext context) 
            throws KmipParseException;
    
    /**
     * Validates that the given value length is valid for this type.
     * 
     * @param valueLength the length of the value in bytes
     * @param tagString the tag string for error reporting
     * @throws KmipParseException if the length is invalid
     */
    void validateLength(int valueLength, String tagString) throws KmipParseException;
    
    /**
     * Checks if this parser can handle empty values (zero length).
     * 
     * @return true if empty values are allowed, false otherwise
     */
    boolean allowsEmptyValue();
    
    /**
     * Gets the expected minimum length for this type.
     * 
     * @return the minimum length in bytes, or -1 if variable length
     */
    int getMinimumLength();
    
    /**
     * Gets the expected maximum length for this type.
     * 
     * @return the maximum length in bytes, or -1 if no maximum
     */
    int getMaximumLength();
    
    /**
     * Checks if this type requires specific alignment.
     * 
     * @return the required alignment in bytes, or 1 if no specific alignment
     */
    int getRequiredAlignment();
    
    /**
     * Context information passed to type parsers.
     * This provides additional information that may be needed during parsing.
     */
    interface ParseContext {
        
        /**
         * Gets the current nesting depth.
         * 
         * @return the current nesting depth
         */
        int getNestingDepth();
        
        /**
         * Gets the current position in the buffer.
         * 
         * @return the current position
         */
        int getCurrentPosition();
        
        /**
         * Gets the parser configuration.
         * 
         * @return the parser configuration
         */
        Object getParserConfig();
        
        /**
         * Checks if detailed logging is enabled.
         * 
         * @return true if detailed logging is enabled
         */
        boolean isDetailedLoggingEnabled();
        
        /**
         * Creates a child context for nested parsing.
         * 
         * @return a new context for nested parsing
         */
        ParseContext createChildContext();
        
        /**
         * Gets a type parser for the specified type.
         * 
         * @param typeCode the KMIP type code
         * @return the type parser, or null if not found
         */
        KmipTypeParser getTypeParser(byte typeCode);
    }
}
