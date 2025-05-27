package com.kmip.server.core.enums;

/**
 * KMIP Result Reason Enumeration.
 * 
 * This enum defines all possible result reason values for KMIP operations
 * as specified in the KMIP 2.0 specification. The result reason provides
 * additional detail about why an operation succeeded or failed.
 * 
 * Reference: KMIP 2.0 Specification, Table 408 (Result Reason Enumeration)
 */
public enum KmipResultReason {
    
    // General reasons
    GENERAL_FAILURE(0x000100, "General Failure"),
    ITEM_NOT_FOUND(0x000101, "Item Not Found"),
    RESPONSE_TOO_LARGE(0x000102, "Response Too Large"),
    AUTHENTICATION_NOT_SUCCESSFUL(0x000103, "Authentication Not Successful"),
    INVALID_MESSAGE(0x000104, "Invalid Message"),
    OPERATION_NOT_SUPPORTED(0x000105, "Operation Not Supported"),
    MISSING_DATA(0x000106, "Missing Data"),
    INVALID_FIELD(0x000107, "Invalid Field"),
    FEATURE_NOT_SUPPORTED(0x000108, "Feature Not Supported"),
    OPERATION_CANCELED_BY_REQUESTER(0x000109, "Operation Canceled by Requester"),
    CRYPTOGRAPHIC_FAILURE(0x00010A, "Cryptographic Failure"),
    ILLEGAL_OPERATION(0x00010B, "Illegal Operation"),
    PERMISSION_DENIED(0x00010C, "Permission Denied"),
    OBJECT_ARCHIVED(0x00010D, "Object Archived"),
    INDEX_OUT_OF_BOUNDS(0x00010E, "Index Out of Bounds"),
    APPLICATION_NAMESPACE_NOT_SUPPORTED(0x00010F, "Application Namespace Not Supported"),
    KEY_FORMAT_TYPE_NOT_SUPPORTED(0x000110, "Key Format Type Not Supported"),
    KEY_COMPRESSION_TYPE_NOT_SUPPORTED(0x000111, "Key Compression Type Not Supported"),
    ENCODING_OPTION_ERROR(0x000112, "Encoding Option Error"),
    KEY_VALUE_NOT_PRESENT(0x000113, "Key Value Not Present"),
    ATTESTATION_REQUIRED(0x000114, "Attestation Required"),
    ATTESTATION_FAILED(0x000115, "Attestation Failed"),
    SENSITIVE(0x000116, "Sensitive"),
    NOT_EXTRACTABLE(0x000117, "Not Extractable"),
    OBJECT_GROUP_MEMBER(0x000118, "Object Group Member"),
    TAG_NOT_SUPPORTED(0x000119, "Tag Not Supported"),
    OPERATION_NOT_ATTEMPTED(0x00011A, "Operation Not Attempted"),
    MAX_POLICY_TEMPLATE_SIZE_EXCEEDED(0x00011B, "Max Policy Template Size Exceeded"),
    INVALID_POLICY_TEMPLATE(0x00011C, "Invalid Policy Template"),
    OPERATION_NOT_POSSIBLE(0x00011D, "Operation Not Possible"),
    STORAGE_LIMITATION(0x00011E, "Storage Limitation"),
    UNSUITABLE_ALTERNATE_CERTIFICATE(0x00011F, "Unsuitable Alternate Certificate"),
    
    // Object state related reasons
    OBJECT_EXISTS(0x000200, "Object Exists"),
    OBJECT_NOT_FOUND(0x000201, "Object Not Found"),
    OBJECT_DESTROYED(0x000202, "Object Destroyed"),
    OBJECT_NOT_DESTROYED(0x000203, "Object Not Destroyed"),
    OBJECT_COMPROMISED(0x000204, "Object Compromised"),
    OBJECT_NOT_COMPROMISED(0x000205, "Object Not Compromised"),
    OBJECT_REVOKED(0x000206, "Object Revoked"),
    OBJECT_NOT_REVOKED(0x000207, "Object Not Revoked"),
    OBJECT_ACTIVATED(0x000208, "Object Activated"),
    OBJECT_NOT_ACTIVATED(0x000209, "Object Not Activated"),
    OBJECT_DEACTIVATED(0x00020A, "Object Deactivated"),
    OBJECT_NOT_DEACTIVATED(0x00020B, "Object Not Deactivated"),
    OBJECT_ARCHIVED_BY_DATE(0x00020C, "Object Archived by Date"),
    OBJECT_ARCHIVED_BY_REQUESTER(0x00020D, "Object Archived by Requester"),
    
