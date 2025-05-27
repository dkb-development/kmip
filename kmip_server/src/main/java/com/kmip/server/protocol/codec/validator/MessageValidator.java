package com.kmip.server.protocol.codec.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.TagValueUtil;
import com.kmip.server.core.exception.KmipParseException;
import com.kmip.server.protocol.codec.config.ParserConfig;

import java.nio.ByteBuffer;

/**
 * Validator for KMIP messages and structures.
 * 
 * This class provides validation logic for KMIP messages at the protocol level.
 * It ensures that parsed messages conform to the KMIP specification requirements
 * and can detect various protocol violations.
 */
@Component
public class MessageValidator {
    
    private static final Logger log = LoggerFactory.getLogger(MessageValidator.class);
    
    // KMIP TTLV Type constants
    private static final byte TYPE_STRUCTURE = 0x01;
    
    /**
     * Validates a top-level KMIP message.
     * 
     * @param data the raw message data
     * @param length the length of the data
     * @param config the parser configuration
     * @throws KmipParseException if validation fails
     */
    public void validateTopLevelMessage(byte[] data, int length, ParserConfig config) 
            throws KmipParseException {
        
        if (data == null) {
            throw new KmipParseException(
                KmipParseException.ParseErrorType.INVALID_TTLV_HEADER,
                "Message data cannot be null"
            );
        }
        
        if (length <= 0) {
            throw new KmipParseException(
                KmipParseException.ParseErrorType.INVALID_LENGTH,
                "Message length must be positive, got: " + length
            );
        }
        
        if (length > config.getMaxMessageSizeBytes()) {
            throw new KmipParseException(
                KmipParseException.ParseErrorType.BUFFER_OVERFLOW,
                String.format("Message size %d exceeds maximum allowed size %d", 
                        length, config.getMaxMessageSizeBytes())
            );
        }
        
        if (length < 8) {
            throw KmipParseException.insufficientData(8, length, "TOP_LEVEL");
        }
        
        log.debug("Top-level message validation passed for {} bytes", length);
    }
    
    /**
     * Validates a TTLV header.
     * 
     * @param buffer the buffer containing the header
     * @param config the parser configuration
     * @return the parsed header information
     * @throws KmipParseException if validation fails
     */
    public TtlvHeader validateTtlvHeader(ByteBuffer buffer, ParserConfig config) 
            throws KmipParseException {
        
        if (buffer.remaining() < 8) {
            throw KmipParseException.insufficientData(8, buffer.remaining(), "TTLV_HEADER");
        }
        
        int startPosition = buffer.position();
        
        // Read Tag (3 bytes)
        byte[] tagBytes = new byte[3];
        buffer.get(tagBytes);
        String tagString = bytesToHex(tagBytes);
        
        // Read Type (1 byte)
        byte type = buffer.get();
        
        // Read Length (4 bytes)
        int valueLength = buffer.getInt();
        
        // Reset position for caller
        buffer.position(startPosition);
        
        // Validate tag
        if (config.isStrictTagValidation()) {
            validateTag(tagString);
        }
        
        // Validate type
        validateType(type, config);
        
        // Validate length
        validateLength(valueLength, tagString, config);
        
        log.debug("TTLV header validation passed: Tag={}, Type=0x{}, Length={}", 
                tagString, String.format("%02X", type), valueLength);
        
        return new TtlvHeader(tagString, type, valueLength);
    }
    
    /**
     * Validates message structure requirements.
     * 
     * @param tagString the message tag
     * @param type the message type
     * @throws KmipParseException if validation fails
     */
    public void validateMessageStructure(String tagString, byte type) throws KmipParseException {
        boolean isRequestMessage = TagValueUtil.TAG_REQUEST_MESSAGE.equals(tagString);
        boolean isResponseMessage = TagValueUtil.TAG_RESPONSE_MESSAGE.equals(tagString);
        
        if (!isRequestMessage && !isResponseMessage) {
            throw new KmipParseException(
                KmipParseException.ParseErrorType.INVALID_TAG,
                "Expected Request or Response Message tag, found: " + tagString
            );
        }
        
        if (type != TYPE_STRUCTURE) {
            throw new KmipParseException(
                KmipParseException.ParseErrorType.INVALID_TYPE,
                String.format("Expected Structure type (0x01) for message, found: 0x%02X", type)
            );
        }
        
        log.debug("Message structure validation passed for {}", 
                isRequestMessage ? "Request" : "Response");
    }
    
