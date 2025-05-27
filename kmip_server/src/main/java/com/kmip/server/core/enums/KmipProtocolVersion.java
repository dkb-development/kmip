package com.kmip.server.core.enums;

/**
 * KMIP Protocol Version Enumeration.
 * 
 * This enum defines all KMIP protocol versions as specified in the KMIP specifications.
 * Protocol versions are used to negotiate capabilities between client and server.
 * 
 * Reference: KMIP Specifications (various versions)
 */
public enum KmipProtocolVersion {
    
    /**
     * KMIP Version 1.0 - Initial KMIP specification.
     */
    VERSION_1_0(1, 0, "1.0"),
    
    /**
     * KMIP Version 1.1 - Enhanced KMIP specification.
     */
    VERSION_1_1(1, 1, "1.1"),
    
    /**
     * KMIP Version 1.2 - Extended KMIP specification with cryptographic operations.
     */
    VERSION_1_2(1, 2, "1.2"),
    
    /**
     * KMIP Version 1.3 - Further enhanced KMIP specification.
     */
    VERSION_1_3(1, 3, "1.3"),
    
    /**
     * KMIP Version 1.4 - KMIP specification with import/export operations.
     */
    VERSION_1_4(1, 4, "1.4"),
    
    /**
     * KMIP Version 2.0 - Major revision of KMIP specification.
     */
    VERSION_2_0(2, 0, "2.0"),
    
    /**
     * KMIP Version 2.1 - Enhanced KMIP 2.0 specification.
     */
    VERSION_2_1(2, 1, "2.1");
    
    private final int majorVersion;
    private final int minorVersion;
    private final String versionString;
    
    /**
     * Constructor for KMIP Protocol Version enum.
     * 
     * @param majorVersion the major version number
     * @param minorVersion the minor version number
     * @param versionString the version string representation
     */
    KmipProtocolVersion(int majorVersion, int minorVersion, String versionString) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.versionString = versionString;
    }
    
    /**
     * Gets the major version number.
     * 
     * @return the major version
     */
    public int getMajorVersion() {
        return majorVersion;
    }
    
    /**
     * Gets the minor version number.
     * 
     * @return the minor version
     */
    public int getMinorVersion() {
        return minorVersion;
    }
    
    /**
     * Gets the version string representation.
     * 
     * @return the version string (e.g., "2.0")
     */
    public String getVersionString() {
        return versionString;
    }
    
    /**
     * Gets the version as a combined integer (major * 100 + minor).
     * 
     * @return the combined version number
     */
    public int getCombinedVersion() {
        return majorVersion * 100 + minorVersion;
    }
    
    /**
     * Finds the protocol version by version string.
     * 
     * @param versionString the version string to look up
     * @return the corresponding KmipProtocolVersion, or null if not found
     */
    public static KmipProtocolVersion fromVersionString(String versionString) {
        if (versionString == null) {
            return null;
        }
        
        for (KmipProtocolVersion version : values()) {
            if (version.versionString.equals(versionString)) {
                return version;
            }
        }
        return null;
    }
    
    /**
     * Finds the protocol version by major and minor version numbers.
     * 
     * @param majorVersion the major version number
     * @param minorVersion the minor version number
     * @return the corresponding KmipProtocolVersion, or null if not found
     */
    public static KmipProtocolVersion fromVersionNumbers(int majorVersion, int minorVersion) {
        for (KmipProtocolVersion version : values()) {
            if (version.majorVersion == majorVersion && version.minorVersion == minorVersion) {
                return version;
            }
        }
        return null;
    }
    
    /**
     * Finds the protocol version by combined version number.
     * 
     * @param combinedVersion the combined version number
     * @return the corresponding KmipProtocolVersion, or null if not found
     */
    public static KmipProtocolVersion fromCombinedVersion(int combinedVersion) {
        for (KmipProtocolVersion version : values()) {
            if (version.getCombinedVersion() == combinedVersion) {
                return version;
            }
        }
        return null;
    }
    
    /**
     * Gets the latest supported protocol version.
     * 
     * @return the latest protocol version
     */
    public static KmipProtocolVersion getLatest() {
        return VERSION_2_1;
    }
    
    /**
     * Gets the default protocol version for the server.
     * 
     * @return the default protocol version
     */
    public static KmipProtocolVersion getDefault() {
        return VERSION_2_0;
    }
    
    /**
     * Checks if this version is compatible with another version.
     * Generally, newer versions are backward compatible with older versions.
     * 
     * @param other the other version to check compatibility with
     * @return true if compatible, false otherwise
     */
    public boolean isCompatibleWith(KmipProtocolVersion other) {
        if (other == null) {
            return false;
        }
        
        // Same version is always compatible
        if (this == other) {
            return true;
        }
        
        // Newer versions are generally backward compatible
        return this.getCombinedVersion() >= other.getCombinedVersion();
    }
    
    /**
     * Checks if this version is newer than another version.
     * 
     * @param other the other version to compare with
     * @return true if this version is newer, false otherwise
     */
    public boolean isNewerThan(KmipProtocolVersion other) {
        if (other == null) {
            return true;
        }
        
        return this.getCombinedVersion() > other.getCombinedVersion();
    }
    
    /**
     * Checks if this version is older than another version.
     * 
     * @param other the other version to compare with
     * @return true if this version is older, false otherwise
     */
    public boolean isOlderThan(KmipProtocolVersion other) {
        if (other == null) {
            return false;
        }
        
        return this.getCombinedVersion() < other.getCombinedVersion();
    }
    
    /**
     * Checks if this version is supported by the current server implementation.
     * 
     * @return true if supported, false otherwise
     */
    public boolean isSupported() {
        // Currently, we primarily support KMIP 2.0
        return this == VERSION_2_0 || this == VERSION_1_4 || this == VERSION_1_3;
    }
    
    /**
     * Gets the operations supported by this protocol version.
     * 
     * @return array of supported operation codes
     */
    public int[] getSupportedOperations() {
        switch (this) {
            case VERSION_1_0:
                return new int[]{0x000001, 0x00000A, 0x000014, 0x000018}; // Create, Get, Destroy, Query
            case VERSION_1_1:
            case VERSION_1_2:
            case VERSION_1_3:
            case VERSION_1_4:
                return new int[]{0x000001, 0x00000A, 0x000014, 0x000018, 0x00000B}; // + Get Attributes
            case VERSION_2_0:
            case VERSION_2_1:
                return new int[]{0x000001, 0x00000A, 0x000014, 0x000018, 0x00000B, 0x00001E}; // + Discover Versions
            default:
                return new int[0];
        }
    }
    
    @Override
    public String toString() {
        return String.format("KMIP %s", versionString);
    }
}
