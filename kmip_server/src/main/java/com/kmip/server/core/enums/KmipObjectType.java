package com.kmip.server.core.enums;

/**
 * KMIP Object Type Enumeration.
 * 
 * This enum defines all KMIP object types as specified in the KMIP 2.0 specification.
 * Object types identify the kind of cryptographic object being managed.
 * 
 * Reference: KMIP 2.0 Specification, Table 405 (Object Type Enumeration)
 */
public enum KmipObjectType {
    
    /**
     * Certificate object - X.509 certificates and other certificate formats.
     */
    CERTIFICATE(0x000001, "Certificate"),
    
    /**
     * Symmetric Key object - Keys used for symmetric cryptographic operations.
     */
    SYMMETRIC_KEY(0x000002, "Symmetric Key"),
    
    /**
     * Public Key object - Public keys from asymmetric key pairs.
     */
    PUBLIC_KEY(0x000003, "Public Key"),
    
    /**
     * Private Key object - Private keys from asymmetric key pairs.
     */
    PRIVATE_KEY(0x000004, "Private Key"),
    
    /**
     * Split Key object - Keys that have been split for security purposes.
     */
    SPLIT_KEY(0x000005, "Split Key"),
    
    /**
     * Template object - Attribute templates for object creation.
     */
    TEMPLATE(0x000006, "Template"),
    
    /**
     * Secret Data object - Generic secret data that is not a key.
     */
    SECRET_DATA(0x000007, "Secret Data"),
    
    /**
     * Opaque Data object - Data that the server treats as opaque.
     */
    OPAQUE_DATA(0x000008, "Opaque Data"),
    
    /**
     * PGP Key object - Pretty Good Privacy (PGP) keys.
     */
    PGP_KEY(0x000009, "PGP Key"),
    
    /**
     * Certificate Request object - Certificate signing requests.
     */
    CERTIFICATE_REQUEST(0x00000A, "Certificate Request"),
    
    /**
     * Unknown object type for error handling.
     */
    UNKNOWN(0xFFFFFF, "Unknown");
    
    private final int code;
    private final String displayName;
    
    /**
     * Constructor for KMIP Object Type enum.
     * 
     * @param code the object type code
     * @param displayName the human-readable name
     */
    KmipObjectType(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    /**
     * Gets the object type code.
     * 
     * @return the type code
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
     * Gets the type code as hexadecimal string.
     * 
     * @return the type code in hex format
     */
    public String getHexCode() {
        return String.format("0x%06X", code);
    }
    
    /**
     * Finds the object type by its code.
     * 
     * @param code the type code to look up
     * @return the corresponding KmipObjectType, or UNKNOWN if not found
     */
    public static KmipObjectType fromCode(int code) {
        for (KmipObjectType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * Checks if this object type represents a cryptographic key.
     * 
     * @return true if this is a key type
     */
    public boolean isKeyType() {
        return this == SYMMETRIC_KEY || this == PUBLIC_KEY || this == PRIVATE_KEY || 
               this == SPLIT_KEY || this == PGP_KEY;
    }
    
    /**
     * Checks if this object type represents a certificate.
     * 
     * @return true if this is a certificate type
     */
    public boolean isCertificateType() {
        return this == CERTIFICATE || this == CERTIFICATE_REQUEST;
    }
    
    /**
     * Checks if this object type represents data (not keys or certificates).
     * 
     * @return true if this is a data type
     */
    public boolean isDataType() {
        return this == SECRET_DATA || this == OPAQUE_DATA;
    }
    
    /**
     * Checks if this object type represents an asymmetric key.
     * 
     * @return true if this is an asymmetric key type
     */
    public boolean isAsymmetricKeyType() {
        return this == PUBLIC_KEY || this == PRIVATE_KEY;
    }
    
    /**
     * Checks if this object type represents a symmetric key.
     * 
     * @return true if this is a symmetric key type
     */
    public boolean isSymmetricKeyType() {
        return this == SYMMETRIC_KEY;
    }
    
    /**
     * Checks if this object type is supported by the current server implementation.
     * 
     * @return true if supported, false otherwise
     */
    public boolean isSupported() {
        switch (this) {
            case SYMMETRIC_KEY:
            case SECRET_DATA:
            case OPAQUE_DATA:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Gets the default cryptographic usage mask for this object type.
     * 
     * @return default usage mask, or 0 if not applicable
     */
    public int getDefaultUsageMask() {
        switch (this) {
            case SYMMETRIC_KEY:
                return 0x0000000C; // Encrypt | Decrypt
            case PUBLIC_KEY:
                return 0x00000004; // Encrypt
            case PRIVATE_KEY:
                return 0x00000008; // Decrypt
            default:
                return 0x00000000; // No cryptographic usage
        }
    }
    
    /**
     * Checks if this object type requires cryptographic attributes.
     * 
     * @return true if cryptographic attributes are required
     */
    public boolean requiresCryptographicAttributes() {
        return isKeyType();
    }
    
    /**
     * Gets the expected key format types for this object type.
     * 
     * @return array of supported key format type codes, or empty array if not applicable
     */
    public int[] getSupportedKeyFormats() {
        switch (this) {
            case SYMMETRIC_KEY:
                return new int[]{0x000001}; // Raw
            case PUBLIC_KEY:
            case PRIVATE_KEY:
                return new int[]{0x000001, 0x000002, 0x000003}; // Raw, PKCS#1, PKCS#8
            case CERTIFICATE:
                return new int[]{0x000004}; // X.509
            default:
                return new int[0];
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, getHexCode());
    }
}
