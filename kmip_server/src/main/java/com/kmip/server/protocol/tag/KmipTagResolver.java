package com.kmip.server.protocol.tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for resolving KMIP tags.
 * 
 * This class provides constants and utility methods for working with KMIP tags.
 */
public class KmipTagResolver {
    // Protocol Version
    public static final int TAG_PROTOCOL_VERSION = 0x420069;
    public static final int TAG_PROTOCOL_VERSION_MAJOR = 0x42006A;
    public static final int TAG_PROTOCOL_VERSION_MINOR = 0x42006B;
    
    // Message Structure
    public static final int TAG_OPERATION = 0x42005C;
    public static final int TAG_RESULT_STATUS = 0x42007F;
    public static final int TAG_RESULT_REASON = 0x42007E;
    public static final int TAG_RESULT_MESSAGE = 0x42007D;
    public static final int TAG_BATCH_COUNT = 0x42000D;
    public static final int TAG_BATCH_ITEM = 0x42000F;
    public static final int TAG_RESPONSE_HEADER = 0x42007A;
    public static final int TAG_REQUEST_HEADER = 0x420077;
    public static final int TAG_RESPONSE_MESSAGE = 0x42007B;
    public static final int TAG_REQUEST_MESSAGE = 0x420078;
    public static final int TAG_RESPONSE_PAYLOAD = 0x42007C;
    public static final int TAG_REQUEST_PAYLOAD = 0x420079;
    public static final int TAG_RESPONSE_BATCH_ITEM = 0x42000F;
    public static final int TAG_REQUEST_BATCH_ITEM = 0x42000F;
    public static final int TAG_TIME_STAMP = 0x420092;
    
    // Object Types
    public static final int TAG_OBJECT_TYPE = 0x420057;
    public static final int TAG_UNIQUE_IDENTIFIER = 0x420094;
    public static final int TAG_SYMMETRIC_KEY = 0x42008F;
    public static final int TAG_TEMPLATE_ATTRIBUTE = 0x420091;
    
    // Key Block
    public static final int TAG_KEY_BLOCK = 0x420040;
    public static final int TAG_KEY_FORMAT_TYPE = 0x420042;
    public static final int TAG_KEY_VALUE = 0x420045;
    public static final int TAG_KEY_MATERIAL = 0x420043;
    public static final int TAG_CRYPTOGRAPHIC_ALGORITHM = 0x420028;
    public static final int TAG_CRYPTOGRAPHIC_LENGTH = 0x42002A;
    public static final int TAG_CRYPTOGRAPHIC_USAGE_MASK = 0x42002B;
    
    // Attributes
    public static final int TAG_ATTRIBUTE = 0x420008;
    public static final int TAG_ATTRIBUTE_NAME = 0x42000A;
    public static final int TAG_ATTRIBUTE_VALUE = 0x42000B;
    
    // Private static maps for tag name and enumeration lookup
    private static final Map<Integer, String> TAG_NAMES = new HashMap<>();
    private static final Set<Integer> ENUMERATION_TAGS = Set.of(
        TAG_OBJECT_TYPE,
        TAG_OPERATION,
        TAG_RESULT_STATUS,
        TAG_RESULT_REASON,
        TAG_KEY_FORMAT_TYPE,
        TAG_CRYPTOGRAPHIC_ALGORITHM,
        TAG_CRYPTOGRAPHIC_USAGE_MASK
    );
    
    static {
        // Initialize tag names
        TAG_NAMES.put(TAG_PROTOCOL_VERSION, "Protocol Version");
        TAG_NAMES.put(TAG_PROTOCOL_VERSION_MAJOR, "Protocol Version Major");
        TAG_NAMES.put(TAG_PROTOCOL_VERSION_MINOR, "Protocol Version Minor");
        TAG_NAMES.put(TAG_OPERATION, "Operation");
        TAG_NAMES.put(TAG_RESULT_STATUS, "Result Status");
        TAG_NAMES.put(TAG_RESULT_REASON, "Result Reason");
        TAG_NAMES.put(TAG_RESULT_MESSAGE, "Result Message");
        TAG_NAMES.put(TAG_BATCH_COUNT, "Batch Count");
        TAG_NAMES.put(TAG_BATCH_ITEM, "Batch Item");
        TAG_NAMES.put(TAG_RESPONSE_HEADER, "Response Header");
        TAG_NAMES.put(TAG_REQUEST_HEADER, "Request Header");
        TAG_NAMES.put(TAG_RESPONSE_MESSAGE, "Response Message");
        TAG_NAMES.put(TAG_REQUEST_MESSAGE, "Request Message");
        TAG_NAMES.put(TAG_RESPONSE_PAYLOAD, "Response Payload");
        TAG_NAMES.put(TAG_REQUEST_PAYLOAD, "Request Payload");
        TAG_NAMES.put(TAG_RESPONSE_BATCH_ITEM, "Response Batch Item");
        TAG_NAMES.put(TAG_REQUEST_BATCH_ITEM, "Request Batch Item");
        TAG_NAMES.put(TAG_TIME_STAMP, "Time Stamp");
        TAG_NAMES.put(TAG_OBJECT_TYPE, "Object Type");
        TAG_NAMES.put(TAG_UNIQUE_IDENTIFIER, "Unique Identifier");
        TAG_NAMES.put(TAG_SYMMETRIC_KEY, "Symmetric Key");
        TAG_NAMES.put(TAG_TEMPLATE_ATTRIBUTE, "Template Attribute");
        TAG_NAMES.put(TAG_KEY_BLOCK, "Key Block");
        TAG_NAMES.put(TAG_KEY_FORMAT_TYPE, "Key Format Type");
        TAG_NAMES.put(TAG_KEY_VALUE, "Key Value");
        TAG_NAMES.put(TAG_KEY_MATERIAL, "Key Material");
        TAG_NAMES.put(TAG_CRYPTOGRAPHIC_ALGORITHM, "Cryptographic Algorithm");
        TAG_NAMES.put(TAG_CRYPTOGRAPHIC_LENGTH, "Cryptographic Length");
        TAG_NAMES.put(TAG_CRYPTOGRAPHIC_USAGE_MASK, "Cryptographic Usage Mask");
        TAG_NAMES.put(TAG_ATTRIBUTE, "Attribute");
        TAG_NAMES.put(TAG_ATTRIBUTE_NAME, "Attribute Name");
        TAG_NAMES.put(TAG_ATTRIBUTE_VALUE, "Attribute Value");
    }
    
    /**
     * Gets the name of a tag.
     *
     * @param tag The tag
     * @return The name of the tag, or "Unknown Tag" if the tag is not recognized
     */
    public static String getTagName(int tag) {
        return TAG_NAMES.getOrDefault(tag, "Unknown Tag (0x" + Integer.toHexString(tag) + ")");
    }
    
    /**
     * Checks if a tag represents an enumeration.
     *
     * @param tag The tag
     * @return True if the tag represents an enumeration, false otherwise
     */
    public static boolean isEnumeration(int tag) {
        return ENUMERATION_TAGS.contains(tag);
    }
}
