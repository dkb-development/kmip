package com.kmip.server.protocol.codec;

import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.KmipTagResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Encoder for KMIP messages.
 *
 * This class is responsible for encoding KMIP messages into the TTLV (Tag, Type, Length, Value)
 * format as specified in the KMIP protocol specification.
 *
 * The encoder is stateless and thread-safe.
 */
@Component
public class KmipEncoder {

    private static final Logger log = LoggerFactory.getLogger(KmipEncoder.class);

    // TTLV Type constants
    public static final byte TYPE_STRUCTURE = 0x01;
    public static final byte TYPE_INTEGER = 0x02;
    public static final byte TYPE_LONG_INTEGER = 0x03;
    public static final byte TYPE_BIG_INTEGER = 0x04;
    public static final byte TYPE_ENUMERATION = 0x05;
    public static final byte TYPE_BOOLEAN = 0x06;
    public static final byte TYPE_TEXT_STRING = 0x07;
    public static final byte TYPE_BYTE_STRING = 0x08;
    public static final byte TYPE_DATE_TIME = 0x09;
    public static final byte TYPE_INTERVAL = 0x0A;

    // Common tag constants
    public static final int TAG_PROTOCOL_VERSION = KmipTagResolver.TAG_PROTOCOL_VERSION;
    public static final int TAG_PROTOCOL_VERSION_MAJOR = KmipTagResolver.TAG_PROTOCOL_VERSION_MAJOR;
    public static final int TAG_PROTOCOL_VERSION_MINOR = KmipTagResolver.TAG_PROTOCOL_VERSION_MINOR;
    public static final int TAG_OPERATION = KmipTagResolver.TAG_OPERATION;
    public static final int TAG_RESULT_STATUS = KmipTagResolver.TAG_RESULT_STATUS;
    public static final int TAG_RESULT_REASON = KmipTagResolver.TAG_RESULT_REASON;
    public static final int TAG_RESULT_MESSAGE = KmipTagResolver.TAG_RESULT_MESSAGE;
    public static final int TAG_BATCH_COUNT = KmipTagResolver.TAG_BATCH_COUNT;
    public static final int TAG_BATCH_ITEM = KmipTagResolver.TAG_BATCH_ITEM;
    public static final int TAG_RESPONSE_HEADER = KmipTagResolver.TAG_RESPONSE_HEADER;
    public static final int TAG_REQUEST_HEADER = KmipTagResolver.TAG_REQUEST_HEADER;
    public static final int TAG_RESPONSE_MESSAGE = KmipTagResolver.TAG_RESPONSE_MESSAGE;
    public static final int TAG_REQUEST_MESSAGE = KmipTagResolver.TAG_REQUEST_MESSAGE;
    public static final int TAG_RESPONSE_PAYLOAD = KmipTagResolver.TAG_RESPONSE_PAYLOAD;
    public static final int TAG_REQUEST_PAYLOAD = KmipTagResolver.TAG_REQUEST_PAYLOAD;
    public static final int TAG_RESPONSE_BATCH_ITEM = KmipTagResolver.TAG_RESPONSE_BATCH_ITEM;
    public static final int TAG_REQUEST_BATCH_ITEM = KmipTagResolver.TAG_REQUEST_BATCH_ITEM;
    public static final int TAG_UNIQUE_IDENTIFIER = KmipTagResolver.TAG_UNIQUE_IDENTIFIER;
    public static final int TAG_OBJECT_TYPE = KmipTagResolver.TAG_OBJECT_TYPE;
    public static final int TAG_SYMMETRIC_KEY = KmipTagResolver.TAG_SYMMETRIC_KEY;
    public static final int TAG_TEMPLATE_ATTRIBUTE = KmipTagResolver.TAG_TEMPLATE_ATTRIBUTE;
    public static final int TAG_KEY_BLOCK = KmipTagResolver.TAG_KEY_BLOCK;
    public static final int TAG_KEY_FORMAT_TYPE = KmipTagResolver.TAG_KEY_FORMAT_TYPE;
    public static final int TAG_KEY_VALUE = KmipTagResolver.TAG_KEY_VALUE;
    public static final int TAG_KEY_MATERIAL = KmipTagResolver.TAG_KEY_MATERIAL;
    public static final int TAG_CRYPTOGRAPHIC_ALGORITHM = KmipTagResolver.TAG_CRYPTOGRAPHIC_ALGORITHM;
    public static final int TAG_CRYPTOGRAPHIC_LENGTH = KmipTagResolver.TAG_CRYPTOGRAPHIC_LENGTH;
    public static final int TAG_CRYPTOGRAPHIC_USAGE_MASK = KmipTagResolver.TAG_CRYPTOGRAPHIC_USAGE_MASK;
    public static final int TAG_TIME_STAMP = KmipTagResolver.TAG_TIME_STAMP;

