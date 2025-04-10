package com.kmip.server.protocol.codec;

import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.KmipTagResolver;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

/**
 * Parser for KMIP messages.
 *
 * This class is responsible for parsing KMIP messages from the TTLV (Tag, Type, Length, Value)
 * format as specified in the KMIP protocol specification.
 *
 * The parser is stateless and thread-safe.
 */
@Component
public class KmipParser {

    private static final Logger log = LoggerFactory.getLogger(KmipParser.class);

    // TTLV Type constants
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
     * Parses a KMIP message from a byte array.
     *
     * @param data The byte array containing the KMIP message
     * @return The parsed KMIP message
     * @throws IOException If an error occurs during parsing
     */
    public KmipMessage parse(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);

        // Parse the message
        KmipMessage message = parseMessage(dis);

        // Check if there's any data left
        if (bais.available() > 0) {
            log.warn("Extra data found after parsing KMIP message: {} bytes", bais.available());
        }

        return message;
    }

    private KmipMessage parseMessage(DataInputStream dis) throws IOException {
        // Read the tag
        int tag = dis.readInt();
        log.debug("Parsing message with tag: 0x{}", Integer.toHexString(tag));

        // Read the type
        byte type = dis.readByte();

        // Read the length
        int length = dis.readInt();
        log.debug("Message type: 0x{}, length: {}", Integer.toHexString(type), length);

        // For structures, parse the structure
        if (type == TYPE_STRUCTURE) {
            return parseStructure(dis, tag, length);
        } else {
            // Skip this field, we're only interested in the top-level structure
            skipValue(dis, length);
            return new KmipMessage();
        }
    }

    private KmipMessage parseStructure(DataInputStream dis, int tag, int length) throws IOException {
        KmipMessage message = new KmipMessage();

        // Read the structure content
        byte[] content = new byte[length];
        dis.readFully(content);

        // Skip padding
        int padding = (8 - (length % 8)) % 8;
        if (padding > 0) {
            dis.skipBytes(padding);
        }

        // Parse the structure content
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        DataInputStream contentDis = new DataInputStream(bais);

        // Parse all fields in the structure
        while (bais.available() > 0) {
            parseField(contentDis, message);
        }

        return message;
    }

    private void parseField(DataInputStream dis, KmipMessage message) throws IOException {
        // Read the tag
        int tag = dis.readInt();

        // Read the type
        byte type = dis.readByte();

        // Read the length
        int length = dis.readInt();

        log.debug("Parsing field - Tag: 0x{}, Type: 0x{}, Length: {}",
            Integer.toHexString(tag), Integer.toHexString(type), length);

        // Parse the value based on the type
        Object value = parseValue(dis, type, length);

        // Add the field to the message
        message.addField(tag, value);

        // Skip padding
        int padding = (8 - (length % 8)) % 8;
        if (padding > 0) {
            dis.skipBytes(padding);
        }
    }

    private Object parseValue(DataInputStream dis, byte type, int length) throws IOException {
        switch (type) {
            case TYPE_STRUCTURE:
                return parseStructure(dis, 0, length);
            case TYPE_INTEGER:
                return dis.readInt();
            case TYPE_LONG_INTEGER:
                return dis.readLong();
            case TYPE_ENUMERATION:
                return dis.readInt();
            case TYPE_BOOLEAN:
                return dis.readLong() != 0;
            case TYPE_TEXT_STRING:
                byte[] textBytes = new byte[length];
                dis.readFully(textBytes);
                return new String(textBytes, StandardCharsets.UTF_8);
            case TYPE_BYTE_STRING:
                byte[] bytes = new byte[length];
                dis.readFully(bytes);
                return bytes;
            case TYPE_DATE_TIME:
                long seconds = dis.readLong();
                return Instant.ofEpochSecond(seconds);
            default:
                // Skip unknown types
                skipValue(dis, length);
                return null;
        }
    }

    private void skipValue(DataInputStream dis, int length) throws IOException {
        dis.skipBytes(length);

        // Skip padding
        int padding = (8 - (length % 8)) % 8;
        if (padding > 0) {
            dis.skipBytes(padding);
        }
    }
}
