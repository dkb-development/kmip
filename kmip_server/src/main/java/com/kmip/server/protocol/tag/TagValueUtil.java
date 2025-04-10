package com.kmip.server.protocol.tag;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.kmip.server.protocol.message.KmipMessage;

public class TagValueUtil {

    // Define KMIP Tag constants (add more as needed)
    public static final String TAG_REQUEST_MESSAGE = "420078";
    public static final String TAG_REQUEST_HEADER = "420077";
    public static final String TAG_BATCH_ITEM = "42000F";
    public static final String TAG_OPERATION = "42005C";
    public static final String TAG_REQUEST_PAYLOAD = "420079";
    public static final String TAG_OBJECT_TYPE = "420057";
    public static final String TAG_TEMPLATE_ATTRIBUTE = "420091";
    public static final String TAG_ATTRIBUTE = "420008";
    public static final String TAG_ATTRIBUTE_NAME = "42000A";
    public static final String TAG_ATTRIBUTE_VALUE = "42000B";
    public static final String TAG_CRYPTOGRAPHIC_ALGORITHM = "420028";
    public static final String TAG_CRYPTOGRAPHIC_LENGTH = "42002A";
    public static final String TAG_CRYPTOGRAPHIC_USAGE_MASK = "42002C";
    public static final String TAG_UNIQUE_IDENTIFIER = "420094";
    // Add other relevant tags...

    /**
     * Safely retrieves a field from a KmipMessage as a specific type using integer tag.
     *
     * @param message The KmipMessage to extract from.
     * @param tag The INTEGER tag of the field to retrieve (from KmipTagResolver).
     * @param expectedType The expected Class of the value.
     * @return An Optional containing the value if found and of the correct type, otherwise empty.
     */
    public static <T> Optional<T> getFieldAs(KmipMessage message, int tag, Class<T> expectedType) {
        if (message == null) {
            System.err.println("DEBUG getFieldAs: Input message is null for tag 0x" + Integer.toHexString(tag));
            return Optional.empty();
        }
        // Pass INT tag directly to KmipMessage.getField
        Object value = message.getField(tag);
        if (value == null) {
            System.err.printf("DEBUG getFieldAs: Field with tag 0x%X ('%s') NOT FOUND in message.%n", tag, KmipTagResolver.getTagName(tag));
            return Optional.empty();
        }

        if (expectedType.isInstance(value)) {
            return Optional.of(expectedType.cast(value));
        } else {
            System.err.printf("Warning: Field with tag 0x%X ('%s') has unexpected type %s (expected %s)%n",
                tag, KmipTagResolver.getTagName(tag), value.getClass().getSimpleName(), expectedType.getSimpleName());
            return Optional.empty();
        }
    }

    /**
     * Extracts the Operation code from a Batch Item.
     */
    public static Optional<Integer> getOperation(KmipMessage batchItem) {
        return getFieldAs(batchItem, KmipTagResolver.TAG_OPERATION, Integer.class);
    }

    /**
     * Extracts the Object Type from a Request Payload.
     */
    public static Optional<Integer> getObjectType(KmipMessage requestPayload) {
        return getFieldAs(requestPayload, KmipTagResolver.TAG_OBJECT_TYPE, Integer.class);
    }

    /**
     * Extracts the Request Header from a Request Message.
     */
    public static Optional<KmipMessage> getRequestHeader(KmipMessage requestMessage) {
        return getFieldAs(requestMessage, KmipTagResolver.TAG_REQUEST_HEADER, KmipMessage.class);
    }

    /**
     * Extracts the Protocol Version structure from a Request Header.
     */
    public static Optional<KmipMessage> getProtocolVersionStructure(KmipMessage requestHeader) {
        return getFieldAs(requestHeader, KmipTagResolver.TAG_PROTOCOL_VERSION, KmipMessage.class);
    }

    /**
     * Extracts the (first) Batch Item from a Request/Response Message.
     * NOTE: Assumes only one batch item for simplicity now.
     */
    public static Optional<KmipMessage> getBatchItem(KmipMessage message) {
        // Check for Request Batch Item first, then Response Batch Item
        Optional<KmipMessage> reqBatchItem = getFieldAs(message, KmipTagResolver.TAG_REQUEST_BATCH_ITEM, KmipMessage.class);
        if (reqBatchItem.isPresent()) {
            return reqBatchItem;
        }
        return getFieldAs(message, KmipTagResolver.TAG_RESPONSE_BATCH_ITEM, KmipMessage.class);
    }

