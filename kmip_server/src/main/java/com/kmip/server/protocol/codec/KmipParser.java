package com.kmip.server.protocol.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;

import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.KmipTagResolver;
import com.kmip.server.protocol.tag.TagValueUtil;
import com.kmip.server.protocol.codec.config.ParserConfig;
import com.kmip.server.protocol.codec.parser.TypeParserRegistry;
import com.kmip.server.protocol.codec.parser.KmipTypeParser;
import com.kmip.server.protocol.codec.validator.MessageValidator;
import com.kmip.server.core.exception.KmipParseException;
import com.kmip.server.core.enums.KmipTtlvType;

/**
 * KMIP Protocol Parser - Restructured Architecture
 *
 * This class is responsible for parsing KMIP protocol messages according to the
 * KMIP 2.0 specification. It handles the Tag-Type-Length-Value (TTLV) encoding
 * format and converts binary messages into KmipMessage objects.
 *
 * ARCHITECTURAL SEPARATION:
 * - Part-1 (Transport/Codec Layer): TTLV parsing, buffer management, basic validation
 * - Part-2 (Protocol Layer): Type-specific parsing, protocol validation, extensibility
 *
 * This design provides:
 * - Clear separation of concerns
 * - Easy extensibility for new KMIP types
 * - Configuration-driven behavior
 * - Better error handling and diagnostics
 * - Improved testability
 */
@Component
public class KmipParser {

    private static final Logger log = LoggerFactory.getLogger(KmipParser.class);

    // Configuration and dependencies
    @Autowired
    private ParserConfig config;

    @Autowired
    private TypeParserRegistry typeParserRegistry;

    @Autowired
    private MessageValidator messageValidator;

    // Statistics tracking
    private long totalMessagesProcessed = 0;
    private long totalParseErrors = 0;
    private long totalParseTimeMs = 0;

    // KMIP TTLV Type constants removed - now using KmipTtlvType enum directly

