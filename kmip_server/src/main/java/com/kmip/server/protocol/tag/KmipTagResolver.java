package com.kmip.server.protocol.tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.HashSet;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Basic Tag Resolver - expand as needed
public class KmipTagResolver {

    private static final Logger log = LoggerFactory.getLogger(KmipTagResolver.class);

    // --- Make Tags Public Static Final ---
    // Define known tags (expand this significantly based on required KMIP operations)
    public static final int TAG_REQUEST_MESSAGE = 0x420078;
    public static final int TAG_REQUEST_HEADER = 0x420077;
    public static final int TAG_REQUEST_BATCH_ITEM = 0x420076;
    public static final int TAG_OPERATION = 0x42005C;
    public static final int TAG_UNIQUE_BATCH_ITEM_ID = 0x420093;
    public static final int TAG_REQUEST_PAYLOAD = 0x420079;
    public static final int TAG_ATTRIBUTE = 0x420008;
    public static final int TAG_ATTRIBUTE_NAME = 0x42000A;
    public static final int TAG_ATTRIBUTE_VALUE = 0x42000B;
    public static final int TAG_OBJECT_TYPE = 0x420057;
    public static final int TAG_CRYPTOGRAPHIC_ALGORITHM = 0x420028;
    public static final int TAG_CRYPTOGRAPHIC_LENGTH = 0x42002A;
    public static final int TAG_CRYPTOGRAPHIC_USAGE_MASK = 0x42002B; // Added based on CreateOperationHandler usage
    public static final int TAG_TEMPLATE_ATTRIBUTE = 0x420091; // Added based on CreateOperationHandler usage
    public static final int TAG_PROTOCOL_VERSION = 0x420069;
    public static final int TAG_PROTOCOL_VERSION_MAJOR = 0x42006A;
    public static final int TAG_PROTOCOL_VERSION_MINOR = 0x42006B;

    // Response Tags
    public static final int TAG_RESPONSE_MESSAGE = 0x42007B;
    public static final int TAG_RESPONSE_HEADER = 0x42007A;
    public static final int TAG_TIMESTAMP = 0x420092;
    public static final int TAG_BATCH_COUNT = 0x42000D;
    public static final int TAG_RESPONSE_BATCH_ITEM = 0x42000F; // Updated to match KMIP 2.0 spec
    public static final int TAG_RESULT_STATUS = 0x42007F;
    public static final int TAG_RESULT_REASON = 0x42007E;
    public static final int TAG_RESULT_MESSAGE = 0x42007D;
    public static final int TAG_RESPONSE_PAYLOAD = 0x42007C; // Updated to match KMIP 2.0 spec
    public static final int TAG_UNIQUE_IDENTIFIER = 0x420094; // Updated to match KMIP 2.0 spec
    public static final int TAG_KEY_BLOCK = 0x420040; // Key Block for key material and attributes
    public static final int TAG_KEY_FORMAT_TYPE = 0x420042; // Key Format Type
    public static final int TAG_KEY_VALUE = 0x420045; // Key Value
    public static final int TAG_KEY_MATERIAL = 0x420043; // Key Material
    public static final int TAG_SYMMETRIC_KEY = 0x42008F; // Symmetric Key object (0x42008F in PyKMIP)
    // --- End Public Tags ---

    private static final Map<String, Integer> tagMap = createTagMap();
    private static final Map<Integer, String> nameMap = createNameMap(); // For reverse lookup if needed

    // Add a set of tags that are enumerations
    private static final HashSet<Integer> ENUMERATION_TAGS = new HashSet<>(Arrays.asList(
        TAG_OBJECT_TYPE,
        TAG_OPERATION,
        TAG_RESULT_STATUS,
        TAG_RESULT_REASON,
        TAG_CRYPTOGRAPHIC_ALGORITHM,
        TAG_CRYPTOGRAPHIC_USAGE_MASK,
        TAG_KEY_FORMAT_TYPE
    ));

    public static boolean isEnumerationTag(int tag) {
        boolean isEnum = ENUMERATION_TAGS.contains(tag);
        log.debug("Checking if tag 0x{} ({}) is enumeration: {}",
            Integer.toHexString(tag), getTagName(tag), isEnum);
        return isEnum;
    }