    // Attribute related reasons
    ATTRIBUTE_NOT_FOUND(0x000300, "Attribute Not Found"),
    ATTRIBUTE_NOT_SUPPORTED(0x000301, "Attribute Not Supported"),
    ATTRIBUTE_READ_ONLY(0x000302, "Attribute Read Only"),
    ATTRIBUTE_SINGLE_VALUED(0x000303, "Attribute Single Valued"),
    BAD_CRYPTOGRAPHIC_DOMAINPARAMETERS(0x000304, "Bad Cryptographic Domain Parameters"),
    BAD_CRYPTOGRAPHIC_PARAMETERS(0x000305, "Bad Cryptographic Parameters"),
    INVALID_ATTRIBUTE(0x000306, "Invalid Attribute"),
    INVALID_ATTRIBUTE_VALUE(0x000307, "Invalid Attribute Value"),
    MULTIVALUED_ATTRIBUTE(0x000308, "Multivalued Attribute"),
    UNSUPPORTED_ATTRIBUTE(0x000309, "Unsupported Attribute"),
    
    // Cryptographic reasons
    INVALID_CRYPTOGRAPHIC_ALGORITHM(0x000400, "Invalid Cryptographic Algorithm"),
    INVALID_CRYPTOGRAPHIC_DOMAIN_PARAMETERS(0x000401, "Invalid Cryptographic Domain Parameters"),
    INVALID_CRYPTOGRAPHIC_PARAMETERS(0x000402, "Invalid Cryptographic Parameters"),
    CRYPTOGRAPHIC_DOMAIN_PARAMETERS_NOT_SUPPORTED(0x000403, "Cryptographic Domain Parameters Not Supported"),
    CRYPTOGRAPHIC_PARAMETERS_NOT_SUPPORTED(0x000404, "Cryptographic Parameters Not Supported"),
    CRYPTOGRAPHIC_ALGORITHM_NOT_SUPPORTED(0x000405, "Cryptographic Algorithm Not Supported"),
    INVALID_KEY_LENGTH(0x000406, "Invalid Key Length"),
    KEY_LENGTH_NOT_SUPPORTED(0x000407, "Key Length Not Supported"),
    
    // Unknown reason for error handling
    UNKNOWN(0xFFFFFF, "Unknown");
    
    private final int code;
    private final String displayName;
    
    /**
     * Constructor for KMIP Result Reason enum.
     * 
     * @param code the reason code
     * @param displayName the human-readable name
     */
    KmipResultReason(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    /**
     * Gets the reason code.
     * 
     * @return the reason code
     */
    public int getCode() {
        return code;
    }
    
    /**
     * Gets the human-readable display name.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the reason code as hexadecimal string.
     * 
     * @return the reason code in hex format
     */
    public String getHexCode() {
        return String.format("0x%06X", code);
    }
    
    /**
     * Finds the result reason by its code.
     * 
     * @param code the reason code to look up
     * @return the corresponding KmipResultReason, or UNKNOWN if not found
     */
    public static KmipResultReason fromCode(int code) {
        for (KmipResultReason reason : values()) {
            if (reason.code == code) {
                return reason;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * Checks if this reason indicates a general failure.
     * 
     * @return true if general failure category
     */
    public boolean isGeneralFailure() {
        return code >= 0x000100 && code < 0x000200;
    }
    
    /**
     * Checks if this reason is related to object state.
     * 
     * @return true if object state related
     */
    public boolean isObjectStateRelated() {
        return code >= 0x000200 && code < 0x000300;
    }
    
    /**
     * Checks if this reason is related to attributes.
     * 
     * @return true if attribute related
     */
    public boolean isAttributeRelated() {
        return code >= 0x000300 && code < 0x000400;
    }
    
    /**
     * Checks if this reason is related to cryptography.
     * 
     * @return true if cryptographic related
     */
    public boolean isCryptographicRelated() {
        return code >= 0x000400 && code < 0x000500;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, getHexCode());
    }
}
