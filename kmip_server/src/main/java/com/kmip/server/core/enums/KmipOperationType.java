package com.kmip.server.core.enums;

/**
 * Enumeration of KMIP operation types as defined in KMIP 2.0 specification.
 * 
 * Reference: OASIS KMIP 2.0 Specification, Section 6.1.9 - Operation Enumeration
 * https://docs.oasis-open.org/kmip/kmip-spec/v2.0/os/kmip-spec-v2.0-os.html
 */
public enum KmipOperationType {
    
    // Core Operations (KMIP 1.0)
    CREATE(0x000001, "Create"),
    CREATE_KEY_PAIR(0x000002, "Create Key Pair"),
    REGISTER(0x000003, "Register"),
    REKEY(0x000004, "Rekey"),
    DERIVE_KEY(0x000005, "Derive Key"),
    CERTIFY(0x000006, "Certify"),
    RECERTIFY(0x000007, "Recertify"),
    LOCATE(0x000008, "Locate"),
    CHECK(0x000009, "Check"),
    GET(0x00000A, "Get"),
    GET_ATTRIBUTES(0x00000B, "Get Attributes"),
    GET_ATTRIBUTE_LIST(0x00000C, "Get Attribute List"),
    ADD_ATTRIBUTE(0x00000D, "Add Attribute"),
    MODIFY_ATTRIBUTE(0x00000E, "Modify Attribute"),
    DELETE_ATTRIBUTE(0x00000F, "Delete Attribute"),
    OBTAIN_LEASE(0x000010, "Obtain Lease"),
    GET_USAGE_ALLOCATION(0x000011, "Get Usage Allocation"),
    ACTIVATE(0x000012, "Activate"),
    REVOKE(0x000013, "Revoke"),
    DESTROY(0x000014, "Destroy"),
    ARCHIVE(0x000015, "Archive"),
    RECOVER(0x000016, "Recover"),
    VALIDATE(0x000017, "Validate"),
    QUERY(0x000018, "Query"),
    CANCEL(0x000019, "Cancel"),
    POLL(0x00001A, "Poll"),
    NOTIFY(0x00001B, "Notify"),
    PUT(0x00001C, "Put"),
    
    // Extended Operations (KMIP 1.1+)
    REKEY_KEY_PAIR(0x00001D, "Rekey Key Pair"),
    DISCOVER_VERSIONS(0x00001E, "Discover Versions"),
    
    // Cryptographic Operations (KMIP 1.2+)
    ENCRYPT(0x00001F, "Encrypt"),
    DECRYPT(0x000020, "Decrypt"),
    SIGN(0x000021, "Sign"),
    SIGNATURE_VERIFY(0x000022, "Signature Verify"),
    MAC(0x000023, "MAC"),
    MAC_VERIFY(0x000024, "MAC Verify"),
    RNG_RETRIEVE(0x000025, "RNG Retrieve"),
    RNG_SEED(0x000026, "RNG Seed"),
    HASH(0x000027, "Hash"),
    CREATE_SPLIT_KEY(0x000028, "Create Split Key"),
    JOIN_SPLIT_KEY(0x000029, "Join Split Key"),
    
    // Import/Export Operations (KMIP 1.4+)
    IMPORT(0x00002A, "Import"),
    EXPORT(0x00002B, "Export"),
    
    // Advanced Operations (KMIP 2.0+)
    LOG(0x00002C, "Log"),
    LOGIN(0x00002D, "Login"),
    LOGOUT(0x00002E, "Logout"),
    DELEGATED_LOGIN(0x00002F, "Delegated Login"),
    ADJUST_ATTRIBUTE(0x000030, "Adjust Attribute"),
    SET_ATTRIBUTE(0x000031, "Set Attribute"),
    SET_ENDPOINT_ROLE(0x000032, "Set Endpoint Role"),
    PKCS11(0x000033, "PKCS#11"),
    INTEROP(0x000034, "Interop"),
    REPROVISION(0x000035, "Reprovision"),
    
    // Unknown operation for error handling
    UNKNOWN(0xFFFFFF, "Unknown");
    
    private final int code;
    private final String displayName;
    
    KmipOperationType(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    /**
     * Gets the KMIP operation code as defined in the specification.
     * 
     * @return The operation code
     */
    public int getCode() {
        return code;
    }
    
    /**
     * Gets the human-readable display name for the operation.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the operation code as a byte value.
     * 
     * @return The operation code as byte
     */
    public byte getByteCode() {
        return (byte) code;
    }
    
    /**
     * Gets the operation code as a hexadecimal string.
     * 
     * @return The operation code in hex format
     */
    public String getHexCode() {
        return String.format("0x%06X", code);
    }
    
    /**
     * Finds the operation type by its code.
     * 
     * @param code The operation code to look up
     * @return The corresponding KmipOperationType, or UNKNOWN if not found
     */
    public static KmipOperationType fromCode(int code) {
        for (KmipOperationType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * Finds the operation type by its byte code.
     * 
     * @param byteCode The operation byte code to look up
     * @return The corresponding KmipOperationType, or UNKNOWN if not found
     */
    public static KmipOperationType fromByteCode(byte byteCode) {
        return fromCode(byteCode & 0xFF);
    }
    
    /**
     * Checks if this operation is a core KMIP operation (defined in KMIP 1.0).
     * 
     * @return true if this is a core operation, false otherwise
     */
    public boolean isCoreOperation() {
        return code >= 0x000001 && code <= 0x00001C;
    }
    
    /**
     * Checks if this operation is a cryptographic operation.
     * 
     * @return true if this is a cryptographic operation, false otherwise
     */
    public boolean isCryptographicOperation() {
        return code >= 0x00001F && code <= 0x000029;
    }
    
    /**
     * Checks if this operation is supported by the current server implementation.
     * 
     * @return true if supported, false otherwise
     */
    public boolean isSupported() {
        switch (this) {
            case CREATE:
            case GET:
            case DESTROY:
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, getHexCode());
    }
}