    /**
     * Extracts the Request Payload from a Batch Item.
     */
    public static Optional<KmipMessage> getRequestPayload(KmipMessage batchItem) {
        return getFieldAs(batchItem, KmipTagResolver.TAG_REQUEST_PAYLOAD, KmipMessage.class);
    }

    /**
     * Extracts the Template-Attribute structure from a Request Payload.
     */
    public static Optional<KmipMessage> getTemplateAttributeStructure(KmipMessage requestPayload) {
        return getFieldAs(requestPayload, KmipTagResolver.TAG_TEMPLATE_ATTRIBUTE, KmipMessage.class);
    }

    /**
     * Finds a specific Attribute structure within a Template-Attribute structure
     * by its Attribute Name TAG (e.g., KmipTagResolver.TAG_CRYPTOGRAPHIC_ALGORITHM).
     *
     * @param templateAttribute The Template-Attribute structure.
     * @param attributeNameTag The INTEGER tag representing the name of the attribute to find.
     * @return An Optional containing the Attribute structure if found.
     */
    public static Optional<KmipMessage> findAttributeByNameTag(KmipMessage templateAttribute, int attributeNameTag) {
        if (templateAttribute == null) return Optional.empty();
        String attributeName = KmipTagResolver.getTagName(attributeNameTag); // Still need name for comparison
        int attributeStructureTag = KmipTagResolver.TAG_ATTRIBUTE;

        // Get all Attribute structures using INT tag
        List<Object> attributes = templateAttribute.getAllFields(attributeStructureTag);
        for (Object attrObj : attributes) {
            if (attrObj instanceof KmipMessage) {
                KmipMessage attribute = (KmipMessage) attrObj;
                // Get the Attribute Name field (TextString) using its INT tag
                Optional<String> name = getFieldAs(attribute, KmipTagResolver.TAG_ATTRIBUTE_NAME, String.class);
                if (name.isPresent() && name.get().equalsIgnoreCase(attributeName)) {
                    return Optional.of(attribute);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the value part of a specific Attribute (retrieved via findAttributeByNameTag).
     *
     * @param attribute The Attribute structure.
     * @param expectedValueType The expected Class of the attribute's value.
     * @return An Optional containing the Attribute Value if found and of the correct type.
     */
    public static <T> Optional<T> getAttributeValue(KmipMessage attribute, Class<T> expectedValueType) {
        // Use the INT tag for Attribute Value
        return getFieldAs(attribute, KmipTagResolver.TAG_ATTRIBUTE_VALUE, expectedValueType);
    }

    /**
     * Helper to get Cryptographic Algorithm enumeration value from a Template-Attribute.
     */
    public static Optional<Integer> getCryptographicAlgorithm(KmipMessage templateAttribute) {
        return findAttributeByNameTag(templateAttribute, KmipTagResolver.TAG_CRYPTOGRAPHIC_ALGORITHM)
                .flatMap(attr -> getAttributeValue(attr, Integer.class));
    }

    /**
     * Helper to get Cryptographic Length integer value from a Template-Attribute.
     */
    public static Optional<Integer> getCryptographicLength(KmipMessage templateAttribute) {
        return findAttributeByNameTag(templateAttribute, KmipTagResolver.TAG_CRYPTOGRAPHIC_LENGTH)
                .flatMap(attr -> getAttributeValue(attr, Integer.class));
    }

    /**
     * Helper to get Cryptographic Usage Mask integer value from a Template-Attribute.
     */
    public static Optional<Integer> getCryptographicUsageMask(KmipMessage templateAttribute) {
        // Find the Attribute for Usage Mask
        return findAttributeByNameTag(templateAttribute, KmipTagResolver.TAG_CRYPTOGRAPHIC_USAGE_MASK)
                // Then get its value (which should be an Integer)
                .flatMap(attr -> getAttributeValue(attr, Integer.class));
    }

    /**
     * Extracts the Unique Identifier from a Request Payload.
     * Used primarily for Get, Destroy, and other operations that reference an existing object.
     *
     * @param requestPayload The Request Payload structure.
     * @return An Optional containing the Unique Identifier string if found.
     */
    public static Optional<String> getUniqueIdentifier(KmipMessage requestPayload) {
        return getFieldAs(requestPayload, KmipTagResolver.TAG_UNIQUE_IDENTIFIER, String.class);
    }

    // Add more helper methods as needed (e.g., for getting other Attributes, etc.)
}