    /**
     * Encodes a KMIP message into a byte array.
     *
     * @param message The KMIP message to encode
     * @return The encoded byte array
     * @throws IOException If an error occurs during encoding
     */
    public byte[] encode(KmipMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        log.info("Encoding KMIP message with tag: 0x{}", Integer.toHexString(TAG_RESPONSE_MESSAGE));

        // Encode the message
        encodeStructure(dos, message, TAG_RESPONSE_MESSAGE);

        byte[] encodedMessage = baos.toByteArray();
        log.info("Encoded message size: {} bytes", encodedMessage.length);

        // Fix the TTLV header for PyKMIP compatibility
        if (encodedMessage.length >= 9) { // At least tag (4) + type (1) + length (4)
            // 1. Fix the tag format (already done in encodeStructure)

            // 2. Fix the type field - PyKMIP expects this to be 0x01 for structures
            encodedMessage[4] = TYPE_STRUCTURE;

            // Note: We don't fix the length field here because it will be fixed in KmipTcpServer
            // before sending the response to the client

            log.info("Updated TTLV header: Type=0x{}",
                    Integer.toHexString(encodedMessage[4] & 0xFF));
        }

        // Log the first 64 bytes of the encoded message as a hex dump
        int bytesToLog = Math.min(encodedMessage.length, 64);
        StringBuilder hexDump = new StringBuilder();
        for (int i = 0; i < bytesToLog; i++) {
            hexDump.append(String.format("%02X ", encodedMessage[i] & 0xFF));
            if ((i + 1) % 16 == 0) {
                hexDump.append("\n");
            }
        }
        log.info("Encoded message hex dump: {}", hexDump.toString());

        return encodedMessage;
    }

