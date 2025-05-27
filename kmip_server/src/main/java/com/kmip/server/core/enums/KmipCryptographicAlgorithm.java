package com.kmip.server.core.enums;

/**
 * KMIP Cryptographic Algorithm Enumeration.
 * 
 * This enum defines all cryptographic algorithms supported by KMIP as specified
 * in the KMIP 2.0 specification. These algorithms are used for various
 * cryptographic operations including encryption, decryption, signing, etc.
 * 
 * Reference: KMIP 2.0 Specification, Table 406 (Cryptographic Algorithm Enumeration)
 */
public enum KmipCryptographicAlgorithm {
    
    /**
     * Data Encryption Standard (DES) - Legacy symmetric encryption algorithm.
     */
    DES(0x000001, "DES", "DES"),
    
    /**
     * Triple DES (3DES) - Enhanced version of DES with triple encryption.
     */
    TRIPLE_DES(0x000002, "3DES", "DESede"),
    
    /**
     * Advanced Encryption Standard (AES) - Modern symmetric encryption standard.
     */
    AES(0x000003, "AES", "AES"),
    
    /**
     * RSA - Asymmetric encryption and digital signature algorithm.
     */
    RSA(0x000004, "RSA", "RSA"),
    
    /**
     * Digital Signature Algorithm (DSA) - Digital signature algorithm.
     */
    DSA(0x000005, "DSA", "DSA"),
    
    /**
     * Elliptic Curve Digital Signature Algorithm (ECDSA) - Elliptic curve based DSA.
     */
    ECDSA(0x000006, "ECDSA", "EC"),
    
    /**
     * HMAC with SHA-1 - Hash-based Message Authentication Code with SHA-1.
     */
    HMAC_SHA1(0x000007, "HMAC-SHA1", "HmacSHA1"),
    
    /**
     * HMAC with SHA-224 - Hash-based Message Authentication Code with SHA-224.
     */
    HMAC_SHA224(0x000008, "HMAC-SHA224", "HmacSHA224"),
    
    /**
     * HMAC with SHA-256 - Hash-based Message Authentication Code with SHA-256.
     */
    HMAC_SHA256(0x000009, "HMAC-SHA256", "HmacSHA256"),
    
    /**
     * HMAC with SHA-384 - Hash-based Message Authentication Code with SHA-384.
     */
    HMAC_SHA384(0x00000A, "HMAC-SHA384", "HmacSHA384"),
    
    /**
     * HMAC with SHA-512 - Hash-based Message Authentication Code with SHA-512.
     */
    HMAC_SHA512(0x00000B, "HMAC-SHA512", "HmacSHA512"),
    
    /**
     * HMAC with MD5 - Hash-based Message Authentication Code with MD5 (deprecated).
     */
    HMAC_MD5(0x00000C, "HMAC-MD5", "HmacMD5"),
    
    /**
     * Elliptic Curve Diffie-Hellman (ECDH) - Key agreement algorithm.
     */
    ECDH(0x00000D, "ECDH", "ECDH"),
    
    /**
     * Elliptic Curve Menezes-Qu-Vanstone (ECMQV) - Key agreement algorithm.
     */
    ECMQV(0x00000E, "ECMQV", "ECMQV"),
    
    /**
     * Blowfish - Symmetric block cipher.
     */
    BLOWFISH(0x00000F, "Blowfish", "Blowfish"),
    
    /**
     * Camellia - Symmetric block cipher.
     */
    CAMELLIA(0x000010, "Camellia", "Camellia"),
    
    /**
     * CAST5 - Symmetric block cipher.
     */
    CAST5(0x000011, "CAST5", "CAST5"),
    
    /**
     * IDEA - International Data Encryption Algorithm.
     */
    IDEA(0x000012, "IDEA", "IDEA"),
    
    /**
     * MARS - Symmetric block cipher.
     */
    MARS(0x000013, "MARS", "MARS"),
    
    /**
     * RC2 - Rivest Cipher 2.
     */
    RC2(0x000014, "RC2", "RC2"),
    
    /**
     * RC4 - Rivest Cipher 4 (stream cipher).
     */
    RC4(0x000015, "RC4", "RC4"),
    
    /**
     * RC5 - Rivest Cipher 5.
     */
    RC5(0x000016, "RC5", "RC5"),
    
    /**
     * SKIPJACK - NSA symmetric block cipher.
     */
    SKIPJACK(0x000017, "SKIPJACK", "SKIPJACK"),
    
