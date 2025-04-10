package com.kmip.server.kmip;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Special encoder for PyKMIP compatibility
 * This encoder creates TTLV structures that are compatible with PyKMIP's expectations
 */
@Slf4j
@Component
public class PyKmipCompatEncoder {

    // TTLV Type constants
    private static final byte TYPE_STRUCTURE = 0x01;
    private static final byte TYPE_INTEGER = 0x02;
    private static final byte TYPE_ENUMERATION = 0x05;
    private static final byte TYPE_BYTE_STRING = 0x08;

    // Tag constants from KmipTagResolver
    private static final int TAG_OBJECT_TYPE = KmipTagResolver.TAG_OBJECT_TYPE;
    private static final int TAG_UNIQUE_IDENTIFIER = KmipTagResolver.TAG_UNIQUE_IDENTIFIER;
    private static final int TAG_SYMMETRIC_KEY = KmipTagResolver.TAG_SYMMETRIC_KEY;
    private static final int TAG_KEY_BLOCK = KmipTagResolver.TAG_KEY_BLOCK;
    private static final int TAG_KEY_FORMAT_TYPE = KmipTagResolver.TAG_KEY_FORMAT_TYPE;
    private static final int TAG_KEY_VALUE = KmipTagResolver.TAG_KEY_VALUE;
    private static final int TAG_KEY_MATERIAL = KmipTagResolver.TAG_KEY_MATERIAL;
    private static final int TAG_CRYPTOGRAPHIC_ALGORITHM = KmipTagResolver.TAG_CRYPTOGRAPHIC_ALGORITHM;
    private static final int TAG_CRYPTOGRAPHIC_LENGTH = KmipTagResolver.TAG_CRYPTOGRAPHIC_LENGTH;
    private static final int TAG_CRYPTOGRAPHIC_USAGE_MASK = KmipTagResolver.TAG_CRYPTOGRAPHIC_USAGE_MASK;

    /**
     * Creates a TTLV-encoded byte array for a Get operation response that is compatible with PyKMIP
     * 
     * @param objectType The object type (e.g., 2 for Symmetric Key)
     * @param uniqueIdentifier The unique identifier of the key
     * @param keyMaterial The key material (raw bytes)
     * @param cryptoAlgorithm The cryptographic algorithm (e.g., 3 for AES)
     * @param cryptoLength The cryptographic length in bits
     * @param cryptoUsageMask The cryptographic usage mask (e.g., 12 for Encrypt | Decrypt)
     * @return A TTLV-encoded byte array for the response payload
     * @throws IOException If an error occurs during encoding
     */
    public byte[] createGetResponsePayload(
            int objectType,
            String uniqueIdentifier,
            byte[] keyMaterial,
            int cryptoAlgorithm,
            int cryptoLength,
            int cryptoUsageMask) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // Create the response payload structure
        // 1. Object Type (Required)
        encodeEnumeration(dos, TAG_OBJECT_TYPE, objectType);
        
        // 2. Unique Identifier (Required)
        encodeTextString(dos, TAG_UNIQUE_IDENTIFIER, uniqueIdentifier);
        
        // 3. Symmetric Key (Required)
        // Create the Symmetric Key structure
        ByteArrayOutputStream symmetricKeyBaos = new ByteArrayOutputStream();
        DataOutputStream symmetricKeyDos = new DataOutputStream(symmetricKeyBaos);
        
        // Create the Key Block structure
        ByteArrayOutputStream keyBlockBaos = new ByteArrayOutputStream();
        DataOutputStream keyBlockDos = new DataOutputStream(keyBlockBaos);
        
        // 1. Key Format Type (Required)
        encodeEnumeration(keyBlockDos, TAG_KEY_FORMAT_TYPE, 1); // Raw format
        
        // 2. Key Value (Required)
        // Create the Key Value structure
        ByteArrayOutputStream keyValueBaos = new ByteArrayOutputStream();
        DataOutputStream keyValueDos = new DataOutputStream(keyValueBaos);
        
