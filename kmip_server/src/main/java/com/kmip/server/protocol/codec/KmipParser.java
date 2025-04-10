package com.kmip.server.protocol.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.KmipTagResolver;
import com.kmip.server.protocol.tag.TagValueUtil;

/**
 * KMIP Protocol Parser
 *
 * This class is responsible for parsing KMIP protocol messages according to the
 * KMIP 2.0 specification. It handles the Tag-Type-Length-Value (TTLV) encoding
 * format and converts binary messages into KmipMessage objects that can be
 * processed by the application.
 *
 * The parser is designed to be flexible and can handle any KMIP operation
 * (Create, Get, Destroy, Rotate, etc.) as it parses the message structure
 * without making assumptions about the specific operation being performed.
 */
@Component
public class KmipParser {

    private static final Logger log = LoggerFactory.getLogger(KmipParser.class);

    /**
     * KMIP TTLV Type constants as defined in the KMIP 2.0 specification
     * These constants represent the data types that can be used in KMIP messages.
     * Additional types can be added as needed for future KMIP operations.
     */
    private static final byte TYPE_STRUCTURE = 0x01;    // Nested structure
    private static final byte TYPE_INTEGER = 0x02;      // 32-bit signed integer
    private static final byte TYPE_ENUMERATION = 0x05;  // Enumeration value (32-bit integer)
    private static final byte TYPE_BOOLEAN = 0x06;      // Boolean value
    private static final byte TYPE_TEXT_STRING = 0x07;  // Text string
    private static final byte TYPE_BYTE_STRING = 0x08;  // Byte string (binary data)
    private static final byte TYPE_DATE_TIME = 0x09;    // Date-time value

    // Additional types defined in the KMIP spec but not currently used
    // Can be uncommented and implemented when needed for future operations
    // private static final byte TYPE_LONG_INTEGER = 0x03;  // 64-bit signed integer
    // private static final byte TYPE_BIG_INTEGER = 0x04;   // Big integer (variable length)
    // private static final byte TYPE_INTERVAL = 0x0A;      // Time interval

    /**
     * Parses a KMIP message from raw bytes.
     *
     * This method handles the top-level parsing of a KMIP message, validating
     * the message structure and extracting the content for further processing.
     * It supports both request and response messages.
     *
     * @param data The raw byte array containing the KMIP message
     * @param length The length of the data to parse
     * @return A KmipMessage object containing the parsed message structure
     * @throws KmipParseException If the message cannot be parsed correctly
     */
    public KmipMessage parse(byte[] data, int length) throws KmipParseException {
        log.debug("Parsing KMIP message of {} bytes", length);
        ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);

        // Ensure we have at least enough bytes for the TTLV header
        if (buffer.remaining() <= 8) {
            throw new KmipParseException("Insufficient data for KMIP message header. Minimum 8 bytes required.");
        }

        // Read the top-level Tag, Type, Length
        byte[] tagBytes = new byte[3];
        buffer.get(tagBytes);
        String tag = bytesToHex(tagBytes);
        byte type = buffer.get();
        int valueLength = buffer.getInt();

        log.debug("Top-level message: Tag={}, Type={}, Length={}", tag, String.format("0x%02X", type), valueLength);

        // Validate top-level structure - accept both request and response messages
        boolean isRequestMessage = TagValueUtil.TAG_REQUEST_MESSAGE.equals(tag);
        boolean isResponseMessage = TagValueUtil.TAG_RESPONSE_MESSAGE.equals(tag);

        if ((!isRequestMessage && !isResponseMessage) || type != TYPE_STRUCTURE) {
            throw new KmipParseException(
                "Expected Request/Response Message Structure at top level, found Tag " + tag + ", Type " +
                String.format("0x%02X", type));
        }

        // Ensure we have enough bytes for the declared value length
        if (valueLength > buffer.remaining()) {
            throw new KmipParseException(
                "Insufficient data for Message value. Declared length: " + valueLength +
                ", Remaining bytes: " + buffer.remaining());
        }

        // Create a slice for the value portion of the message
        ByteBuffer messageContentBuffer = buffer.slice();
        messageContentBuffer.limit(valueLength);