    /**
     * Parses a KMIP message from raw bytes using the new architecture.
     *
     * This method handles the top-level parsing of a KMIP message with improved
     * error handling, validation, and statistics tracking.
     *
     * @param data The raw byte array containing the KMIP message
     * @param length The length of the data to parse
     * @return A KmipMessage object containing the parsed message structure
     * @throws KmipParseException If the message cannot be parsed correctly
     */
    public KmipMessage parse(byte[] data, int length) throws KmipParseException {
        long startTime = System.currentTimeMillis();

        try {
            if (config.isEnableDetailedLogging()) {
                log.debug("Starting parse of KMIP message: {} bytes", length);
            }

            // Part-1: Transport/Codec Layer Validation
            messageValidator.validateTopLevelMessage(data, length, config);

            ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);

            // Parse and validate TTLV header
            MessageValidator.TtlvHeader header = messageValidator.validateTtlvHeader(buffer, config);

            // Advance buffer past header
            buffer.position(8);

            // Part-1: Message Structure Validation
            messageValidator.validateMessageStructure(header.getTag(), header.getType());

            // Ensure we have enough bytes for the declared value length
            if (header.getLength() > buffer.remaining()) {
                throw KmipParseException.insufficientData(
                    header.getLength(), buffer.remaining(), header.getTag());
            }

            // Create parsing context
            ParseContextImpl context = new ParseContextImpl(config, typeParserRegistry, 0);

            // Create a slice for the value portion of the message
            ByteBuffer messageContentBuffer = buffer.slice();
            messageContentBuffer.limit(header.getLength());

            // Part-2: Protocol Layer Parsing
            KmipMessage messageContent = parseStructureWithContext(
                messageContentBuffer, header.getTag(), context);

            // Store metadata
            boolean isRequestMessage = TagValueUtil.TAG_REQUEST_MESSAGE.equals(header.getTag());
            messageContent.addMetaInfo("messageType", isRequestMessage ? "Request" : "Response");
            messageContent.addMetaInfo("originalLength", String.valueOf(length));
            messageContent.addMetaInfo("parseTimeMs", String.valueOf(System.currentTimeMillis() - startTime));

            // Update statistics
            totalMessagesProcessed++;
            totalParseTimeMs += (System.currentTimeMillis() - startTime);

            if (config.isEnableDetailedLogging()) {
                log.debug("Successfully parsed KMIP message in {}ms",
                        System.currentTimeMillis() - startTime);
            }

            return messageContent;

        } catch (KmipParseException e) {
            totalParseErrors++;
            log.error("Parse error after {}ms: {}",
                    System.currentTimeMillis() - startTime, e.getMessage());
            throw e;
        } catch (Exception e) {
            totalParseErrors++;
            log.error("Unexpected error during parsing after {}ms: {}",
                    System.currentTimeMillis() - startTime, e.getMessage(), e);
            throw new KmipParseException(
                KmipParseException.ParseErrorType.UNKNOWN,
                "Unexpected error during parsing: " + e.getMessage()
            );
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
     * Parses a structure using the new context-based architecture.
     *
     * @param buffer the buffer containing the structure
     * @param tagString the tag for error reporting
     * @param context the parsing context
     * @return the parsed structure
     * @throws KmipParseException if parsing fails
     */
    private KmipMessage parseStructureWithContext(ByteBuffer buffer, String tagString,
                                                 ParseContextImpl context) throws KmipParseException {

        // Check nesting depth
        if (context.getNestingDepth() > config.getMaxNestingDepth()) {
            throw new KmipParseException(
                KmipParseException.ParseErrorType.STRUCTURE_TOO_DEEP,
                "Maximum nesting depth exceeded: " + context.getNestingDepth()
            );
        }

        KmipMessage structure = new KmipMessage();
        int fieldCount = 0;

        if (config.isEnableDetailedLogging()) {
            log.debug("Parsing structure with {} bytes remaining at depth {}",
                    buffer.remaining(), context.getNestingDepth());
        }

        while (buffer.hasRemaining() && buffer.remaining() >= 8) {
            // Check field count limit
            if (fieldCount >= config.getMaxFieldsPerStructure()) {
                throw new KmipParseException(
                    KmipParseException.ParseErrorType.TOO_MANY_FIELDS,
                    "Maximum fields per structure exceeded: " + fieldCount
                );
            }

            // Parse TTLV header
            MessageValidator.TtlvHeader fieldHeader = messageValidator.validateTtlvHeader(buffer, config);

            // Advance past header
            buffer.position(buffer.position() + 8);

            // Create value buffer
            ByteBuffer valueBuffer = buffer.slice();
            valueBuffer.limit(fieldHeader.getLength());

            // Parse value using type parser
            Object value = parseValueWithTypeParser(
                fieldHeader.getType(), valueBuffer, fieldHeader.getLength(),
                fieldHeader.getTag(), context);

            // Add field if parsing succeeded
            if (value != null) {
                int tagInt = KmipTagResolver.getTagValue(fieldHeader.getTag());
                structure.addField(tagInt, value);
                fieldCount++;

                if (config.isEnableDetailedLogging()) {
                    String tagName = KmipTagResolver.getTagName(tagInt);
                    log.debug("Added field: Tag=0x{} ({}), Value type={}",
                            Integer.toHexString(tagInt), tagName, value.getClass().getSimpleName());
                }
            }

            // Move past value
            buffer.position(buffer.position() + fieldHeader.getLength());

            // Handle padding
            int padding = (8 - (fieldHeader.getLength() % 8)) % 8;
            if (padding > 0) {
                messageValidator.validatePadding(buffer, padding, fieldHeader.getTag(), config);
            }
        }

        return structure;
    }

    /**
     * Parses a value using the appropriate type parser.
     *
     * @param type the KMIP type code
     * @param valueBuffer the buffer containing the value
     * @param valueLength the length of the value
     * @param tagString the tag for error reporting
     * @param context the parsing context
     * @return the parsed value
     * @throws KmipParseException if parsing fails
     */
    private Object parseValueWithTypeParser(byte type, ByteBuffer valueBuffer, int valueLength,
                                          String tagString, ParseContextImpl context)
            throws KmipParseException {

        // Try to get a registered type parser
        KmipTypeParser typeParser = typeParserRegistry.getParser(type);

        if (typeParser != null) {
            try {
                typeParserRegistry.recordParseSuccess(type);
                return typeParser.parseValue(valueBuffer, valueLength, tagString, context);
            } catch (Exception e) {
                typeParserRegistry.recordParseError(type);
                throw new KmipParseException(
                    KmipParseException.ParseErrorType.INVALID_VALUE,
                    "Error parsing value with type parser: " + e.getMessage(),
                    tagString,
                    valueBuffer.position()
                );
            }
        } else {
            // Fallback to legacy parsing for backward compatibility
            return parseValueLegacy(type, valueBuffer, tagString);
        }
    }

    /**
     * Legacy parsing method for backward compatibility.
     * This method contains the original parsing logic for KMIP types.
     *
     * @param type the KMIP type code
     * @param valueBuffer the buffer containing the value
     * @param tagString the tag for error reporting
     * @return the parsed value
     * @throws KmipParseException if parsing fails
     */
    private Object parseValueLegacy(byte type, ByteBuffer valueBuffer, String tagString)
            throws KmipParseException {

        // Handle empty values
        if (!valueBuffer.hasRemaining()) {
            if (type == KmipTtlvType.STRUCTURE.getCode()) {
                log.debug("Empty structure for tag {}", tagString);
                return new KmipMessage();
            } else {
                log.warn("Zero-length value encountered for non-structure type: 0x{} (tag: {})",
                    String.format("%02X", type), tagString);
                return null;
            }
        }

        // Get the TTLV type enum for better type handling
        KmipTtlvType ttlvType = KmipTtlvType.fromCode(type);

        try {
            if (ttlvType == null) {
                if (config.isAllowUnknownTypes()) {
                    log.warn("Unknown TTLV type: 0x{} for tag {}. Returning raw bytes.",
                        String.format("%02X", type), tagString);
                    byte[] rawBytes = new byte[valueBuffer.remaining()];
                    valueBuffer.get(rawBytes);
                    return rawBytes;
                } else {
                    throw KmipParseException.unsupportedType(type, tagString);
                }
            }

            switch (ttlvType) {
                case STRUCTURE:
                    log.debug("Parsing nested structure for tag {}", tagString);
                    return parseStructure(valueBuffer);

                case INTEGER:
                    if (valueBuffer.remaining() >= 4) {
                        int value = valueBuffer.getInt();
                        log.debug("Parsed INTEGER value: {} for tag {}", value, tagString);
                        return value;
                    } else {
                        throw new KmipParseException(
                            KmipParseException.ParseErrorType.INSUFFICIENT_DATA,
                            "Insufficient data for INTEGER: " + valueBuffer.remaining() +
                            " bytes (tag: " + tagString + ")"
                        );
                    }

                case ENUMERATION:
                    if (valueBuffer.remaining() >= 4) {
                        int value = valueBuffer.getInt();
                        log.debug("Parsed ENUMERATION value: {} (0x{}) for tag {}",
                            value, Integer.toHexString(value), tagString);
                        return value;
                    } else {
                        throw new KmipParseException(
                            KmipParseException.ParseErrorType.INSUFFICIENT_DATA,
                            "Insufficient data for ENUMERATION: " + valueBuffer.remaining() +
                            " bytes (tag: " + tagString + ")"
                        );
                    }

                case BOOLEAN:
                    if (valueBuffer.remaining() >= 8) {
                        byte[] booleanBytes = new byte[8];
                        valueBuffer.get(booleanBytes);
                        boolean value = (booleanBytes[7] & 0x01) != 0;
                        log.debug("Parsed BOOLEAN value: {} for tag {}", value, tagString);
                        return value;
                    } else {
                        throw new KmipParseException(
                            KmipParseException.ParseErrorType.INSUFFICIENT_DATA,
                            "Insufficient data for BOOLEAN: " + valueBuffer.remaining() +
                            " bytes (tag: " + tagString + ")"
                        );
                    }

                case TEXT_STRING:
                    byte[] stringBytes = new byte[valueBuffer.remaining()];
                    valueBuffer.get(stringBytes);
                    String value = new String(stringBytes);
                    log.debug("Parsed TEXT_STRING value: '{}' for tag {}", value, tagString);
                    return value;

                case BYTE_STRING:
                    byte[] byteStringBytes = new byte[valueBuffer.remaining()];
                    valueBuffer.get(byteStringBytes);
                    log.debug("Parsed BYTE_STRING of length {} for tag {}",
                        byteStringBytes.length, tagString);
                    return byteStringBytes;

                case DATE_TIME:
                    if (valueBuffer.remaining() >= 8) {
                        long seconds = valueBuffer.getLong();
                        log.debug("Parsed DATE_TIME value: {} seconds for tag {}", seconds, tagString);
                        return seconds;
                    } else {
                        throw new KmipParseException(
                            KmipParseException.ParseErrorType.INSUFFICIENT_DATA,
                            "Insufficient data for DATE_TIME: " + valueBuffer.remaining() +
                            " bytes (tag: " + tagString + ")"
                        );
                    }

                default:
                    // This should not happen since we already checked for null ttlvType above
                    log.warn("Unhandled KMIP type: {} for tag {}. Returning raw bytes.",
                        ttlvType, tagString);
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
                    KmipParseException.ParseErrorType.INVALID_VALUE,
                    "Error parsing value for tag " + tagString + ": " + e.getMessage()
                );
            }
        }
    }