    private void encodeStructure(DataOutputStream dos, KmipMessage message, int tag) throws IOException {
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        DataOutputStream contentDos = new DataOutputStream(contentStream);

        log.info("Encoding structure with tag: 0x{}", Integer.toHexString(tag));

        // For Response Message, ensure fields are in correct order
        if (tag == TAG_RESPONSE_MESSAGE) {
            log.info("Encoding Response Message structure");

            // Response Header MUST be first
            List<Object> responseHeaderValues = message.getFields().get(TAG_RESPONSE_HEADER);
            if (responseHeaderValues != null && !responseHeaderValues.isEmpty()) {
                log.info("Encoding Response Header");
                encodeStructure(contentDos, (KmipMessage) responseHeaderValues.get(0), TAG_RESPONSE_HEADER);
            } else {
                String error = "Response Header is missing from Response Message";
                log.error(error);
                throw new IOException(error);
            }

            // Batch Items MUST be second
            List<Object> batchItemValues = message.getFields().get(TAG_RESPONSE_BATCH_ITEM);
            if (batchItemValues != null && !batchItemValues.isEmpty()) {
                log.info("Encoding {} Batch Items", batchItemValues.size());
                for (Object batchItem : batchItemValues) {
                    encodeStructure(contentDos, (KmipMessage) batchItem, TAG_RESPONSE_BATCH_ITEM);
                }
            } else {
                String error = "Batch Items are missing from Response Message";
                log.error(error);
                throw new IOException(error);
            }
        }
        // For Response Payload, ensure fields are in correct order
        else if (tag == TAG_RESPONSE_PAYLOAD) {
            log.info("Encoding Response Payload structure");

            // Object Type MUST be first
            List<Object> objectTypeValues = message.getFields().get(TAG_OBJECT_TYPE);
            if (objectTypeValues != null && !objectTypeValues.isEmpty()) {
                log.info("Encoding Object Type (0x{}): {}", Integer.toHexString(TAG_OBJECT_TYPE), objectTypeValues.get(0));
                encodeField(contentDos, TAG_OBJECT_TYPE, objectTypeValues.get(0));
            } else {
                String error = "Object Type is missing from Response Payload";
                log.error(error);
                throw new IOException(error);
            }

            // Unique Identifier MUST be second
            List<Object> uniqueIdentifierValues = message.getFields().get(TAG_UNIQUE_IDENTIFIER);
            if (uniqueIdentifierValues != null && !uniqueIdentifierValues.isEmpty()) {
                log.info("Encoding Unique Identifier (0x{}): {}", Integer.toHexString(TAG_UNIQUE_IDENTIFIER), uniqueIdentifierValues.get(0));
                encodeField(contentDos, TAG_UNIQUE_IDENTIFIER, uniqueIdentifierValues.get(0));
            }

            // Symmetric Key MUST be third (for Get operation)
            // This is the "secret" field that PyKMIP is looking for
            List<Object> symmetricKeyValues = message.getFields().get(TAG_SYMMETRIC_KEY);
            if (symmetricKeyValues != null && !symmetricKeyValues.isEmpty()) {
                log.info("Encoding Symmetric Key (0x{})", Integer.toHexString(TAG_SYMMETRIC_KEY));
                encodeField(contentDos, TAG_SYMMETRIC_KEY, symmetricKeyValues.get(0));
            } else {
                log.warn("No Symmetric Key found in response payload (this may cause issues with PyKMIP)");
            }

            // Template-Attribute last if present
            List<Object> templateAttributeValues = message.getFields().get(TAG_TEMPLATE_ATTRIBUTE);
            if (templateAttributeValues != null && !templateAttributeValues.isEmpty()) {
                log.info("Encoding Template-Attribute structure");
                encodeStructure(contentDos, (KmipMessage) templateAttributeValues.get(0), TAG_TEMPLATE_ATTRIBUTE);
            }

            // Debug log the encoded structure
            log.info("Response Payload encoding complete. Fields encoded in order:");
            message.getFields().forEach((fieldTag, values) -> {
                log.info("- Tag: 0x{} -> Values: {}", Integer.toHexString(fieldTag), values);
            });
        }
        // For other structures, encode fields in the order they appear in the message
        else {
            for (Map.Entry<Integer, List<Object>> entry : message.getFields().entrySet()) {
                int fieldTag = entry.getKey();
                List<Object> values = entry.getValue();

                if (values != null && !values.isEmpty()) {
                    for (Object value : values) {
                        encodeField(contentDos, fieldTag, value);
                    }
                }
            }
        }

        // Write the tag, type, length, and value
        // PyKMIP expects the tag to be in big-endian format without leading zeros
        // So we need to write the tag as 3 bytes instead of 4
        dos.writeByte((tag >> 16) & 0xFF); // Most significant byte
        dos.writeByte((tag >> 8) & 0xFF);  // Middle byte
        dos.writeByte(tag & 0xFF);         // Least significant byte
        dos.writeByte(0x00);               // Padding byte to make it 4 bytes total

        // Write the type - PyKMIP expects this to be 1 for structures
        dos.writeByte(TYPE_STRUCTURE);

        // Write the length
        dos.writeInt(contentStream.size());

        // Write the value
        dos.write(contentStream.toByteArray());

        // Add padding if needed
        int padding = (8 - (contentStream.size() % 8)) % 8;
        if (padding > 0) {
            byte[] paddingBytes = new byte[padding];
            dos.write(paddingBytes);
            log.debug("Added {} bytes of zero padding after length {} (total length: {})",
                padding, contentStream.size(), contentStream.size() + padding);
        }
    }