        // Parse the contents of the message structure
        log.debug("Parsing message content structure");
        KmipMessage messageContent = parseStructure(messageContentBuffer);

        // Store the message type for reference
        messageContent.addMetaInfo("messageType", isRequestMessage ? "Request" : "Response");

        return messageContent;
    }

    /**
     * Parses a KMIP Structure recursively.
     *
     * This method recursively parses a KMIP structure, extracting all fields and their values
     * according to the TTLV format. It handles nested structures and ensures proper padding.
     *
     * @param buffer The ByteBuffer containing the structure to parse
     * @return A KmipMessage object containing the parsed structure
     * @throws KmipParseException If the structure cannot be parsed correctly
     */
    private KmipMessage parseStructure(ByteBuffer buffer) throws KmipParseException {
        KmipMessage structure = new KmipMessage();
        log.debug("Parsing structure with {} bytes remaining", buffer.remaining());

        while (buffer.hasRemaining() && buffer.remaining() >= 8) { // Need at least 8 bytes for TTLV header
            // Read Tag (3 bytes)
            byte[] tagBytes = new byte[3];
            buffer.get(tagBytes);
            String tagString = bytesToHex(tagBytes);

            // Read Type (1 byte)
            byte type = buffer.get();

            // Read Length (4 bytes)
            int valueLength = buffer.getInt();

            log.debug("Found field: Tag={}, Type={}, Length={}",
                tagString, String.format("0x%02X", type), valueLength);

            // Validate we have enough bytes for the value
            if (buffer.remaining() < valueLength) {
                throw new KmipParseException(
                    "Insufficient data for value of tag " + tagString +
                    ". Required: " + valueLength + ", Available: " + buffer.remaining());
            }

            // Create a slice for just the value portion
            ByteBuffer valueBuffer = buffer.slice();
            valueBuffer.limit(valueLength);

            // Parse the value based on its type
            Object value = parseValue(type, valueBuffer, tagString);

            // Add the field to the structure if parsing succeeded
            if (value != null) {
                int tagInt = KmipTagResolver.getTagValue(tagString);
                String tagName = KmipTagResolver.getTagName(tagInt);
                log.debug("Adding field: Tag=0x{} ({}), Value type={}",
                    Integer.toHexString(tagInt), tagName, value.getClass().getSimpleName());
                structure.addField(tagInt, value);
            }

            // Move past the value in the buffer
            buffer.position(buffer.position() + valueLength);

            // Handle padding (KMIP requires 8-byte alignment)
            int padding = (8 - (valueLength % 8)) % 8;
            if (padding > 0) {
                log.debug("Padding: {} bytes", padding);

                if (buffer.remaining() < padding) {
                    throw new KmipParseException(
                        "Insufficient data for padding after tag " + tagString +
                        ". Required: " + padding + ", Available: " + buffer.remaining());
                }

                // Validate padding bytes are zero as required by KMIP spec
                for (int i = 0; i < padding; i++) {
                    byte padByte = buffer.get();
                    if (padByte != 0) {
                        throw new KmipParseException(
                            "Invalid padding byte at position " + i + " for tag " + tagString +
                            ". Expected 0, got " + padByte);
                    }
                }
            }
        }

        return structure;
    }

    /**
     * Parses a single value based on its type.
     *
     * This method parses a value from a ByteBuffer based on the KMIP type.
     * It handles all the standard KMIP data types and can be extended to support
     * additional types as needed.
     *
     * @param type The KMIP type code (from the TTLV header)
     * @param valueBuffer The ByteBuffer containing the value to parse
     * @param tagString The tag string (for logging and error reporting)
     * @return The parsed value as an appropriate Java object
     * @throws KmipParseException If the value cannot be parsed correctly
     */
    private Object parseValue(byte type, ByteBuffer valueBuffer, String tagString) throws KmipParseException {
        // Handle empty values
        if (!valueBuffer.hasRemaining()) {
            // Empty structures are allowed by the KMIP spec
            if (type == TYPE_STRUCTURE) {
                log.debug("Empty structure for tag {}", tagString);
                return new KmipMessage();
            } else {
                // Other types should not have zero length according to the KMIP spec
                log.warn("Zero-length value encountered for non-structure type: 0x{} (tag: {})",
                    String.format("%02X", type), tagString);
                return null;
            }
        }

        try {
            switch (type) {
                case TYPE_STRUCTURE:
                    log.debug("Parsing nested structure for tag {}", tagString);
                    return parseStructure(valueBuffer);

                case TYPE_INTEGER:
                    if (valueBuffer.remaining() >= 4) {
                        int value = valueBuffer.getInt();
                        log.debug("Parsed INTEGER value: {} for tag {}", value, tagString);
                        return value;
                    } else {
                        throw new KmipParseException(
                            "Insufficient data for INTEGER: " + valueBuffer.remaining() +
                            " bytes (tag: " + tagString + ")");
                    }

                case TYPE_ENUMERATION:
                    if (valueBuffer.remaining() >= 4) {
                        int value = valueBuffer.getInt();
                        log.debug("Parsed ENUMERATION value: {} (0x{}) for tag {}",
                            value, Integer.toHexString(value), tagString);
                        return value;
                    } else {
                        throw new KmipParseException(
                            "Insufficient data for ENUMERATION: " + valueBuffer.remaining() +
                            " bytes (tag: " + tagString + ")");
                    }

                case TYPE_BOOLEAN:
                    if (valueBuffer.remaining() >= 8) {
                        byte[] booleanBytes = new byte[8];
                        valueBuffer.get(booleanBytes);
                        boolean value = (booleanBytes[7] & 0x01) != 0;
                        log.debug("Parsed BOOLEAN value: {} for tag {}", value, tagString);
                        return value;
                    } else {
                        throw new KmipParseException(
                            "Insufficient data for BOOLEAN: " + valueBuffer.remaining() +
                            " bytes (tag: " + tagString + ")");
                    }

                case TYPE_TEXT_STRING:
                    byte[] stringBytes = new byte[valueBuffer.remaining()];
                    valueBuffer.get(stringBytes);
                    String value = new String(stringBytes);
                    log.debug("Parsed TEXT_STRING value: '{}' for tag {}", value, tagString);
                    return value;

                case TYPE_BYTE_STRING:
                    byte[] byteStringBytes = new byte[valueBuffer.remaining()];
                    valueBuffer.get(byteStringBytes);
                    log.debug("Parsed BYTE_STRING of length {} for tag {}",
                        byteStringBytes.length, tagString);
                    return byteStringBytes;

                case TYPE_DATE_TIME:
                    if (valueBuffer.remaining() >= 8) {
                        long seconds = valueBuffer.getLong();
                        log.debug("Parsed DATE_TIME value: {} seconds for tag {}", seconds, tagString);
                        return seconds;
                    } else {
                        throw new KmipParseException(
                            "Insufficient data for DATE_TIME: " + valueBuffer.remaining() +
                            " bytes (tag: " + tagString + ")");
                    }

                default:
                    log.warn("Unhandled KMIP type: 0x{} for tag {}. Returning raw bytes.",
                        String.format("%02X", type), tagString);
                    byte[] rawBytes = new byte[valueBuffer.remaining()];
                    valueBuffer.get(rawBytes);
                    return rawBytes;
            }
        } catch (Exception e) {
            if (e instanceof KmipParseException) {
                throw (KmipParseException) e;
            } else {
                log.error("Error parsing value for tag {}: {}", tagString, e.getMessage());
                throw new KmipParseException(
                    "Error parsing value for tag " + tagString + ": " + e.getMessage());
            }
        }
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param bytes The byte array to convert
     * @return A hexadecimal string representation of the byte array
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    /**
     * Custom exception for KMIP parsing errors.
     * This exception is thrown when a KMIP message cannot be parsed correctly.
     */
    public static class KmipParseException extends Exception {
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a new KmipParseException with the specified detail message.
         *
         * @param message The detail message
         */
        public KmipParseException(String message) {
            super(message);
        }

        /**
         * Constructs a new KmipParseException with the specified detail message and cause.
         *
         * @param message The detail message
         * @param cause The cause of the exception
         */
        public KmipParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}