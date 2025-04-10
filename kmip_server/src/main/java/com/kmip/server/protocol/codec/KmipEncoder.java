package com.kmip.server.protocol.codec;

import lombok.extern.slf4j.Slf4j;
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@Component
public class KmipEncoder {

    private static final Logger log = LoggerFactory.getLogger(KmipEncoder.class);

    // Constants mirroring KmipParser (or ideally defined centrally)
    private static final byte TYPE_STRUCTURE = 0x01;
    private static final byte TYPE_INTEGER = 0x02;
    private static final byte TYPE_LONG_INTEGER = 0x03;
    private static final byte TYPE_BIG_INTEGER = 0x04; // Assuming BigInteger maps to Long for simplicity now
    private static final byte TYPE_ENUMERATION = 0x05;
    private static final byte TYPE_BOOLEAN = 0x06;
    private static final byte TYPE_TEXT_STRING = 0x07;
    private static final byte TYPE_BYTE_STRING = 0x08;
    private static final byte TYPE_DATE_TIME = 0x09;
    private static final byte TYPE_INTERVAL = 0x0A; // Encoding Interval might need specific logic

    // Tag constants (add more as needed, mirroring KmipTagResolver if used)
    public static final int TAG_RESPONSE_MESSAGE = KmipTagResolver.TAG_RESPONSE_MESSAGE;
    public static final int TAG_RESPONSE_HEADER = KmipTagResolver.TAG_RESPONSE_HEADER;
    public static final int TAG_PROTOCOL_VERSION = KmipTagResolver.TAG_PROTOCOL_VERSION;
    public static final int TAG_TIMESTAMP = KmipTagResolver.TAG_TIMESTAMP;
    public static final int TAG_BATCH_COUNT = KmipTagResolver.TAG_BATCH_COUNT;
    public static final int TAG_RESPONSE_BATCH_ITEM = KmipTagResolver.TAG_RESPONSE_BATCH_ITEM;
    public static final int TAG_RESPONSE_PAYLOAD = KmipTagResolver.TAG_RESPONSE_PAYLOAD;
    public static final int TAG_RESULT_STATUS = KmipTagResolver.TAG_RESULT_STATUS;
    public static final int TAG_UNIQUE_IDENTIFIER = KmipTagResolver.TAG_UNIQUE_IDENTIFIER;
    public static final int TAG_OPERATION = KmipTagResolver.TAG_OPERATION;
    public static final int TAG_OBJECT_TYPE = KmipTagResolver.TAG_OBJECT_TYPE;
    public static final int TAG_SYMMETRIC_KEY = KmipTagResolver.TAG_SYMMETRIC_KEY;
    public static final int TAG_RESULT_REASON = KmipTagResolver.TAG_RESULT_REASON;
    public static final int TAG_RESULT_MESSAGE = KmipTagResolver.TAG_RESULT_MESSAGE;
    public static final int TAG_TEMPLATE_ATTRIBUTE = KmipTagResolver.TAG_TEMPLATE_ATTRIBUTE;
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
     * @param message The KmipMessage to encode.
     * @param rootTag The top-level tag for the message (e.g., TAG_RESPONSE_MESSAGE).
     * @return Byte array representing the encoded TTLV message.
     * @throws IOException If an I/O error occurs during encoding.
     */
    public byte[] encode(KmipMessage message, int rootTag) throws IOException {
        log.info("Encoding KMIP message with tag: 0x{}", Integer.toHexString(rootTag));

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        encodeStructure(dataStream, message, rootTag);

        byte[] result = byteStream.toByteArray();
        log.info("Encoded message size: {} bytes", result.length);
        log.info("Encoded message hex dump: {}", bytesToHex(result, 0, Math.min(result.length, 100)));

        return result;
    }