    private void encodeField(DataOutputStream dos, int tag, Object value) throws IOException {
        log.info("Encoding field - Tag: 0x{}, Value: {}, Value Type: {}",
            Integer.toHexString(tag), value, value != null ? value.getClass().getSimpleName() : "null");

        if (value instanceof KmipMessage) {
            log.info("Encoding as Structure - Tag: 0x{}", Integer.toHexString(tag));

            // Special handling for Symmetric Key tag
            if (tag == TAG_SYMMETRIC_KEY) {
                log.info("Encoding Symmetric Key structure with tag 0x{}", Integer.toHexString(tag));
                KmipMessage symmetricKey = (KmipMessage) value;
                log.info("Symmetric Key fields: {}", symmetricKey.getFields().keySet().stream()
                    .map(k -> "0x" + Integer.toHexString(k))
                    .collect(java.util.stream.Collectors.joining(", ")));
            }
            // Special handling for Key Block tag
            else if (tag == TAG_KEY_BLOCK) {
                log.info("Encoding Key Block structure with tag 0x{}", Integer.toHexString(tag));
                KmipMessage keyBlock = (KmipMessage) value;
                log.info("Key Block fields: {}", keyBlock.getFields().keySet().stream()
                    .map(k -> "0x" + Integer.toHexString(k))
                    .collect(java.util.stream.Collectors.joining(", ")));

                // Create a new ByteArrayOutputStream for the Key Block content
                ByteArrayOutputStream keyBlockStream = new ByteArrayOutputStream();
                DataOutputStream keyBlockDos = new DataOutputStream(keyBlockStream);

                // Encode Key Block fields in the exact order expected by PyKMIP

                // 1. Key Format Type MUST be first (Required)
                List<Object> keyFormatTypeValues = keyBlock.getFields().get(TAG_KEY_FORMAT_TYPE);
                if (keyFormatTypeValues != null && !keyFormatTypeValues.isEmpty()) {
                    log.info("Encoding Key Format Type (0x{}): {}", Integer.toHexString(TAG_KEY_FORMAT_TYPE), keyFormatTypeValues.get(0));
                    encodeField(keyBlockDos, TAG_KEY_FORMAT_TYPE, keyFormatTypeValues.get(0));
                }

                // 2. Key Compression Type (Optional) - We're skipping this

                // 3. Key Value MUST be second (Required)
                List<Object> keyValueValues = keyBlock.getFields().get(TAG_KEY_VALUE);
                if (keyValueValues != null && !keyValueValues.isEmpty()) {
                    log.info("Encoding Key Value (0x{})", Integer.toHexString(TAG_KEY_VALUE));
                    encodeField(keyBlockDos, TAG_KEY_VALUE, keyValueValues.get(0));
                }

                // 4. Cryptographic Algorithm (Optional)
                List<Object> cryptoAlgValues = keyBlock.getFields().get(TAG_CRYPTOGRAPHIC_ALGORITHM);
                if (cryptoAlgValues != null && !cryptoAlgValues.isEmpty()) {
                    log.info("Encoding Cryptographic Algorithm (0x{}): {}", Integer.toHexString(TAG_CRYPTOGRAPHIC_ALGORITHM), cryptoAlgValues.get(0));
                    encodeField(keyBlockDos, TAG_CRYPTOGRAPHIC_ALGORITHM, cryptoAlgValues.get(0));
                }

                // 5. Cryptographic Length (Optional)
                List<Object> cryptoLengthValues = keyBlock.getFields().get(TAG_CRYPTOGRAPHIC_LENGTH);
                if (cryptoLengthValues != null && !cryptoLengthValues.isEmpty()) {
                    log.info("Encoding Cryptographic Length (0x{}): {}", Integer.toHexString(TAG_CRYPTOGRAPHIC_LENGTH), cryptoLengthValues.get(0));
                    encodeField(keyBlockDos, TAG_CRYPTOGRAPHIC_LENGTH, cryptoLengthValues.get(0));
                }

                // 6. Key Wrapping Data (Optional) - We're skipping this

                // Encode the Key Block structure with the ordered content
                byte[] keyBlockBytes = keyBlockStream.toByteArray();

                // Write the tag (4 bytes)
                dos.writeInt(tag);

                // Write the type (1 byte)
                dos.writeByte(TYPE_STRUCTURE);

                // Write the length (4 bytes)
                dos.writeInt(keyBlockBytes.length);

                // Write the value
                dos.write(keyBlockBytes);

                // Add padding if needed
                int padding = (8 - (keyBlockBytes.length % 8)) % 8;
                if (padding > 0) {
                    byte[] paddingBytes = new byte[padding];
                    dos.write(paddingBytes);
                }

                // Skip the default encoding since we've handled it manually
                return;
            }

            encodeStructure(dos, (KmipMessage) value, tag);
        } else if (value instanceof Integer) {
            // Check if this tag represents an enumeration
            boolean isEnum = KmipTagResolver.isEnumeration(tag);
            log.debug("Checking if tag 0x{} ({}) is enumeration: {}",
                Integer.toHexString(tag), KmipTagResolver.getTagName(tag), isEnum);

            if (isEnum) {
                log.info("Encoding as Enumeration - Tag: 0x{}, Value: {}, Type: 0x{}",
                    Integer.toHexString(tag), value, Integer.toHexString(TYPE_ENUMERATION));
                encodeEnumeration(dos, tag, (Integer) value);
            } else {
                log.info("Encoding as Integer - Tag: 0x{}, Value: {}, Type: 0x{}",
                    Integer.toHexString(tag), value, Integer.toHexString(TYPE_INTEGER));
                encodeInteger(dos, tag, (Integer) value);
            }
        } else if (value instanceof Long) {
            log.info("Encoding as Long Integer - Tag: 0x{}", Integer.toHexString(tag));
            encodeLongInteger(dos, tag, (Long) value);
        } else if (value instanceof Boolean) {
            log.info("Encoding as Boolean - Tag: 0x{}", Integer.toHexString(tag));
            encodeBoolean(dos, tag, (Boolean) value);
        } else if (value instanceof String) {
            log.info("Encoding as TextString - Tag: 0x{}", Integer.toHexString(tag));
            encodeTextString(dos, tag, (String) value);
        } else if (value instanceof byte[]) {
            log.info("Encoding as ByteString - Tag: 0x{}, Length: {}", Integer.toHexString(tag), ((byte[]) value).length);
            encodeByteString(dos, tag, (byte[]) value);
        } else if (value instanceof Instant) {
            log.info("Encoding as DateTime - Tag: 0x{}, Value: {}", Integer.toHexString(tag), value);
            encodeDateTime(dos, tag, (Instant) value);
        } else {
            throw new IOException("Unsupported value type: " + (value != null ? value.getClass().getName() : "null"));
        }
    }

