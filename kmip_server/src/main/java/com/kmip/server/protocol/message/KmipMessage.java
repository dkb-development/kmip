package com.kmip.server.protocol.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a KMIP message with fields.
 * 
 * This class is used to build and manipulate KMIP messages in a structured way
 * before encoding them to the TTLV format or after decoding from the TTLV format.
 */
public class KmipMessage {
    private final Map<Integer, List<Object>> fields = new HashMap<>();

    /**
     * Adds a field to the message.
     *
     * @param tag The tag of the field
     * @param value The value of the field
     */
    public void addField(int tag, Object value) {
        fields.computeIfAbsent(tag, k -> new ArrayList<>()).add(value);
    }

    /**
     * Gets the fields of the message.
     *
     * @return The fields of the message
     */
    public Map<Integer, List<Object>> getFields() {
        return fields;
    }

    /**
     * Gets the first value of a field.
     *
     * @param tag The tag of the field
     * @return The first value of the field, or null if the field doesn't exist
     */
    public Object getFieldValue(int tag) {
        List<Object> values = fields.get(tag);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    /**
     * Gets all values of a field.
     *
     * @param tag The tag of the field
     * @return The values of the field, or an empty list if the field doesn't exist
     */
    public List<Object> getFieldValues(int tag) {
        return fields.getOrDefault(tag, new ArrayList<>());
    }

    /**
     * Checks if a field exists.
     *
     * @param tag The tag of the field
     * @return True if the field exists, false otherwise
     */
    public boolean hasField(int tag) {
        return fields.containsKey(tag);
    }

    /**
     * Removes a field.
     *
     * @param tag The tag of the field
     */
    public void removeField(int tag) {
        fields.remove(tag);
    }

    /**
     * Clears all fields.
     */
    public void clear() {
        fields.clear();
    }

    /**
     * Returns a string representation of the message.
     *
     * @return A string representation of the message
     */
    @Override
    public String toString() {
        return "KmipMessage{" +
                "fields=" + fields +
                '}';
    }
}