    private String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length && i < bytes.length; i++) {
            sb.append(String.format("%02X ", bytes[i] & 0xFF));
            if ((i - offset + 1) % 16 == 0) sb.append("\n");
        }
        return sb.toString();
    }

    private void encodeStructure(DataOutputStream dos, KmipMessage message, int tag) throws IOException {
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        DataOutputStream contentDos = new DataOutputStream(contentStream);

        log.info("Encoding structure with tag: 0x{}", Integer.toHexString(tag));

        // For Response Header, ensure fields are in correct order
        if (tag == TAG_RESPONSE_HEADER) {
            // Protocol Version MUST be first
            List<Object> protocolVersionValues = message.getFields().get(TAG_PROTOCOL_VERSION);
            if (protocolVersionValues != null && !protocolVersionValues.isEmpty()) {
                encodeStructure(contentDos, (KmipMessage) protocolVersionValues.get(0), TAG_PROTOCOL_VERSION);
            }

            // Time Stamp MUST be second
            List<Object> timestampValues = message.getFields().get(TAG_TIMESTAMP);
            if (timestampValues != null && !timestampValues.isEmpty()) {
                encodeField(contentDos, TAG_TIMESTAMP, timestampValues.get(0));
            }

            // Batch Count MUST be third
            List<Object> batchCountValues = message.getFields().get(TAG_BATCH_COUNT);
            if (batchCountValues != null && !batchCountValues.isEmpty()) {
                encodeField(contentDos, TAG_BATCH_COUNT, batchCountValues.get(0));
            }
        }
        // For Response Batch Item, ensure fields are in correct order
        else if (tag == TAG_RESPONSE_BATCH_ITEM) {
            // Operation MUST be first
            List<Object> operationValues = message.getFields().get(TAG_OPERATION);
            if (operationValues != null && !operationValues.isEmpty()) {
                encodeField(contentDos, TAG_OPERATION, operationValues.get(0));
            }

            // Result Status MUST be second
            List<Object> resultStatusValues = message.getFields().get(TAG_RESULT_STATUS);
            if (resultStatusValues != null && !resultStatusValues.isEmpty()) {
                encodeField(contentDos, TAG_RESULT_STATUS, resultStatusValues.get(0));
            }

            // Result Reason MUST be third if present
            List<Object> resultReasonValues = message.getFields().get(TAG_RESULT_REASON);
            if (resultReasonValues != null && !resultReasonValues.isEmpty()) {
                encodeField(contentDos, TAG_RESULT_REASON, resultReasonValues.get(0));
            }

            // Result Message MUST be fourth if present - CRITICAL for PyKMIP compatibility
            List<Object> resultMessageValues = message.getFields().get(TAG_RESULT_MESSAGE);
            if (resultMessageValues != null && !resultMessageValues.isEmpty()) {
                log.info("Encoding Result Message: {}", resultMessageValues.get(0));
                encodeField(contentDos, TAG_RESULT_MESSAGE, resultMessageValues.get(0));
            } else {
                log.warn("No Result Message found in Response Batch Item - PyKMIP client may fail");
            }

            // Response Payload MUST be fifth if present
            List<Object> responsePayloadValues = message.getFields().get(TAG_RESPONSE_PAYLOAD);
            if (responsePayloadValues != null && !responsePayloadValues.isEmpty()) {
                encodeStructure(contentDos, (KmipMessage) responsePayloadValues.get(0), TAG_RESPONSE_PAYLOAD);
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
        else {
            // For other structures, encode fields in the order they were added
            for (Map.Entry<Integer, List<Object>> entry : message.getFields().entrySet()) {
                int fieldTag = entry.getKey();
                List<Object> values = entry.getValue();
                for (Object value : values) {
                    if (value instanceof KmipMessage) {
                        encodeStructure(contentDos, (KmipMessage) value, fieldTag);
                    } else {
                        encodeField(contentDos, fieldTag, value);
                    }
                }
            }
        }

        byte[] contentBytes = contentStream.toByteArray();
        writeTagTypeLength(dos, tag, TYPE_STRUCTURE, contentBytes.length);
        dos.write(contentBytes);
        pad(dos, contentBytes.length); // Pad structure content
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
            // Special handling for object type and other enumeration tags
            if (tag == TAG_OBJECT_TYPE || KmipTagResolver.isEnumerationTag(tag)) {
                log.info("Encoding as Enumeration - Tag: 0x{}, Value: {}, Type: 0x{}",
                    Integer.toHexString(tag), value, Integer.toHexString(TYPE_ENUMERATION & 0xFF));
                encodeInteger(dos, tag, (Integer) value, TYPE_ENUMERATION);
            } else {
                log.info("Encoding as Integer - Tag: 0x{}, Value: {}, Type: 0x{}",
                    Integer.toHexString(tag), value, Integer.toHexString(TYPE_INTEGER & 0xFF));
                encodeInteger(dos, tag, (Integer) value, TYPE_INTEGER);
            }
        } else if (value instanceof Long) {
            byte type = (KmipTagResolver.isLongIntegerTag(tag)) ? TYPE_LONG_INTEGER : TYPE_BIG_INTEGER;
            log.debug("Encoding as {} - Tag: 0x{}, Value: {}",
                type == TYPE_LONG_INTEGER ? "LongInteger" : "BigInteger", Integer.toHexString(tag), value);
            encodeLong(dos, tag, (Long) value, type);
        } else if (value instanceof Boolean) {
            log.debug("Encoding as Boolean - Tag: 0x{}, Value: {}", Integer.toHexString(tag), value);
            encodeBoolean(dos, tag, (Boolean) value);
        } else if (value instanceof String) {
            log.debug("Encoding as TextString - Tag: 0x{}, Value: {}", Integer.toHexString(tag), value);
            encodeTextString(dos, tag, (String) value);
        } else if (value instanceof byte[]) {
            log.debug("Encoding as ByteString - Tag: 0x{}, Length: {}", Integer.toHexString(tag), ((byte[]) value).length);
            encodeByteString(dos, tag, (byte[]) value);
        } else if (value instanceof Instant) {
            log.debug("Encoding as DateTime - Tag: 0x{}, Value: {}", Integer.toHexString(tag), value);
            encodeDateTime(dos, tag, (Instant) value);
        } else if (value == null) {
            log.warn("Skipping encoding for null value with tag: 0x{}", Integer.toHexString(tag));
        } else {
            log.error("Unsupported data type '{}' for encoding field with tag: 0x{}", value.getClass().getName(), Integer.toHexString(tag));
            throw new IOException("Unsupported data type for TTLV encoding: " + value.getClass().getName());
        }
    }

    private void encodeInteger(DataOutputStream dos, int tag, int value, byte type) throws IOException {
        // For enumeration values, ensure we're using the correct type
        if (type == TYPE_ENUMERATION) {
            log.debug("Encoding enumeration - Tag: 0x{}, Value: {}, Type: 0x{}",
                Integer.toHexString(tag), value, Integer.toHexString(type & 0xFF));
            writeTagTypeLength(dos, tag, TYPE_ENUMERATION, 4);
            dos.writeInt(value);
            pad(dos, 4); // Add padding for enumeration values too
        } else {
            log.debug("Encoding integer - Tag: 0x{}, Value: {}, Type: 0x{}",
                Integer.toHexString(tag), value, Integer.toHexString(type & 0xFF));
            writeTagTypeLength(dos, tag, TYPE_INTEGER, 4);
            dos.writeInt(value);
            pad(dos, 4); // Pad integer value
        }
    }

     private void encodeLong(DataOutputStream dos, int tag, long value, byte type) throws IOException {
        writeTagTypeLength(dos, tag, type, 8);
        dos.writeLong(value);
        // Long is 8 bytes, already aligned
    }

    private void encodeBoolean(DataOutputStream dos, int tag, boolean value) throws IOException {
        writeTagTypeLength(dos, tag, TYPE_BOOLEAN, 8); // Booleans are encoded as 8 bytes in KMIP
        dos.writeLong(value ? 1L : 0L); // Write 1 or 0 as a Long
    }

    private void encodeTextString(DataOutputStream dos, int tag, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeTagTypeLength(dos, tag, TYPE_TEXT_STRING, bytes.length);
        dos.write(bytes);
        pad(dos, bytes.length);
    }

    private void encodeByteString(DataOutputStream dos, int tag, byte[] value) throws IOException {
        writeTagTypeLength(dos, tag, TYPE_BYTE_STRING, value.length);
        dos.write(value);
        pad(dos, value.length);
    }

     private void encodeDateTime(DataOutputStream dos, int tag, Instant value) throws IOException {
        // KMIP DateTime is typically a Long Integer representing seconds since epoch
        writeTagTypeLength(dos, tag, TYPE_DATE_TIME, 8);
        dos.writeLong(value.getEpochSecond());
        // Long is 8 bytes, already aligned
    }

    private void writeTagTypeLength(DataOutputStream dos, int tag, byte type, int length) throws IOException {
        // Create a zero-initialized buffer
        byte[] headerBytes = new byte[8];
        ByteBuffer header = ByteBuffer.wrap(headerBytes);
        header.order(ByteOrder.BIG_ENDIAN); // KMIP uses big-endian

        // Write Tag (3 bytes)
        header.put((byte) ((tag >> 16) & 0xFF));
        header.put((byte) ((tag >> 8) & 0xFF));
        header.put((byte) (tag & 0xFF));

        // Write Type (1 byte)
        header.put(type);

        // Write Length (4 bytes)
        header.putInt(length);

        dos.write(headerBytes);
    }

    private void pad(DataOutputStream dos, int length) throws IOException {
        int padding = (8 - (length % 8)) % 8;
        if (padding > 0) {
            byte[] zeroPadding = new byte[padding];
            Arrays.fill(zeroPadding, (byte)0); // Explicitly set all padding bytes to zero
            dos.write(zeroPadding);
            log.debug("Added {} bytes of zero padding after length {} (total length: {})",
                padding, length, length + padding);
        }
    }

    private void encodeObjectType(DataOutputStream dos, int objectType) throws IOException {
        log.debug("Encoding object type - Value: {} (0x{})", objectType, Integer.toHexString(objectType));
        encodeInteger(dos, TAG_OBJECT_TYPE, objectType, TYPE_ENUMERATION);
    }
}