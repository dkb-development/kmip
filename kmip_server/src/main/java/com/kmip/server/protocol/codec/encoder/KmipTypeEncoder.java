package com.kmip.server.protocol.codec.encoder;

import java.io.DataOutputStream;
import java.io.IOException;
import com.kmip.server.core.exception.KmipEncodeException;

/**
 * Interface for encoding specific KMIP data types.
 * 
 * This interface defines the contract for encoding different KMIP data types
 * to binary data. Each KMIP type (Structure, Integer, Enumeration, etc.)
 * should have its own implementation of this interface.
 * 
 * This design allows for:
 * - Easy extension of new KMIP types
 * - Type-specific encoding logic
 * - Better separation of concerns
 * - Testability of individual type encoders
 */
public interface KmipTypeEncoder {
    
    /**
     * Gets the KMIP type code that this encoder handles.
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
     * Checks if this encoder can handle the given value type.
     * 
     * @param value the value to check
     * @return true if this encoder can handle the value
     */
    boolean canEncode(Object value);
    
    /**
     * Encodes a value of this type to the given output stream.
     * 
     * @param dos the output stream to write to
     * @param tag the KMIP tag for this field
     * @param value the value to encode
     * @param context the encoding context for additional information
     * @throws KmipEncodeException if the value cannot be encoded
     * @throws IOException if an I/O error occurs
     */
    void encodeValue(DataOutputStream dos, int tag, Object value, EncodeContext context) 
            throws KmipEncodeException, IOException;
    
    /**
     * Validates that the given value can be encoded by this encoder.
     * 
     * @param value the value to validate
     * @param tagString the tag string for error reporting
     * @throws KmipEncodeException if the value is invalid
     */
    void validateValue(Object value, String tagString) throws KmipEncodeException;
    
    /**
     * Gets the expected encoded length for this value (if deterministic).
     * 
     * @param value the value to calculate length for
     * @return the encoded length in bytes, or -1 if variable/unknown
     */
    int getEncodedLength(Object value);
    
    /**
     * Checks if this type requires specific alignment.
     * 
     * @return the required alignment in bytes, or 1 if no specific alignment
     */
    int getRequiredAlignment();
    
    /**
     * Checks if this encoder supports null values.
     * 
     * @return true if null values are allowed, false otherwise
     */
    boolean allowsNullValue();
    
    /**
     * Context information passed to type encoders.
     * This provides additional information that may be needed during encoding.
     */
    interface EncodeContext {
        
        /**
         * Gets the current nesting depth.
         * 
         * @return the current nesting depth
         */
        int getNestingDepth();
        
        /**
         * Gets the current position in the output stream.
         * 
         * @return the current position
         */
        int getCurrentPosition();
        
        /**
         * Gets the encoder configuration.
         * 
         * @return the encoder configuration
         */
        Object getEncoderConfig();
        
        /**
         * Checks if detailed logging is enabled.
         * 
         * @return true if detailed logging is enabled
         */
        boolean isDetailedLoggingEnabled();
        
        /**
         * Checks if hex dump logging is enabled.
         * 
         * @return true if hex dump logging is enabled
         */
        boolean isHexDumpLoggingEnabled();
        
        /**
         * Creates a child context for nested encoding.
         * 
         * @return a new context for nested encoding
         */
        EncodeContext createChildContext();
        
        /**
         * Gets a type encoder for the specified type.
         * 
         * @param typeCode the KMIP type code
         * @return the type encoder, or null if not found
         */
        KmipTypeEncoder getTypeEncoder(byte typeCode);
        
        /**
         * Gets a type encoder for the specified value.
         * 
         * @param value the value to encode
         * @return the type encoder, or null if not found
         */
        KmipTypeEncoder getTypeEncoderForValue(Object value);
        
        /**
         * Writes TTLV header (Tag-Type-Length) to the output stream.
         * 
         * @param dos the output stream
         * @param tag the KMIP tag
         * @param type the KMIP type
         * @param length the value length
         * @throws IOException if an I/O error occurs
         */
        void writeTagTypeLength(DataOutputStream dos, int tag, byte type, int length) throws IOException;
        
        /**
         * Writes padding bytes to ensure proper alignment.
         * 
         * @param dos the output stream
         * @param length the length that needs padding
         * @throws IOException if an I/O error occurs
         */
        void writePadding(DataOutputStream dos, int length) throws IOException;
        
        /**
         * Validates that the current buffer size doesn't exceed limits.
         * 
         * @param additionalBytes the number of bytes to be added
         * @param tagString the tag for error reporting
         * @throws KmipEncodeException if size limit would be exceeded
         */
        void validateBufferSize(int additionalBytes, String tagString) throws KmipEncodeException;
        
        /**
         * Records encoding statistics for monitoring.
         * 
         * @param typeCode the type that was encoded
         * @param encodeTimeMs the time taken to encode
         * @param encodedBytes the number of bytes encoded
         */
        void recordEncodeSuccess(byte typeCode, long encodeTimeMs, int encodedBytes);
        
        /**
         * Records encoding error for monitoring.
         * 
         * @param typeCode the type that failed to encode
         * @param errorMessage the error message
         */
        void recordEncodeError(byte typeCode, String errorMessage);
    }
}