    private static Map<String, Integer> createTagMap() {
        Map<String, Integer> map = new HashMap<>();
        // Request related
        map.put("Request Message", TAG_REQUEST_MESSAGE);
        map.put("Request Header", TAG_REQUEST_HEADER);
        map.put("Request Batch Item", TAG_REQUEST_BATCH_ITEM);
        map.put("Operation", TAG_OPERATION);
        map.put("Unique Batch Item ID", TAG_UNIQUE_BATCH_ITEM_ID);
        map.put("Request Payload", TAG_REQUEST_PAYLOAD);
        map.put("Attribute", TAG_ATTRIBUTE);
        map.put("Attribute Name", TAG_ATTRIBUTE_NAME);
        map.put("Attribute Value", TAG_ATTRIBUTE_VALUE);
        map.put("Object Type", TAG_OBJECT_TYPE);
        map.put("Cryptographic Algorithm", TAG_CRYPTOGRAPHIC_ALGORITHM);
        map.put("Cryptographic Length", TAG_CRYPTOGRAPHIC_LENGTH);
        map.put("Cryptographic Usage Mask", TAG_CRYPTOGRAPHIC_USAGE_MASK);
        map.put("Template-Attribute", TAG_TEMPLATE_ATTRIBUTE); // Use correct name from spec
        map.put("Protocol Version", TAG_PROTOCOL_VERSION);
        map.put("Protocol Version Major", TAG_PROTOCOL_VERSION_MAJOR);
        map.put("Protocol Version Minor", TAG_PROTOCOL_VERSION_MINOR);

        // Response related (add corresponding names)
        map.put("Response Message", TAG_RESPONSE_MESSAGE);
        map.put("Response Header", TAG_RESPONSE_HEADER);
        map.put("Time Stamp", TAG_TIMESTAMP);
        map.put("Batch Count", TAG_BATCH_COUNT);
        map.put("Response Batch Item", TAG_RESPONSE_BATCH_ITEM);
        map.put("Result Status", TAG_RESULT_STATUS);
        map.put("Result Reason", TAG_RESULT_REASON);
        map.put("Result Message", TAG_RESULT_MESSAGE);
        map.put("Response Payload", TAG_RESPONSE_PAYLOAD);
        map.put("Unique Identifier", TAG_UNIQUE_IDENTIFIER);
        map.put("Protocol Version", TAG_PROTOCOL_VERSION);
        map.put("Protocol Version Major", TAG_PROTOCOL_VERSION_MAJOR);
        map.put("Protocol Version Minor", TAG_PROTOCOL_VERSION_MINOR);
        map.put("Key Block", TAG_KEY_BLOCK);
        map.put("Key Format Type", TAG_KEY_FORMAT_TYPE);
        map.put("Key Value", TAG_KEY_VALUE);
        map.put("Key Material", TAG_KEY_MATERIAL);
        map.put("Symmetric Key", TAG_SYMMETRIC_KEY);

        // ... Add all other necessary tags used by KmipParser and needed for KmipEncoder

        return Collections.unmodifiableMap(map);
    }

    private static Map<Integer, String> createNameMap() {
        Map<Integer, String> reverseMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {
            // Handle potential duplicate values if different names map to the same tag
            if (!reverseMap.containsKey(entry.getValue())) {
                reverseMap.put(entry.getValue(), entry.getKey());
            }
        }
        return Collections.unmodifiableMap(reverseMap);
    }

    public static int getTagValue(String tagName) {
        // First try to parse as hex string
        try {
            if (tagName.startsWith("0x")) {
                return Integer.parseInt(tagName.substring(2), 16);
            } else if (tagName.matches("[0-9A-Fa-f]+")) {
                return Integer.parseInt(tagName, 16);
            }
        } catch (NumberFormatException e) {
            // Not a valid hex string, continue with name lookup
        }

        // Try name lookup
        Integer tagValue = tagMap.get(tagName);
        if (tagValue == null) {
            // Try case-insensitive match as fallback
            for(Map.Entry<String, Integer> entry : tagMap.entrySet()){
                if(entry.getKey().equalsIgnoreCase(tagName)){
                    log.warn("Tag name '{}' matched case-insensitively to '{}'. Using tag 0x{}",
                            tagName, entry.getKey(), Integer.toHexString(entry.getValue()));
                    return entry.getValue();
                }
            }
            log.error("Unknown tag name encountered and no case-insensitive match found: {}", tagName);
            throw new IllegalArgumentException("Unknown tag name: " + tagName);
        }
        return tagValue;
    }

    public static String getTagName(int tagValue) {
        String tagName = nameMap.get(tagValue);
        if (tagName == null) {
            log.trace("Unknown tag value encountered: 0x{}", Integer.toHexString(tagValue));
            return "UnknownTag_0x" + Integer.toHexString(tagValue);
        }
        return tagName;
    }

    // Add helper methods to identify types if needed by the encoder
    public static boolean isLongIntegerTag(int tag) {
        // Add logic based on known LongInteger tags (like Timestamp if it's Long)
        // KMIP DateTime (Tag 0x420092, Type 0x09) is encoded as LongInteger
        return tag == TAG_TIMESTAMP;
    }

    // Add more specific type check methods if needed (isStructureTag, isByteStringTag, etc.)

}