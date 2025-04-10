package com.kmip.server.protocol.message;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kmip.server.protocol.tag.KmipTagResolver;

public class KmipMessage {
    private static final Logger log = LoggerFactory.getLogger(KmipMessage.class);
    // Use Integer as key for tags
    private Map<Integer, List<Object>> fields;

    public KmipMessage() {
        this.fields = new LinkedHashMap<>();
    }

    /**
     * Adds a field (tag-value pair) to the message using an integer tag.
     * Handles multiple values for the same tag.
     */
    public void addField(int tag, Object value) {
        // Debug logging for field addition
        log.debug("Adding field - Tag: 0x{}, Value: {}, Value Type: {}", 
            Integer.toHexString(tag), value, value != null ? value.getClass().getSimpleName() : "null");
        
        // Ensure value is not null before adding
        if (value == null) {
             log.warn("Tried to add null value for tag: 0x{} ({})", Integer.toHexString(tag), KmipTagResolver.getTagName(tag));
             return; // Don't store nulls
        }
        
        // Use integer tag as key
        this.fields.computeIfAbsent(tag, k -> new ArrayList<>()).add(value);
        log.debug("Field added successfully - Tag: 0x{}, Total fields for tag: {}", 
            Integer.toHexString(tag), this.fields.get(tag).size());
    }

    /**
     * Gets the first value associated with a given integer tag.
     * Returns null if the tag is not present or has no values.
     */
    public Object getField(int tag) {
        List<Object> values = fields.get(tag);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
    
    /**
     * Gets all values associated with a given integer tag.
     * Returns an empty list if the tag is not present.
     */
     public List<Object> getAllFields(int tag) {
         // Use Collections.emptyList() for immutable empty list
         return fields.getOrDefault(tag, Collections.emptyList());
     }

    /**
     * Gets the underlying map of fields (Integer tags to List of Objects).
     * Note: Modifying the returned map directly is discouraged.
     */
    public Map<Integer, List<Object>> getFields() {
        return fields;
    }
    
    /**
     * Gets the first field value found within this message structure, 
     * attempting to cast it to the expected type.
     * Useful for KMIP structures that wrap a single value (like Attribute Value).
     *
     * @param expectedType The desired class of the value.
     * @return Optional containing the cast value if found and type matches, otherwise empty.
     */
     public <T> Optional<T> getFirstFieldValue(Class<T> expectedType) {
         if (fields.isEmpty()) {
             return Optional.empty();
         }
         // Get the first value from the first list encountered in the map's entry set
         Map.Entry<Integer, List<Object>> firstEntry = fields.entrySet().iterator().next();
         if (firstEntry.getValue() != null && !firstEntry.getValue().isEmpty()) {
             Object rawValue = firstEntry.getValue().get(0);
             if (expectedType.isInstance(rawValue)) {
                 return Optional.of(expectedType.cast(rawValue));
             } else {
                  System.err.printf("Warning: getFirstFieldValue expected %s but found %s for tag 0x%X (%s)%n",
                                      expectedType.getSimpleName(), 
                                      rawValue.getClass().getSimpleName(), 
                                      firstEntry.getKey(), // Key is now Integer
                                      KmipTagResolver.getTagName(firstEntry.getKey())); // Get name for log
             }
         }
         return Optional.empty();
     }
}