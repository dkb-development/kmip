package com.kmip.server.protocol.codec;

import org.springframework.stereotype.Component;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.KmipTagResolver;
import com.kmip.server.protocol.tag.TagValueUtil;

@Component
public class KmipParser {

    // KMIP TTLV Type constants (add more as needed)
    private static final byte TYPE_STRUCTURE = 0x01;
    private static final byte TYPE_INTEGER = 0x02;
    private static final byte TYPE_LONG_INTEGER = 0x03;
    private static final byte TYPE_BIG_INTEGER = 0x04;
    private static final byte TYPE_ENUMERATION = 0x05;
    private static final byte TYPE_BOOLEAN = 0x06;
    private static final byte TYPE_TEXT_STRING = 0x07;
    private static final byte TYPE_BYTE_STRING = 0x08;
    private static final byte TYPE_DATE_TIME = 0x09;
    private static final byte TYPE_INTERVAL = 0x0A;

    /**
     * Parses the top-level KMIP Request Message.
     */
    public KmipMessage parse(byte[] data, int length) throws KmipParseException {
        ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);

        if (buffer.remaining() <= 7) {
             throw new KmipParseException("Insufficient data for KMIP message header.");
        }

        // Read the top-level Tag, Type, Length
        byte[] tagBytes = new byte[3];
        buffer.get(tagBytes);
        String tag = bytesToHex(tagBytes);
        byte type = buffer.get();
        int valueLength = buffer.getInt();

        // Validate top-level structure
        if (!TagValueUtil.TAG_REQUEST_MESSAGE.equals(tag) || type != TYPE_STRUCTURE) {
            throw new KmipParseException("Expected Request Message Structure (Tag 420078, Type 01) at top level, found Tag " + tag + ", Type " + type);
        }
        
        if (valueLength > buffer.remaining()) {
            throw new KmipParseException("Insufficient data for Request Message value. Declared length: " + valueLength + ", Remaining bytes: " + buffer.remaining());
        }
        
        // Create a slice for the *value* of the Request Message structure
        ByteBuffer messageContentBuffer = buffer.slice();
        messageContentBuffer.limit(valueLength);

        // Parse the *contents* of the Request Message structure
        // This should contain the Request Header and Batch Item(s)
        KmipMessage messageContent = parseStructure(messageContentBuffer);

        // Optional: Add top-level tag info if needed elsewhere, 
        // but messageContent is what the handler needs.
        // messageContent.addMetaInfo("topLevelTag", tag);

        return messageContent; 
    }

    /**
     * Parses a KMIP Structure recursively.
     */
    private KmipMessage parseStructure(ByteBuffer buffer) throws KmipParseException {
        KmipMessage structure = new KmipMessage();
        while (buffer.hasRemaining() && buffer.remaining() > 7) {
            byte[] tagBytes = new byte[3];
            buffer.get(tagBytes);
            String tagString = bytesToHex(tagBytes);
            byte type = buffer.get();
            int valueLength = buffer.getInt();
            
            if (buffer.remaining() < valueLength) {
                 throw new KmipParseException("Insufficient data for value of tag " + tagString + ". Required: " + valueLength + ", Available: " + buffer.remaining());
            }

            ByteBuffer valueBuffer = buffer.slice();
            valueBuffer.limit(valueLength);

            Object value = parseValue(type, valueBuffer);
            if (value != null) { // Don't add field if value parsing failed
                 int tagInt = KmipTagResolver.getTagValue(tagString);
                 structure.addField(tagInt, value);
             }

            buffer.position(buffer.position() + valueLength);
            int padding = (8 - (valueLength % 8)) % 8;
            if (buffer.remaining() >= padding) {
                // Validate padding bytes are zero
                for (int i = 0; i < padding; i++) {
                    byte padByte = buffer.get();
                    if (padByte != 0) {
                        throw new KmipParseException("Invalid padding byte at position " + i + " for tag " + tagString + ". Expected 0, got " + padByte);
                    }
                }
            } else if (padding > 0) {
                throw new KmipParseException("Insufficient data for padding after tag " + tagString);
            }
        }
        return structure;
    }

    /**
     * Parses a single value based on its type.
     */
    private Object parseValue(byte type, ByteBuffer valueBuffer) throws KmipParseException {
        if (!valueBuffer.hasRemaining() && type != TYPE_STRUCTURE) { // Allow empty structures
             // Minor types like Integer, Enum, Boolean etc shouldn't have zero length according to spec
             System.err.println("Warning: Zero-length value encountered for non-structure type: " + String.format("0x%02X", type));
             // Depending on strictness, could throw exception or return null/default.
             // Let's return null for now to avoid adding empty fields.
             return null; 
        }
        
        switch (type) {
            case TYPE_STRUCTURE:
                return parseStructure(valueBuffer);
            case TYPE_INTEGER:
                if (valueBuffer.remaining() >= 4) return valueBuffer.getInt();
                else throw new KmipParseException("Insufficient data for INTEGER: " + valueBuffer.remaining());
            case TYPE_ENUMERATION:
                 if (valueBuffer.remaining() >= 4) return valueBuffer.getInt();
                 else throw new KmipParseException("Insufficient data for ENUMERATION: " + valueBuffer.remaining());
            case TYPE_BOOLEAN:
                if (valueBuffer.remaining() >= 8) {
                    byte[] booleanBytes = new byte[8]; valueBuffer.get(booleanBytes);
                    return (booleanBytes[7] & 0x01) != 0;
                } else throw new KmipParseException("Insufficient data for BOOLEAN: " + valueBuffer.remaining());
            case TYPE_TEXT_STRING:
                byte[] stringBytes = new byte[valueBuffer.remaining()]; valueBuffer.get(stringBytes);
                return new String(stringBytes); 
            case TYPE_BYTE_STRING:
                 byte[] byteStringBytes = new byte[valueBuffer.remaining()]; valueBuffer.get(byteStringBytes);
                return byteStringBytes;
            default:
                 System.err.println("Warning: Unhandled KMIP type: " + String.format("0x%02X", type) + ". Returning raw bytes.");
                byte[] rawBytes = new byte[valueBuffer.remaining()]; valueBuffer.get(rawBytes);
                return rawBytes;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString(); // No trim or space needed for internal tag representation
    }

    // Custom exception for parsing errors
    public static class KmipParseException extends Exception {
        public KmipParseException(String message) {
            super(message);
        }
    }
}