    private void encodeInteger(DataOutputStream dos, int tag, int value) throws IOException {
        log.debug("Encoding integer - Tag: 0x{}, Value: {}, Type: 0x{}",
            Integer.toHexString(tag), value, Integer.toHexString(TYPE_INTEGER));

        // PyKMIP expects the tag to be in big-endian format without leading zeros
        dos.writeByte((tag >> 16) & 0xFF); // Most significant byte
        dos.writeByte((tag >> 8) & 0xFF);  // Middle byte
        dos.writeByte(tag & 0xFF);         // Least significant byte
        dos.writeByte(0x00);               // Padding byte to make it 4 bytes total

        dos.writeByte(TYPE_INTEGER);
        dos.writeInt(4); // Length is always 4 bytes for an integer
        dos.writeInt(value);

        // Add padding (4 bytes for integers)
        byte[] padding = new byte[4];
        dos.write(padding);
        log.debug("Added 4 bytes of zero padding after length 4 (total length: 8)");
    }

    private void encodeLongInteger(DataOutputStream dos, int tag, long value) throws IOException {
        dos.writeInt(tag);
        dos.writeByte(TYPE_LONG_INTEGER);
        dos.writeInt(8); // Length is always 8 bytes for a long integer
        dos.writeLong(value);
    }

    private void encodeEnumeration(DataOutputStream dos, int tag, int value) throws IOException {
        log.debug("Encoding enumeration - Tag: 0x{}, Value: {}, Type: 0x{}",
            Integer.toHexString(tag), value, Integer.toHexString(TYPE_ENUMERATION));

        // PyKMIP expects the tag to be in big-endian format without leading zeros
        dos.writeByte((tag >> 16) & 0xFF); // Most significant byte
        dos.writeByte((tag >> 8) & 0xFF);  // Middle byte
        dos.writeByte(tag & 0xFF);         // Least significant byte
        dos.writeByte(0x00);               // Padding byte to make it 4 bytes total

        dos.writeByte(TYPE_ENUMERATION);
        dos.writeInt(4); // Length is always 4 bytes for an enumeration
        dos.writeInt(value);

        // Add padding (4 bytes for enumerations)
        byte[] padding = new byte[4];
        dos.write(padding);
        log.debug("Added 4 bytes of zero padding after length 4 (total length: 8)");
    }