    /**
     * Twofish - Symmetric block cipher.
     */
    TWOFISH(0x000018, "Twofish", "Twofish"),
    
    /**
     * Unknown algorithm for error handling.
     */
    UNKNOWN(0xFFFFFF, "Unknown", "Unknown");
    
    private final int code;
    private final String displayName;
    private final String jcaName;
    
    /**
     * Constructor for KMIP Cryptographic Algorithm enum.
     * 
     * @param code the algorithm code
     * @param displayName the human-readable name
     * @param jcaName the Java Cryptography Architecture name
     */
    KmipCryptographicAlgorithm(int code, String displayName, String jcaName) {
        this.code = code;
        this.displayName = displayName;
        this.jcaName = jcaName;
    }
    
    /**
     * Gets the algorithm code.
     * 
     * @return the algorithm code
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
     * Gets the Java Cryptography Architecture (JCA) name.
     * 
     * @return the JCA algorithm name
     */
    public String getJcaName() {
        return jcaName;
    }
    
    /**
     * Gets the algorithm code as hexadecimal string.
     * 
     * @return the algorithm code in hex format
     */
    public String getHexCode() {
        return String.format("0x%06X", code);
    }
    
    /**
     * Finds the algorithm by its code.
     * 
     * @param code the algorithm code to look up
     * @return the corresponding KmipCryptographicAlgorithm, or UNKNOWN if not found
     */
    public static KmipCryptographicAlgorithm fromCode(int code) {
        for (KmipCryptographicAlgorithm algorithm : values()) {
            if (algorithm.code == code) {
                return algorithm;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * Finds the algorithm by its JCA name.
     * 
     * @param jcaName the JCA name to look up
     * @return the corresponding KmipCryptographicAlgorithm, or UNKNOWN if not found
     */
    public static KmipCryptographicAlgorithm fromJcaName(String jcaName) {
        if (jcaName == null) {
            return UNKNOWN;
        }
        
        for (KmipCryptographicAlgorithm algorithm : values()) {
            if (algorithm.jcaName.equalsIgnoreCase(jcaName)) {
                return algorithm;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * Checks if this algorithm is a symmetric encryption algorithm.
     * 
     * @return true if symmetric encryption algorithm
     */
    public boolean isSymmetricEncryption() {
        return this == DES || this == TRIPLE_DES || this == AES || this == BLOWFISH ||
               this == CAMELLIA || this == CAST5 || this == IDEA || this == MARS ||
               this == RC2 || this == RC4 || this == RC5 || this == SKIPJACK || this == TWOFISH;
    }
    
    /**
     * Checks if this algorithm is an asymmetric encryption algorithm.
     * 
     * @return true if asymmetric encryption algorithm
     */
    public boolean isAsymmetricEncryption() {
        return this == RSA;
    }
    
    /**
     * Checks if this algorithm is a digital signature algorithm.
     * 
     * @return true if digital signature algorithm
     */
    public boolean isDigitalSignature() {
        return this == RSA || this == DSA || this == ECDSA;
    }
    
    /**
     * Checks if this algorithm is a message authentication code algorithm.
     * 
     * @return true if MAC algorithm
     */
    public boolean isMacAlgorithm() {
        return this == HMAC_SHA1 || this == HMAC_SHA224 || this == HMAC_SHA256 ||
               this == HMAC_SHA384 || this == HMAC_SHA512 || this == HMAC_MD5;
    }
    
    /**
     * Checks if this algorithm is a key agreement algorithm.
     * 
     * @return true if key agreement algorithm
     */
    public boolean isKeyAgreement() {
        return this == ECDH || this == ECMQV;
    }
    
    /**
     * Checks if this algorithm is supported by the current server implementation.
     * 
     * @return true if supported, false otherwise
     */
    public boolean isSupported() {
        switch (this) {
            case AES:
            case DES:
            case TRIPLE_DES:
            case RSA:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Gets the default key lengths for this algorithm in bits.
     * 
     * @return array of supported key lengths, or empty array if not applicable
     */
    public int[] getDefaultKeyLengths() {
        switch (this) {
            case DES:
                return new int[]{56};
            case TRIPLE_DES:
                return new int[]{112, 168};
            case AES:
                return new int[]{128, 192, 256};
            case RSA:
                return new int[]{1024, 2048, 3072, 4096};
            case BLOWFISH:
                return new int[]{32, 64, 128, 256, 448};
            default:
                return new int[0];
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, getHexCode());
    }
}