    /**
     * Legacy parseStructure method for backward compatibility.
     *
     * @param buffer the buffer containing the structure
     * @return the parsed structure
     * @throws KmipParseException if parsing fails
     */
    private KmipMessage parseStructure(ByteBuffer buffer) throws KmipParseException {
        KmipMessage structure = new KmipMessage();
        log.debug("Parsing structure with {} bytes remaining", buffer.remaining());

        while (buffer.hasRemaining() && buffer.remaining() >= 8) {
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
                throw KmipParseException.insufficientData(valueLength, buffer.remaining(), tagString);
            }

            // Create a slice for just the value portion
            ByteBuffer valueBuffer = buffer.slice();
            valueBuffer.limit(valueLength);

            // Parse the value based on its type
            Object value = parseValueLegacy(type, valueBuffer, tagString);

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
                messageValidator.validatePadding(buffer, padding, tagString, config);
            }
        }

        return structure;
    }

    /**
     * Gets parsing statistics.
     *
     * @return map of parsing statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessagesProcessed", totalMessagesProcessed);
        stats.put("totalParseErrors", totalParseErrors);
        stats.put("totalParseTimeMs", totalParseTimeMs);
        stats.put("averageParseTimeMs",
                totalMessagesProcessed > 0 ? totalParseTimeMs / totalMessagesProcessed : 0);
        stats.put("errorRate",
                totalMessagesProcessed > 0 ? (double) totalParseErrors / totalMessagesProcessed : 0.0);

        // Include type parser statistics
        stats.putAll(typeParserRegistry.getStatistics());

        return stats;
    }

    /**
     * Implementation of ParseContext for providing context to type parsers.
     */
    private static class ParseContextImpl implements KmipTypeParser.ParseContext {
        private final ParserConfig config;
        private final TypeParserRegistry registry;
        private final int nestingDepth;
        private int currentPosition;

        public ParseContextImpl(ParserConfig config, TypeParserRegistry registry, int nestingDepth) {
            this.config = config;
            this.registry = registry;
            this.nestingDepth = nestingDepth;
            this.currentPosition = 0;
        }

        @Override
        public int getNestingDepth() {
            return nestingDepth;
        }

        @Override
        public int getCurrentPosition() {
            return currentPosition;
        }

        @Override
        public Object getParserConfig() {
            return config;
        }

        @Override
        public boolean isDetailedLoggingEnabled() {
            return config.isEnableDetailedLogging();
        }

        @Override
        public KmipTypeParser.ParseContext createChildContext() {
            return new ParseContextImpl(config, registry, nestingDepth + 1);
        }

        @Override
        public KmipTypeParser getTypeParser(byte typeCode) {
            return registry.getParser(typeCode);
        }

        public void setCurrentPosition(int position) {
            this.currentPosition = position;
        }
    }
}