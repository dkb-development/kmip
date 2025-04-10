package com.kmip.server.protocol.tag;

/**
 * Utility class for working with KMIP tag values.
 * 
 * This class provides constants and utility methods for working with KMIP tag values.
 */
public class TagValueUtil {
    // Operation values
    public static final int OPERATION_CREATE = 0x01;
    public static final int OPERATION_GET = 0x0A;
    public static final int OPERATION_DESTROY = 0x14;
    
    // Object Type values
    public static final int OBJECT_TYPE_SYMMETRIC_KEY = 0x02;
    public static final int OBJECT_TYPE_PUBLIC_KEY = 0x03;
    public static final int OBJECT_TYPE_PRIVATE_KEY = 0x04;
    public static final int OBJECT_TYPE_CERTIFICATE = 0x05;
    
    // Result Status values
    public static final int RESULT_STATUS_SUCCESS = 0x00;
    public static final int RESULT_STATUS_OPERATION_FAILED = 0x01;
    public static final int RESULT_STATUS_OPERATION_PENDING = 0x02;
    public static final int RESULT_STATUS_OPERATION_UNDONE = 0x03;
    
    // Result Reason values
    public static final int RESULT_REASON_ITEM_NOT_FOUND = 0x01;
    public static final int RESULT_REASON_RESPONSE_TOO_LARGE = 0x02;
    public static final int RESULT_REASON_AUTHENTICATION_NOT_SUCCESSFUL = 0x03;
    public static final int RESULT_REASON_INVALID_MESSAGE = 0x04;
    public static final int RESULT_REASON_OPERATION_NOT_SUPPORTED = 0x05;
    
    // Key Format Type values
    public static final int KEY_FORMAT_TYPE_RAW = 0x01;
    public static final int KEY_FORMAT_TYPE_PKCS1 = 0x02;
    public static final int KEY_FORMAT_TYPE_PKCS8 = 0x03;
    
    // Cryptographic Algorithm values
    public static final int CRYPTO_ALGORITHM_DES = 0x01;
    public static final int CRYPTO_ALGORITHM_3DES = 0x02;
    public static final int CRYPTO_ALGORITHM_AES = 0x03;
    public static final int CRYPTO_ALGORITHM_RSA = 0x04;
    public static final int CRYPTO_ALGORITHM_DSA = 0x05;
    public static final int CRYPTO_ALGORITHM_ECDSA = 0x06;
    
    // Cryptographic Usage Mask values
    public static final int CRYPTO_USAGE_SIGN = 0x00000001;
    public static final int CRYPTO_USAGE_VERIFY = 0x00000002;
    public static final int CRYPTO_USAGE_ENCRYPT = 0x00000004;
    public static final int CRYPTO_USAGE_DECRYPT = 0x00000008;
    public static final int CRYPTO_USAGE_WRAP_KEY = 0x00000010;
    public static final int CRYPTO_USAGE_UNWRAP_KEY = 0x00000020;
    
    /**
     * Gets the name of an operation.
     *
     * @param operation The operation value
     * @return The name of the operation, or "Unknown Operation" if the operation is not recognized
     */
    public static String getOperationName(int operation) {
        switch (operation) {
            case OPERATION_CREATE:
                return "Create";
            case OPERATION_GET:
                return "Get";
            case OPERATION_DESTROY:
                return "Destroy";
            default:
                return "Unknown Operation (0x" + Integer.toHexString(operation) + ")";
        }
    }
    
    /**
     * Gets the name of an object type.
     *
     * @param objectType The object type value
     * @return The name of the object type, or "Unknown Object Type" if the object type is not recognized
     */
    public static String getObjectTypeName(int objectType) {
        switch (objectType) {
            case OBJECT_TYPE_SYMMETRIC_KEY:
                return "Symmetric Key";
            case OBJECT_TYPE_PUBLIC_KEY:
                return "Public Key";
            case OBJECT_TYPE_PRIVATE_KEY:
                return "Private Key";
            case OBJECT_TYPE_CERTIFICATE:
                return "Certificate";
            default:
                return "Unknown Object Type (0x" + Integer.toHexString(objectType) + ")";
        }
    }
    
