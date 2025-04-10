package com.kmip.server.protocol.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.KmipTagResolver;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * KMIP Protocol Encoder
 *
 * This class is responsible for encoding KMIP protocol messages according to the
 * KMIP 2.0 specification. It handles the Tag-Type-Length-Value (TTLV) encoding
 * format and converts KmipMessage objects into binary messages that can be sent
 * over the network.
 *
 * The encoder is designed to be flexible and can handle any KMIP operation
 * (Create, Get, Destroy, Rotate, etc.) by encoding the message structure
 * according to the KMIP specification. It ensures proper field ordering and
 * padding as required by the protocol.
 */
@Component
public class KmipEncoder {

    private static final Logger log = LoggerFactory.getLogger(KmipEncoder.class);

    /**
     * KMIP TTLV Type constants as defined in the KMIP 2.0 specification
     * These constants represent the data types that can be used in KMIP messages.
     */
    private static final byte TYPE_STRUCTURE = 0x01;    // Nested structure
    private static final byte TYPE_INTEGER = 0x02;      // 32-bit signed integer
    private static final byte TYPE_LONG_INTEGER = 0x03; // 64-bit signed integer
    private static final byte TYPE_BIG_INTEGER = 0x04;  // Big integer (variable length)
    private static final byte TYPE_ENUMERATION = 0x05;  // Enumeration value (32-bit integer)
    private static final byte TYPE_BOOLEAN = 0x06;      // Boolean value
    private static final byte TYPE_TEXT_STRING = 0x07;  // Text string
    private static final byte TYPE_BYTE_STRING = 0x08;  // Byte string (binary data)
    private static final byte TYPE_DATE_TIME = 0x09;    // Date-time value

    /**
     * KMIP Tag constants for common message elements
     * These constants are used to identify different parts of a KMIP message.
     * They are organized by category for better readability.
     */
    // Message structure tags
    public static final int TAG_RESPONSE_MESSAGE = KmipTagResolver.TAG_RESPONSE_MESSAGE;
    public static final int TAG_RESPONSE_HEADER = KmipTagResolver.TAG_RESPONSE_HEADER;
    public static final int TAG_PROTOCOL_VERSION = KmipTagResolver.TAG_PROTOCOL_VERSION;
    public static final int TAG_TIMESTAMP = KmipTagResolver.TAG_TIMESTAMP;
    public static final int TAG_BATCH_COUNT = KmipTagResolver.TAG_BATCH_COUNT;
    public static final int TAG_RESPONSE_BATCH_ITEM = KmipTagResolver.TAG_RESPONSE_BATCH_ITEM;
    public static final int TAG_RESPONSE_PAYLOAD = KmipTagResolver.TAG_RESPONSE_PAYLOAD;

    // Operation result tags
    public static final int TAG_RESULT_STATUS = KmipTagResolver.TAG_RESULT_STATUS;
    public static final int TAG_RESULT_REASON = KmipTagResolver.TAG_RESULT_REASON;
    public static final int TAG_RESULT_MESSAGE = KmipTagResolver.TAG_RESULT_MESSAGE;

    // Operation and object tags
    public static final int TAG_OPERATION = KmipTagResolver.TAG_OPERATION;
    public static final int TAG_OBJECT_TYPE = KmipTagResolver.TAG_OBJECT_TYPE;
    public static final int TAG_UNIQUE_IDENTIFIER = KmipTagResolver.TAG_UNIQUE_IDENTIFIER;
    public static final int TAG_TEMPLATE_ATTRIBUTE = KmipTagResolver.TAG_TEMPLATE_ATTRIBUTE;