        // Key Material (Required)
        encodeByteString(keyValueDos, TAG_KEY_MATERIAL, keyMaterial);
        
        // Add Key Value to Key Block
        encodeStructure(keyBlockDos, TAG_KEY_VALUE, keyValueBaos.toByteArray());
        
        // 3. Cryptographic Algorithm (Required)
        encodeEnumeration(keyBlockDos, TAG_CRYPTOGRAPHIC_ALGORITHM, cryptoAlgorithm);
        
        // 4. Cryptographic Length (Required)
        encodeInteger(keyBlockDos, TAG_CRYPTOGRAPHIC_LENGTH, cryptoLength);
        
        // 5. Cryptographic Usage Mask (Required)
        encodeEnumeration(keyBlockDos, TAG_CRYPTOGRAPHIC_USAGE_MASK, cryptoUsageMask);
        
        // Add Key Block to Symmetric Key
        encodeStructure(symmetricKeyDos, TAG_KEY_BLOCK, keyBlockBaos.toByteArray());
        
        // Add Symmetric Key to Response Payload
        encodeStructure(dos, TAG_SYMMETRIC_KEY, symmetricKeyBaos.toByteArray());
        
        return baos.toByteArray();
    }
    
    private void encodeTag(DataOutputStream dos, int tag) throws IOException {
        dos.writeInt(tag);
    }
    
    private void encodeType(DataOutputStream dos, byte type) throws IOException {
        dos.writeByte(type);
    }
    
    private void encodeLength(DataOutputStream dos, int length) throws IOException {
        dos.writeInt(length);
    }
    
    private void encodeValue(DataOutputStream dos, byte[] value) throws IOException {
        dos.write(value);
        
        // Add padding if needed
        int padding = (8 - (value.length % 8)) % 8;
        if (padding > 0) {
            byte[] paddingBytes = new byte[padding];
            dos.write(paddingBytes);
        }
    }
    
    private void encodeStructure(DataOutputStream dos, int tag, byte[] value) throws IOException {
        encodeTag(dos, tag);
        encodeType(dos, TYPE_STRUCTURE);
        encodeLength(dos, value.length);
        encodeValue(dos, value);
    }
    
    private void encodeInteger(DataOutputStream dos, int tag, int value) throws IOException {
        encodeTag(dos, tag);
        encodeType(dos, TYPE_INTEGER);
        encodeLength(dos, 4); // Integer is always 4 bytes
        
        // Write the integer value
        dos.writeInt(value);
        
        // Add padding (4 bytes for integers)
        byte[] padding = new byte[4];
        dos.write(padding);
    }
    
    private void encodeEnumeration(DataOutputStream dos, int tag, int value) throws IOException {
        encodeTag(dos, tag);
        encodeType(dos, TYPE_ENUMERATION);
        encodeLength(dos, 4); // Enumeration is always 4 bytes
        
        // Write the enumeration value
        dos.writeInt(value);
        
        // Add padding (4 bytes for enumerations)
        byte[] padding = new byte[4];
        dos.write(padding);
    }
    
    private void encodeTextString(DataOutputStream dos, int tag, String value) throws IOException {
        byte[] bytes = value.getBytes("UTF-8");
        
        encodeTag(dos, tag);
        encodeType(dos, (byte)0x07); // Text String
        encodeLength(dos, bytes.length);
        encodeValue(dos, bytes);
    }
    
    private void encodeByteString(DataOutputStream dos, int tag, byte[] value) throws IOException {
        encodeTag(dos, tag);
        encodeType(dos, TYPE_BYTE_STRING);
        encodeLength(dos, value.length);
        encodeValue(dos, value);
    }
    
    /**
     * Converts a byte array to a hexadecimal string for debugging
     */
    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xff));
            if (sb.length() % 48 == 0) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