    /**
     * Gets the name of a result status.
     *
     * @param resultStatus The result status value
     * @return The name of the result status, or "Unknown Result Status" if the result status is not recognized
     */
    public static String getResultStatusName(int resultStatus) {
        switch (resultStatus) {
            case RESULT_STATUS_SUCCESS:
                return "Success";
            case RESULT_STATUS_OPERATION_FAILED:
                return "Operation Failed";
            case RESULT_STATUS_OPERATION_PENDING:
                return "Operation Pending";
            case RESULT_STATUS_OPERATION_UNDONE:
                return "Operation Undone";
            default:
                return "Unknown Result Status (0x" + Integer.toHexString(resultStatus) + ")";
        }
    }
    
    /**
     * Gets the name of a result reason.
     *
     * @param resultReason The result reason value
     * @return The name of the result reason, or "Unknown Result Reason" if the result reason is not recognized
     */
    public static String getResultReasonName(int resultReason) {
        switch (resultReason) {
            case RESULT_REASON_ITEM_NOT_FOUND:
                return "Item Not Found";
            case RESULT_REASON_RESPONSE_TOO_LARGE:
                return "Response Too Large";
            case RESULT_REASON_AUTHENTICATION_NOT_SUCCESSFUL:
                return "Authentication Not Successful";
            case RESULT_REASON_INVALID_MESSAGE:
                return "Invalid Message";
            case RESULT_REASON_OPERATION_NOT_SUPPORTED:
                return "Operation Not Supported";
            default:
                return "Unknown Result Reason (0x" + Integer.toHexString(resultReason) + ")";
        }
    }
    
    /**
     * Gets the name of a key format type.
     *
     * @param keyFormatType The key format type value
     * @return The name of the key format type, or "Unknown Key Format Type" if the key format type is not recognized
     */
    public static String getKeyFormatTypeName(int keyFormatType) {
        switch (keyFormatType) {
            case KEY_FORMAT_TYPE_RAW:
                return "Raw";
            case KEY_FORMAT_TYPE_PKCS1:
                return "PKCS#1";
            case KEY_FORMAT_TYPE_PKCS8:
                return "PKCS#8";
            default:
                return "Unknown Key Format Type (0x" + Integer.toHexString(keyFormatType) + ")";
        }
    }
    
    /**
     * Gets the name of a cryptographic algorithm.
     *
     * @param algorithm The cryptographic algorithm value
     * @return The name of the cryptographic algorithm, or "Unknown Algorithm" if the algorithm is not recognized
     */
    public static String getCryptoAlgorithmName(int algorithm) {
        switch (algorithm) {
            case CRYPTO_ALGORITHM_DES:
                return "DES";
            case CRYPTO_ALGORITHM_3DES:
                return "3DES";
            case CRYPTO_ALGORITHM_AES:
                return "AES";
            case CRYPTO_ALGORITHM_RSA:
                return "RSA";
            case CRYPTO_ALGORITHM_DSA:
                return "DSA";
            case CRYPTO_ALGORITHM_ECDSA:
                return "ECDSA";
            default:
                return "Unknown Algorithm (0x" + Integer.toHexString(algorithm) + ")";
        }
    }
    
    /**
     * Gets a string representation of a cryptographic usage mask.
     *
     * @param usageMask The cryptographic usage mask value
     * @return A string representation of the cryptographic usage mask
     */
    public static String getCryptoUsageMaskString(int usageMask) {
        StringBuilder sb = new StringBuilder();
        
        if ((usageMask & CRYPTO_USAGE_SIGN) != 0) {
            sb.append("Sign, ");
        }
        if ((usageMask & CRYPTO_USAGE_VERIFY) != 0) {
            sb.append("Verify, ");
        }
        if ((usageMask & CRYPTO_USAGE_ENCRYPT) != 0) {
            sb.append("Encrypt, ");
        }
        if ((usageMask & CRYPTO_USAGE_DECRYPT) != 0) {
            sb.append("Decrypt, ");
        }
        if ((usageMask & CRYPTO_USAGE_WRAP_KEY) != 0) {
            sb.append("Wrap Key, ");
        }
        if ((usageMask & CRYPTO_USAGE_UNWRAP_KEY) != 0) {
            sb.append("Unwrap Key, ");
        }
        
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2); // Remove trailing comma and space
            return sb.toString();
        } else {
            return "None";
        }
    }
}