    /**
     * Validates padding bytes according to KMIP specification.
     * 
     * @param buffer the buffer containing padding
     * @param paddingLength the expected padding length
     * @param tagString the tag for error reporting
     * @param config the parser configuration
     * @throws KmipParseException if validation fails
     */
    public void validatePadding(ByteBuffer buffer, int paddingLength, String tagString, 
                               ParserConfig config) throws KmipParseException {
        
        if (paddingLength <= 0) {
            return; // No padding to validate
        }
        
        if (buffer.remaining() < paddingLength) {
            throw KmipParseException.insufficientData(paddingLength, buffer.remaining(), tagString);
        }
        
        if (config.isValidatePaddingBytes()) {
            for (int i = 0; i < paddingLength; i++) {
                byte padByte = buffer.get();
                if (padByte != 0) {
                    throw new KmipParseException(
                        KmipParseException.ParseErrorType.INVALID_PADDING,
                        String.format("Invalid padding byte at position %d for tag %s. Expected 0, got %d", 
                                i, tagString, padByte),
                        tagString,
                        buffer.position() - 1
                    );
                }
            }
        } else {
            // Skip padding without validation
            buffer.position(buffer.position() + paddingLength);
        }
        
        log.debug("Padding validation passed for tag {} ({} bytes)", tagString, paddingLength);
    }
    
    /**
     * Validates a tag string.
     * 
     * @param tagString the tag to validate
     * @throws KmipParseException if validation fails
     */
    private void validateTag(String tagString) throws KmipParseException {
        if (tagString == null || tagString.length() != 6) {
            throw new KmipParseException(
                KmipParseException.ParseErrorType.INVALID_TAG,
                "Tag must be exactly 6 hex characters, got: " + tagString
            );
        }
        
        // Validate hex format
        try {
            Integer.parseInt(tagString, 16);
        } catch (NumberFormatException e) {
            throw new KmipParseException(
                KmipParseException.ParseErrorType.INVALID_TAG,
                "Tag must be valid hexadecimal: " + tagString
            );
        }
    }
    
    /**
     * Validates a type code.
     * 
     * @param type the type to validate
     * @param config the parser configuration
     * @throws KmipParseException if validation fails
     */
    private void validateType(byte type, ParserConfig config) throws KmipParseException {
        // Basic type validation - ensure it's a known KMIP type
        if (type < 0x01 || type > 0x0A) {
            if (!config.isAllowUnknownTypes()) {
                throw KmipParseException.unsupportedType(type, "UNKNOWN");
            } else {
                log.warn("Unknown KMIP type encountered: 0x{}", String.format("%02X", type));
            }
        }
    }
    
    /**
     * Validates a length value.
     * 
     * @param length the length to validate
     * @param tagString the tag for error reporting
     * @param config the parser configuration
     * @throws KmipParseException if validation fails
     */
    private void validateLength(int length, String tagString, ParserConfig config) 
            throws KmipParseException {
        
        if (length < 0) {
            throw new KmipParseException(
                KmipParseException.ParseErrorType.INVALID_LENGTH,
                "Length cannot be negative: " + length,
                tagString,
                -1
            );
        }
        
        if (length > config.getMaxMessageSizeBytes()) {
            throw new KmipParseException(
                KmipParseException.ParseErrorType.BUFFER_OVERFLOW,
                String.format("Length %d exceeds maximum allowed %d", 
                        length, config.getMaxMessageSizeBytes()),
                tagString,
                -1
            );
        }
    }
    
    /**
     * Converts byte array to hex string.
     * 
     * @param bytes the bytes to convert
     * @return hex string representation
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }
    
    /**
     * Represents a parsed TTLV header.
     */
    public static class TtlvHeader {
        private final String tag;
        private final byte type;
        private final int length;
        
        public TtlvHeader(String tag, byte type, int length) {
            this.tag = tag;
            this.type = type;
            this.length = length;
        }
        
        public String getTag() { return tag; }
        public byte getType() { return type; }
        public int getLength() { return length; }
        
        @Override
        public String toString() {
            return String.format("TtlvHeader{tag='%s', type=0x%02X, length=%d}", 
                    tag, type, length);
        }
    }
}