    private void encodeBoolean(DataOutputStream dos, int tag, boolean value) throws IOException {
        // PyKMIP expects the tag to be in big-endian format without leading zeros
        dos.writeByte((tag >> 16) & 0xFF); // Most significant byte
        dos.writeByte((tag >> 8) & 0xFF);  // Middle byte
        dos.writeByte(tag & 0xFF);         // Least significant byte
        dos.writeByte(0x00);               // Padding byte to make it 4 bytes total

        dos.writeByte(TYPE_BOOLEAN);
        dos.writeInt(8); // Length is always 8 bytes for a boolean
        dos.writeLong(value ? 1L : 0L);
    }

    private void encodeTextString(DataOutputStream dos, int tag, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        // PyKMIP expects the tag to be in big-endian format without leading zeros
        dos.writeByte((tag >> 16) & 0xFF); // Most significant byte
        dos.writeByte((tag >> 8) & 0xFF);  // Middle byte
        dos.writeByte(tag & 0xFF);         // Least significant byte
        dos.writeByte(0x00);               // Padding byte to make it 4 bytes total

        dos.writeByte(TYPE_TEXT_STRING);
        dos.writeInt(bytes.length);
        dos.write(bytes);

        // Add padding if needed
        int padding = (8 - (bytes.length % 8)) % 8;
        if (padding > 0) {
            byte[] paddingBytes = new byte[padding];
            dos.write(paddingBytes);
            log.debug("Added {} bytes of zero padding after length {} (total length: {})",
                padding, bytes.length, bytes.length + padding);
        }
    }

    private void encodeByteString(DataOutputStream dos, int tag, byte[] value) throws IOException {
        // PyKMIP expects the tag to be in big-endian format without leading zeros
        dos.writeByte((tag >> 16) & 0xFF); // Most significant byte
        dos.writeByte((tag >> 8) & 0xFF);  // Middle byte
        dos.writeByte(tag & 0xFF);         // Least significant byte
        dos.writeByte(0x00);               // Padding byte to make it 4 bytes total

        dos.writeByte(TYPE_BYTE_STRING);
        dos.writeInt(value.length);
        dos.write(value);

        // Add padding if needed
        int padding = (8 - (value.length % 8)) % 8;
        if (padding > 0) {
            byte[] paddingBytes = new byte[padding];
            dos.write(paddingBytes);
        }
    }

    private void encodeDateTime(DataOutputStream dos, int tag, Instant value) throws IOException {
        // Convert to UTC
        ZonedDateTime utcDateTime = value.atZone(ZoneOffset.UTC);

        // KMIP uses seconds since Jan 1, 1970 UTC (same as Unix timestamp)
        long seconds = utcDateTime.toEpochSecond();

        // PyKMIP expects the tag to be in big-endian format without leading zeros
        dos.writeByte((tag >> 16) & 0xFF); // Most significant byte
        dos.writeByte((tag >> 8) & 0xFF);  // Middle byte
        dos.writeByte(tag & 0xFF);         // Least significant byte
        dos.writeByte(0x00);               // Padding byte to make it 4 bytes total

        dos.writeByte(TYPE_DATE_TIME);
        dos.writeInt(8); // Length is always 8 bytes for a date-time
        dos.writeLong(seconds);
    }

    /**
     * Encodes a Result Message field with the given message.
     * This is a convenience method for encoding the Result Message field.
     *
     * @param dos The DataOutputStream to write to
     * @param message The result message
     * @throws IOException If an error occurs during encoding
     */
    public void encodeResultMessage(DataOutputStream dos, String message) throws IOException {
        log.info("Encoding Result Message: {}", message);
        encodeTextString(dos, TAG_RESULT_MESSAGE, message);
    }
}