    // Cryptographic key tags
    public static final int TAG_SYMMETRIC_KEY = KmipTagResolver.TAG_SYMMETRIC_KEY;
    public static final int TAG_KEY_BLOCK = KmipTagResolver.TAG_KEY_BLOCK;
    public static final int TAG_KEY_FORMAT_TYPE = KmipTagResolver.TAG_KEY_FORMAT_TYPE;
    public static final int TAG_KEY_VALUE = KmipTagResolver.TAG_KEY_VALUE;
    public static final int TAG_KEY_MATERIAL = KmipTagResolver.TAG_KEY_MATERIAL;
    public static final int TAG_CRYPTOGRAPHIC_ALGORITHM = KmipTagResolver.TAG_CRYPTOGRAPHIC_ALGORITHM;
    public static final int TAG_CRYPTOGRAPHIC_LENGTH = KmipTagResolver.TAG_CRYPTOGRAPHIC_LENGTH;
    public static final int TAG_CRYPTOGRAPHIC_USAGE_MASK = KmipTagResolver.TAG_CRYPTOGRAPHIC_USAGE_MASK;


    /**
     * Encodes a KmipMessage object into a TTLV byte array.
     *
     * This method takes a KmipMessage object and encodes it into a binary format
     * according to the KMIP 2.0 specification. The resulting byte array can be
     * sent over the network to a KMIP client.
     *
     * @param message The KmipMessage to encode
     * @param rootTag The top-level tag for the message (e.g., TAG_RESPONSE_MESSAGE)
     * @return Byte array representing the encoded TTLV message
     * @throws IOException If an I/O error occurs during encoding
     */
    public byte[] encode(KmipMessage message, int rootTag) throws IOException {
        if (message == null) {
            throw new IllegalArgumentException("Cannot encode null message");
        }

        log.debug("Encoding KMIP message with root tag: 0x{}", Integer.toHexString(rootTag));

        try {
            // Create output streams for writing the encoded message
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream dataStream = new DataOutputStream(byteStream);

            // Encode the message structure recursively
            encodeStructure(dataStream, message, rootTag);

            // Get the final encoded message
            byte[] result = byteStream.toByteArray();

            // Log the encoded message details
            log.debug("Encoded message size: {} bytes", result.length);
            if (log.isTraceEnabled()) {
                log.trace("Encoded message hex dump: {}", bytesToHex(result, 0, Math.min(result.length, 100)));
            }

            return result;
        } catch (IOException e) {
            log.error("Error encoding KMIP message: {}", e.getMessage());
            throw new IOException("Failed to encode KMIP message: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a byte array to a hexadecimal string with formatting.
     *
     * @param bytes The byte array to convert
     * @param offset The starting offset in the byte array
     * @param length The number of bytes to convert
     * @return A formatted hexadecimal string representation of the byte array
     */
    private String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length && i < bytes.length; i++) {
            sb.append(String.format("%02X ", bytes[i] & 0xFF));
            if ((i - offset + 1) % 16 == 0) sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Encodes a KMIP message structure with the specified tag.
     *
     * This method handles the encoding of KMIP structures, ensuring that fields
     * are encoded in the correct order according to the KMIP specification.
     * Different structure types (Response Header, Response Batch Item, etc.)
     * have specific field ordering requirements.
     *
     * @param dos The output stream to write the encoded structure to
     * @param message The KmipMessage containing the structure to encode
     * @param tag The tag identifying the type of structure
     * @throws IOException If an I/O error occurs during encoding
     */
    private void encodeStructure(DataOutputStream dos, KmipMessage message, int tag) throws IOException {
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        DataOutputStream contentDos = new DataOutputStream(contentStream);

        log.debug("Encoding structure with tag: 0x{}", Integer.toHexString(tag));

        // Different structure types have different field ordering requirements
        switch (tag) {
            case TAG_RESPONSE_HEADER:
                encodeResponseHeader(contentDos, message);
                break;

            case TAG_RESPONSE_BATCH_ITEM:
                encodeResponseBatchItem(contentDos, message);
                break;

            case TAG_RESPONSE_PAYLOAD:
                encodeResponsePayload(contentDos, message);
                break;

            default:
                // For other structures, encode fields in the order they were added
                encodeGenericStructure(contentDos, message);
                break;
        }

        // Write the structure with its tag, type, and length
        byte[] contentBytes = contentStream.toByteArray();
        writeTagTypeLength(dos, tag, TYPE_STRUCTURE, contentBytes.length);
        dos.write(contentBytes);
        pad(dos, contentBytes.length); // Pad structure content to 8-byte boundary
    }

    /**
     * Encodes a Response Header structure.
     * Fields must be encoded in a specific order according to the KMIP specification.
     */
    private void encodeResponseHeader(DataOutputStream dos, KmipMessage message) throws IOException {
        // Protocol Version MUST be first
        List<Object> protocolVersionValues = message.getFields().get(TAG_PROTOCOL_VERSION);
        if (protocolVersionValues != null && !protocolVersionValues.isEmpty()) {
            encodeStructure(dos, (KmipMessage) protocolVersionValues.get(0), TAG_PROTOCOL_VERSION);
        }

        // Time Stamp MUST be second
        List<Object> timestampValues = message.getFields().get(TAG_TIMESTAMP);
        if (timestampValues != null && !timestampValues.isEmpty()) {
            encodeField(dos, TAG_TIMESTAMP, timestampValues.get(0));
        }

        // Batch Count MUST be third
        List<Object> batchCountValues = message.getFields().get(TAG_BATCH_COUNT);
        if (batchCountValues != null && !batchCountValues.isEmpty()) {
            encodeField(dos, TAG_BATCH_COUNT, batchCountValues.get(0));
        }
    }

    /**
     * Encodes a Response Batch Item structure.
     * Fields must be encoded in a specific order according to the KMIP specification.
     */
    private void encodeResponseBatchItem(DataOutputStream dos, KmipMessage message) throws IOException {
        // Operation MUST be first
        List<Object> operationValues = message.getFields().get(TAG_OPERATION);
        if (operationValues != null && !operationValues.isEmpty()) {
            encodeField(dos, TAG_OPERATION, operationValues.get(0));
        }

        // Result Status MUST be second
        List<Object> resultStatusValues = message.getFields().get(TAG_RESULT_STATUS);
        if (resultStatusValues != null && !resultStatusValues.isEmpty()) {
            encodeField(dos, TAG_RESULT_STATUS, resultStatusValues.get(0));
        }

        // Result Reason MUST be third if present
        List<Object> resultReasonValues = message.getFields().get(TAG_RESULT_REASON);
        if (resultReasonValues != null && !resultReasonValues.isEmpty()) {
            encodeField(dos, TAG_RESULT_REASON, resultReasonValues.get(0));
        }

        // Result Message MUST be fourth if present - CRITICAL for PyKMIP compatibility
        List<Object> resultMessageValues = message.getFields().get(TAG_RESULT_MESSAGE);
        if (resultMessageValues != null && !resultMessageValues.isEmpty()) {
            log.debug("Encoding Result Message: {}", resultMessageValues.get(0));
            encodeField(dos, TAG_RESULT_MESSAGE, resultMessageValues.get(0));
        } else {
            log.debug("No Result Message found in Response Batch Item");
        }

        // Response Payload MUST be fifth if present
        List<Object> responsePayloadValues = message.getFields().get(TAG_RESPONSE_PAYLOAD);
        if (responsePayloadValues != null && !responsePayloadValues.isEmpty()) {
            encodeStructure(dos, (KmipMessage) responsePayloadValues.get(0), TAG_RESPONSE_PAYLOAD);
        }
    }

    /**
     * Encodes a Response Payload structure.
     * Fields must be encoded in a specific order according to the KMIP specification.
     */
    private void encodeResponsePayload(DataOutputStream dos, KmipMessage message) throws IOException {
        log.debug("Encoding Response Payload structure");

        // Object Type MUST be first
        List<Object> objectTypeValues = message.getFields().get(TAG_OBJECT_TYPE);
        if (objectTypeValues != null && !objectTypeValues.isEmpty()) {
            log.debug("Encoding Object Type (0x{}): {}", Integer.toHexString(TAG_OBJECT_TYPE), objectTypeValues.get(0));
            encodeField(dos, TAG_OBJECT_TYPE, objectTypeValues.get(0));
        } else {
            String error = "Object Type is missing from Response Payload";
            log.error(error);
            throw new IOException(error);
        }

        // Unique Identifier MUST be second
        List<Object> uniqueIdentifierValues = message.getFields().get(TAG_UNIQUE_IDENTIFIER);
        if (uniqueIdentifierValues != null && !uniqueIdentifierValues.isEmpty()) {
            log.debug("Encoding Unique Identifier (0x{}): {}", Integer.toHexString(TAG_UNIQUE_IDENTIFIER), uniqueIdentifierValues.get(0));
            encodeField(dos, TAG_UNIQUE_IDENTIFIER, uniqueIdentifierValues.get(0));
        }

        // Symmetric Key MUST be third (for Get operation)
        List<Object> symmetricKeyValues = message.getFields().get(TAG_SYMMETRIC_KEY);
        if (symmetricKeyValues != null && !symmetricKeyValues.isEmpty()) {
            log.debug("Encoding Symmetric Key (0x{})", Integer.toHexString(TAG_SYMMETRIC_KEY));
            encodeField(dos, TAG_SYMMETRIC_KEY, symmetricKeyValues.get(0));
        }

        // Template-Attribute last if present
        List<Object> templateAttributeValues = message.getFields().get(TAG_TEMPLATE_ATTRIBUTE);
        if (templateAttributeValues != null && !templateAttributeValues.isEmpty()) {
            log.debug("Encoding Template-Attribute structure");
            encodeStructure(dos, (KmipMessage) templateAttributeValues.get(0), TAG_TEMPLATE_ATTRIBUTE);
        }

        if (log.isTraceEnabled()) {
            log.trace("Response Payload encoding complete. Fields encoded in order:");
            message.getFields().forEach((fieldTag, values) -> {
                log.trace("- Tag: 0x{} -> Values: {}", Integer.toHexString(fieldTag), values);
            });
        }
    }

    /**
     * Encodes a generic structure with no specific field ordering requirements.
     */
    private void encodeGenericStructure(DataOutputStream dos, KmipMessage message) throws IOException {
        for (Map.Entry<Integer, List<Object>> entry : message.getFields().entrySet()) {
            int fieldTag = entry.getKey();
            List<Object> values = entry.getValue();
            for (Object value : values) {
                if (value instanceof KmipMessage) {
                    encodeStructure(dos, (KmipMessage) value, fieldTag);
                } else {
                    encodeField(dos, fieldTag, value);
                }
            }
        }
    }

    /**
     * Encodes a field with the specified tag and value.
     *
     * This method handles the encoding of individual KMIP fields based on their data type.
     * It supports all the standard KMIP data types and can be extended to support
     * additional types as needed.
     *
     * @param dos The output stream to write the encoded field to
     * @param tag The tag identifying the field
     * @param value The value to encode
     * @throws IOException If an I/O error occurs during encoding
     */
    private void encodeField(DataOutputStream dos, int tag, Object value) throws IOException {
        if (value == null) {
            log.warn("Skipping encoding for null value with tag: 0x{}", Integer.toHexString(tag));
            return;
        }

        log.debug("Encoding field - Tag: 0x{}, Value Type: {}",
            Integer.toHexString(tag), value.getClass().getSimpleName());

        try {
            if (value instanceof KmipMessage) {
                // Handle special structure types
                if (tag == TAG_KEY_BLOCK) {
                    encodeKeyBlockStructure(dos, (KmipMessage) value, tag);
                } else if (tag == TAG_SYMMETRIC_KEY) {
                    log.debug("Encoding Symmetric Key structure with tag 0x{}", Integer.toHexString(tag));
                    encodeStructure(dos, (KmipMessage) value, tag);
                } else {
                    // Default structure handling
                    encodeStructure(dos, (KmipMessage) value, tag);
                }
            } else if (value instanceof Integer) {
                encodeIntegerValue(dos, tag, (Integer) value);
            } else if (value instanceof Long) {
                encodeLongValue(dos, tag, (Long) value);
            } else if (value instanceof Boolean) {
                encodeBoolean(dos, tag, (Boolean) value);
            } else if (value instanceof String) {
                encodeTextString(dos, tag, (String) value);
            } else if (value instanceof byte[]) {
                encodeByteString(dos, tag, (byte[]) value);
            } else if (value instanceof Instant) {
                encodeDateTime(dos, tag, (Instant) value);
            } else {
                log.error("Unsupported data type '{}' for encoding field with tag: 0x{}",
                    value.getClass().getName(), Integer.toHexString(tag));
                throw new IOException("Unsupported data type for TTLV encoding: " + value.getClass().getName());
            }
        } catch (IOException e) {
            log.error("Error encoding field with tag 0x{}: {}", Integer.toHexString(tag), e.getMessage());
            throw e;
        }
    }

    /**
     * Encodes an integer value with the appropriate type (Integer or Enumeration).
     */
    private void encodeIntegerValue(DataOutputStream dos, int tag, Integer value) throws IOException {
        // Determine if this tag should be encoded as an enumeration or integer
        if (tag == TAG_OBJECT_TYPE || KmipTagResolver.isEnumerationTag(tag)) {
            log.debug("Encoding as Enumeration - Tag: 0x{}, Value: {}", Integer.toHexString(tag), value);
            encodeInteger(dos, tag, value, TYPE_ENUMERATION);
        } else {
            log.debug("Encoding as Integer - Tag: 0x{}, Value: {}", Integer.toHexString(tag), value);
            encodeInteger(dos, tag, value, TYPE_INTEGER);
        }
    }

    /**
     * Encodes a long value with the appropriate type (LongInteger or BigInteger).
     */
    private void encodeLongValue(DataOutputStream dos, int tag, Long value) throws IOException {
        byte type = (KmipTagResolver.isLongIntegerTag(tag)) ? TYPE_LONG_INTEGER : TYPE_BIG_INTEGER;
        log.debug("Encoding as {} - Tag: 0x{}, Value: {}",
            type == TYPE_LONG_INTEGER ? "LongInteger" : "BigInteger", Integer.toHexString(tag), value);
        encodeLong(dos, tag, value, type);
    }

    /**
     * Encodes a Key Block structure with fields in the specific order required by the KMIP specification.
     */
    private void encodeKeyBlockStructure(DataOutputStream dos, KmipMessage keyBlock, int tag) throws IOException {
        log.debug("Encoding Key Block structure with tag 0x{}", Integer.toHexString(tag));

        // Create a new ByteArrayOutputStream for the Key Block content
        ByteArrayOutputStream keyBlockStream = new ByteArrayOutputStream();
        DataOutputStream keyBlockDos = new DataOutputStream(keyBlockStream);

        // Encode Key Block fields in the exact order required by the KMIP specification

        // 1. Key Format Type MUST be first (Required)
        List<Object> keyFormatTypeValues = keyBlock.getFields().get(TAG_KEY_FORMAT_TYPE);
        if (keyFormatTypeValues != null && !keyFormatTypeValues.isEmpty()) {
            log.debug("Encoding Key Format Type (0x{}): {}",
                Integer.toHexString(TAG_KEY_FORMAT_TYPE), keyFormatTypeValues.get(0));
            encodeField(keyBlockDos, TAG_KEY_FORMAT_TYPE, keyFormatTypeValues.get(0));
        }

        // 2. Key Value MUST be second (Required)
        List<Object> keyValueValues = keyBlock.getFields().get(TAG_KEY_VALUE);
        if (keyValueValues != null && !keyValueValues.isEmpty()) {
            log.debug("Encoding Key Value (0x{})", Integer.toHexString(TAG_KEY_VALUE));
            encodeField(keyBlockDos, TAG_KEY_VALUE, keyValueValues.get(0));
        }

        // 3. Cryptographic Algorithm (Optional)
        List<Object> cryptoAlgValues = keyBlock.getFields().get(TAG_CRYPTOGRAPHIC_ALGORITHM);
        if (cryptoAlgValues != null && !cryptoAlgValues.isEmpty()) {
            log.debug("Encoding Cryptographic Algorithm (0x{}): {}",
                Integer.toHexString(TAG_CRYPTOGRAPHIC_ALGORITHM), cryptoAlgValues.get(0));
            encodeField(keyBlockDos, TAG_CRYPTOGRAPHIC_ALGORITHM, cryptoAlgValues.get(0));
        }

        // 4. Cryptographic Length (Optional)
        List<Object> cryptoLengthValues = keyBlock.getFields().get(TAG_CRYPTOGRAPHIC_LENGTH);
        if (cryptoLengthValues != null && !cryptoLengthValues.isEmpty()) {
            log.debug("Encoding Cryptographic Length (0x{}): {}",
                Integer.toHexString(TAG_CRYPTOGRAPHIC_LENGTH), cryptoLengthValues.get(0));
            encodeField(keyBlockDos, TAG_CRYPTOGRAPHIC_LENGTH, cryptoLengthValues.get(0));
        }

        // 5. Cryptographic Usage Mask (Optional)
        List<Object> cryptoUsageMaskValues = keyBlock.getFields().get(TAG_CRYPTOGRAPHIC_USAGE_MASK);
        if (cryptoUsageMaskValues != null && !cryptoUsageMaskValues.isEmpty()) {
            log.debug("Encoding Cryptographic Usage Mask (0x{}): {}",
                Integer.toHexString(TAG_CRYPTOGRAPHIC_USAGE_MASK), cryptoUsageMaskValues.get(0));
            encodeField(keyBlockDos, TAG_CRYPTOGRAPHIC_USAGE_MASK, cryptoUsageMaskValues.get(0));
        }

        // Write the Key Block structure with its tag, type, and length
        byte[] keyBlockBytes = keyBlockStream.toByteArray();
        writeTagTypeLength(dos, tag, TYPE_STRUCTURE, keyBlockBytes.length);
        dos.write(keyBlockBytes);
        pad(dos, keyBlockBytes.length);
    }

    /**
     * Encodes an integer value with the specified tag and type.
     *
     * @param dos The output stream to write the encoded integer to
     * @param tag The tag identifying the field
     * @param value The integer value to encode
     * @param type The type of the integer (INTEGER or ENUMERATION)
     * @throws IOException If an I/O error occurs during encoding
     */
    private void encodeInteger(DataOutputStream dos, int tag, int value, byte type) throws IOException {
        // Validate the type
        if (type != TYPE_INTEGER && type != TYPE_ENUMERATION) {
            throw new IllegalArgumentException("Invalid type for integer encoding: " + type);
        }

        // Write the tag, type, and length
        writeTagTypeLength(dos, tag, type, 4); // Integer values are always 4 bytes

        // Write the integer value
        dos.writeInt(value);

        // Add padding to ensure 8-byte alignment
        pad(dos, 4);

        log.trace("Encoded {} - Tag: 0x{}, Value: {}",
            type == TYPE_ENUMERATION ? "enumeration" : "integer",
            Integer.toHexString(tag), value);
    }

    /**
     * Encodes a long value with the specified tag and type.
     *
     * @param dos The output stream to write the encoded long to
     * @param tag The tag identifying the field
     * @param value The long value to encode
     * @param type The type of the long (LONG_INTEGER or BIG_INTEGER)
     * @throws IOException If an I/O error occurs during encoding
     */
    private void encodeLong(DataOutputStream dos, int tag, long value, byte type) throws IOException {
        // Validate the type
        if (type != TYPE_LONG_INTEGER && type != TYPE_BIG_INTEGER) {
            throw new IllegalArgumentException("Invalid type for long encoding: " + type);
        }

        // Write the tag, type, and length
        writeTagTypeLength(dos, tag, type, 8); // Long values are always 8 bytes

        // Write the long value
        dos.writeLong(value);

        // No padding needed as long values are already 8-byte aligned
        log.trace("Encoded long - Tag: 0x{}, Value: {}", Integer.toHexString(tag), value);
    }

    /**
     * Encodes a boolean value with the specified tag.
     *
     * @param dos The output stream to write the encoded boolean to
     * @param tag The tag identifying the field
     * @param value The boolean value to encode
     * @throws IOException If an I/O error occurs during encoding
     */
    private void encodeBoolean(DataOutputStream dos, int tag, boolean value) throws IOException {
        // In KMIP, booleans are encoded as 8-byte values (similar to long)
        writeTagTypeLength(dos, tag, TYPE_BOOLEAN, 8);

        // Write the boolean value as a long (1 for true, 0 for false)
        dos.writeLong(value ? 1L : 0L);

        // No padding needed as boolean values are already 8-byte aligned
        log.trace("Encoded boolean - Tag: 0x{}, Value: {}", Integer.toHexString(tag), value);
    }

    /**
     * Encodes a text string with the specified tag.
     *
     * @param dos The output stream to write the encoded string to
     * @param tag The tag identifying the field
     * @param value The string value to encode
     * @throws IOException If an I/O error occurs during encoding
     */
    private void encodeTextString(DataOutputStream dos, int tag, String value) throws IOException {
        // Convert the string to UTF-8 bytes
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        // Write the tag, type, and length
        writeTagTypeLength(dos, tag, TYPE_TEXT_STRING, bytes.length);

        // Write the string bytes
        dos.write(bytes);

        // Add padding to ensure 8-byte alignment
        pad(dos, bytes.length);

        log.trace("Encoded text string - Tag: 0x{}, Length: {}", Integer.toHexString(tag), bytes.length);
    }

    /**
     * Encodes a byte string with the specified tag.
     *
     * @param dos The output stream to write the encoded bytes to
     * @param tag The tag identifying the field
     * @param value The byte array to encode
     * @throws IOException If an I/O error occurs during encoding
     */
    private void encodeByteString(DataOutputStream dos, int tag, byte[] value) throws IOException {
        // Write the tag, type, and length
        writeTagTypeLength(dos, tag, TYPE_BYTE_STRING, value.length);

        // Write the bytes
        dos.write(value);

        // Add padding to ensure 8-byte alignment
        pad(dos, value.length);

        log.trace("Encoded byte string - Tag: 0x{}, Length: {}", Integer.toHexString(tag), value.length);
    }

    /**
     * Encodes a date-time value with the specified tag.
     *
     * @param dos The output stream to write the encoded date-time to
     * @param tag The tag identifying the field
     * @param value The Instant value to encode
     * @throws IOException If an I/O error occurs during encoding
     */
    private void encodeDateTime(DataOutputStream dos, int tag, Instant value) throws IOException {
        // In KMIP, date-time values are encoded as 8-byte values representing seconds since epoch
        writeTagTypeLength(dos, tag, TYPE_DATE_TIME, 8);

        // Write the epoch seconds as a long
        dos.writeLong(value.getEpochSecond());

        // No padding needed as date-time values are already 8-byte aligned
        log.trace("Encoded date-time - Tag: 0x{}, Value: {}", Integer.toHexString(tag), value);
    }

    /**
     * Writes the Tag, Type, and Length fields of a TTLV item.
     *
     * @param dos The output stream to write to
     * @param tag The tag identifying the field
     * @param type The type of the field
     * @param length The length of the value in bytes
     * @throws IOException If an I/O error occurs during writing
     */
    private void writeTagTypeLength(DataOutputStream dos, int tag, byte type, int length) throws IOException {
        // Create a buffer for the TTLV header (8 bytes: 3 for tag, 1 for type, 4 for length)
        byte[] headerBytes = new byte[8];
        ByteBuffer header = ByteBuffer.wrap(headerBytes);
        header.order(ByteOrder.BIG_ENDIAN); // KMIP uses big-endian encoding

        // Write Tag (3 bytes)
        header.put((byte) ((tag >> 16) & 0xFF));
        header.put((byte) ((tag >> 8) & 0xFF));
        header.put((byte) (tag & 0xFF));

        // Write Type (1 byte)
        header.put(type);

        // Write Length (4 bytes)
        header.putInt(length);

        // Write the header to the output stream
        dos.write(headerBytes);

        log.trace("Wrote TTLV header - Tag: 0x{}, Type: 0x{}, Length: {}",
            Integer.toHexString(tag), Integer.toHexString(type & 0xFF), length);
    }

    /**
     * Adds padding bytes to ensure 8-byte alignment as required by the KMIP specification.
     *
     * @param dos The output stream to write the padding to
     * @param length The length of the value that needs padding
     * @throws IOException If an I/O error occurs during writing
     */
    private void pad(DataOutputStream dos, int length) throws IOException {
        // Calculate the number of padding bytes needed
        int padding = (8 - (length % 8)) % 8;

        if (padding > 0) {
            // Create a zero-filled padding array
            byte[] zeroPadding = new byte[padding];
            Arrays.fill(zeroPadding, (byte)0); // KMIP requires padding bytes to be zero

            // Write the padding bytes
            dos.write(zeroPadding);

            log.trace("Added {} bytes of padding (total length: {})", padding, length + padding);
        }
    }

    /**
     * Creates a TTLV-encoded byte array for a Get operation response.
     *
     * This method creates a complete response payload for a Get operation,
     * including all required fields according to the KMIP specification.
     * It is particularly useful for testing and for ensuring compatibility
     * with KMIP clients.
     *
     * @param objectType The object type (e.g., 2 for Symmetric Key)
     * @param uniqueIdentifier The unique identifier of the key
     * @param keyMaterial The key material (raw bytes)
     * @param cryptoAlgorithm The cryptographic algorithm (e.g., 3 for AES)
     * @param cryptoLength The cryptographic length in bits
     * @param cryptoUsageMask The cryptographic usage mask (e.g., 12 for Encrypt | Decrypt)
     * @return A TTLV-encoded byte array for the response payload
     * @throws IOException If an error occurs during encoding
     */
    public byte[] createGetResponsePayload(
            int objectType,
            String uniqueIdentifier,
            byte[] keyMaterial,
            int cryptoAlgorithm,
            int cryptoLength,
            int cryptoUsageMask) throws IOException {

        log.debug("Creating Get response payload for key: {}", uniqueIdentifier);

        try {
            // Create a KmipMessage for the response payload
            KmipMessage responsePayload = new KmipMessage();

            // 1. Add Object Type (Required)
            responsePayload.addField(TAG_OBJECT_TYPE, objectType);

            // 2. Add Unique Identifier (Required)
            responsePayload.addField(TAG_UNIQUE_IDENTIFIER, uniqueIdentifier);

            // 3. Create Symmetric Key structure
            KmipMessage symmetricKey = new KmipMessage();

            // 4. Create Key Block structure
            KmipMessage keyBlock = new KmipMessage();

            // 4.1 Add Key Format Type (Required)
            keyBlock.addField(TAG_KEY_FORMAT_TYPE, 1); // Raw format

            // 4.2 Create Key Value structure
            KmipMessage keyValue = new KmipMessage();

            // 4.2.1 Add Key Material (Required)
            keyValue.addField(TAG_KEY_MATERIAL, keyMaterial);

            // 4.2 Add Key Value to Key Block
            keyBlock.addField(TAG_KEY_VALUE, keyValue);

            // 4.3 Add Cryptographic Algorithm (Required)
            keyBlock.addField(TAG_CRYPTOGRAPHIC_ALGORITHM, cryptoAlgorithm);

            // 4.4 Add Cryptographic Length (Required)
            keyBlock.addField(TAG_CRYPTOGRAPHIC_LENGTH, cryptoLength);

            // 4.5 Add Cryptographic Usage Mask (Required)
            keyBlock.addField(TAG_CRYPTOGRAPHIC_USAGE_MASK, cryptoUsageMask);

            // 3.1 Add Key Block to Symmetric Key
            symmetricKey.addField(TAG_KEY_BLOCK, keyBlock);

            // 3. Add Symmetric Key to Response Payload
            responsePayload.addField(TAG_SYMMETRIC_KEY, symmetricKey);

            // Encode the response payload
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            encodeStructure(dos, responsePayload, TAG_RESPONSE_PAYLOAD);

            byte[] result = baos.toByteArray();
            log.debug("Created Get response payload of {} bytes", result.length);

            return result;
        } catch (Exception e) {
            log.error("Error creating Get response payload: {}", e.getMessage());
            throw new IOException("Failed to create Get response payload: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a byte array to a hexadecimal string for debugging purposes.
     *
     * @param bytes The byte array to convert
     * @return A hexadecimal string representation of the byte array
     */
    public String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, 0, bytes.length);
    }
}