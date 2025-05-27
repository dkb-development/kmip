package com.kmip.server.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kmip.server.core.exception.KmipException;
import com.kmip.server.protocol.codec.KmipEncoder;
import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.KmipTagResolver;
import com.kmip.server.protocol.tag.TagValueUtil;
import com.kmip.server.service.KeyManagementService;
import com.kmip.server.core.enums.KmipResultReason;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Optional;

/**
 * Handler for KMIP Get operation.
 * Retrieves cryptographic objects by their unique identifier.
 */
@Component
public class GetOperationHandler implements OperationHandler {

    private static final Logger log = LoggerFactory.getLogger(GetOperationHandler.class);
    private static final int OPERATION_GET = 10; // KMIP Get operation code (10 in PyKMIP)
    private static final int OBJECT_TYPE_SYMMETRIC_KEY = 2; // KMIP Symmetric Key object type

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("inMemoryKms")
    private KeyManagementService keyManagementService; // Explicitly use InMemoryKMS

    @Autowired
    private KmipEncoder kmipEncoder;

    @Override
    public KmipMessage handle(KmipMessage requestHeader, KmipMessage requestPayload) throws KmipException {
        log.info("Handling GET operation...");

        // 1. Extract Unique Identifier from request payload
        String uniqueIdentifier = TagValueUtil.getUniqueIdentifier(requestPayload)
                .orElseThrow(() -> new KmipException("Missing Unique Identifier in Get request"));

        log.info("Retrieving object with Unique Identifier: {}", uniqueIdentifier);

        // 2. Retrieve the key from the key management service
        Optional<SecretKey> keyOptional = keyManagementService.getSymmetricKey(uniqueIdentifier);

        if (!keyOptional.isPresent()) {
            log.error("Key with ID {} not found", uniqueIdentifier);
            throw new KmipException("Key not found: " + uniqueIdentifier,
                                    KmipResultReason.ITEM_NOT_FOUND);
        }

        SecretKey key = keyOptional.get();
        log.info("Successfully retrieved key with ID: {}", uniqueIdentifier);

        // 3. Use KmipEncoder to create a response payload that is compatible with PyKMIP
        KmipMessage responsePayload = new KmipMessage();

        try {
            // Create a TTLV-encoded byte array for the response payload
            byte[] encodedPayload = kmipEncoder.createGetResponsePayload(
                OBJECT_TYPE_SYMMETRIC_KEY,
                uniqueIdentifier,
                key.getEncoded(),
                3, // AES
                key.getEncoded().length * 8, // Length in bits
                12 // Encrypt | Decrypt
            );

            // Log the encoded payload for debugging
            log.info("Encoded payload hex dump: {}", kmipEncoder.bytesToHex(encodedPayload));

            // For now, we'll still use the standard KmipMessage approach
            // But we'll keep the direct encoding code for future reference

            // Object Type MUST be first (REQUIRED)
            responsePayload.addField(KmipTagResolver.TAG_OBJECT_TYPE, OBJECT_TYPE_SYMMETRIC_KEY);
            log.info("Added Object Type: {} (Symmetric Key)", OBJECT_TYPE_SYMMETRIC_KEY);

            // Unique Identifier MUST be second (REQUIRED)
            responsePayload.addField(KmipTagResolver.TAG_UNIQUE_IDENTIFIER, uniqueIdentifier);
            log.info("Added Unique Identifier: {}", uniqueIdentifier);

            // The third field MUST be the Symmetric Key object (REQUIRED)
            KmipMessage symmetricKey = new KmipMessage();

            // Create a Key Block that exactly matches PyKMIP's expectations
            KmipMessage keyBlock = new KmipMessage();

            // Fields MUST be added in the exact order expected by PyKMIP:
            // 1. key_format_type (Required)
            keyBlock.addField(KmipTagResolver.TAG_KEY_FORMAT_TYPE, 1); // Raw format

            // 2. key_compression_type (Optional) - We're skipping this

            // 3. key_value (Required)
            KmipMessage keyValue = new KmipMessage();
            keyValue.addField(KmipTagResolver.TAG_KEY_MATERIAL, key.getEncoded());
            keyBlock.addField(KmipTagResolver.TAG_KEY_VALUE, keyValue);

            // 4. cryptographic_algorithm (Optional)
            keyBlock.addField(KmipTagResolver.TAG_CRYPTOGRAPHIC_ALGORITHM, 3); // AES

            // 5. cryptographic_length (Optional)
            keyBlock.addField(KmipTagResolver.TAG_CRYPTOGRAPHIC_LENGTH, key.getEncoded().length * 8);

            // 6. key_wrapping_data (Optional) - We're skipping this

            // Note: We're not adding cryptographic_usage_mask here because it's not part of
            // the KeyBlock structure in PyKMIP. It's an attribute that should be associated
            // with the managed object separately.

            // Add Key Block to Symmetric Key
            symmetricKey.addField(KmipTagResolver.TAG_KEY_BLOCK, keyBlock);

            // Add the Symmetric Key object to the response payload
            responsePayload.addField(KmipTagResolver.TAG_SYMMETRIC_KEY, symmetricKey);

            log.info("Successfully built KMIP 2.0 compliant Get response payload for key: {}", uniqueIdentifier);
        } catch (IOException e) {
            log.error("Error creating response payload: {}", e.getMessage());
            throw new KmipException("Error creating response payload: " + e.getMessage());
        }

        return responsePayload;
    }

    @Override
    public int getOperationCode() {
        return OPERATION_GET;
    }

    /**
     * Future implementation for external KMS integration
     * This is just a placeholder showing how external KMS calls might be structured
     */
    /*
    private SecretKey retrieveKeyFromExternalKMS(String keyId) {
        // Example of how we might call an external KMS via REST API
        try {
            // Setup REST client (using Spring RestTemplate, WebClient, or similar)
            RestTemplate restTemplate = new RestTemplate();

            // Prepare request with authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + kmsApiToken);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("keyId", keyId);
            requestBody.put("operation", "retrieve");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Make API call to external KMS
            ResponseEntity<KeyResponse> response = restTemplate.exchange(
                kmsApiEndpoint + "/keys/" + keyId,
                HttpMethod.GET,
                request,
                KeyResponse.class
            );

            // Process response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                KeyResponse keyResponse = response.getBody();

                // Convert external KMS response to SecretKey
                byte[] keyBytes = Base64.getDecoder().decode(keyResponse.getKeyMaterial());
                SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

                return secretKey;
            } else {
                throw new KmipException("External KMS returned error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error calling external KMS", e);
            throw new KmipException("Failed to retrieve key from external KMS", e);
        }
    }

    // Example response class for external KMS
    private static class KeyResponse {
        private String keyId;
        private String keyMaterial;
        private String algorithm;
        private int length;

        // Getters and setters
        public String getKeyId() { return keyId; }
        public void setKeyId(String keyId) { this.keyId = keyId; }
        public String getKeyMaterial() { return keyMaterial; }
        public void setKeyMaterial(String keyMaterial) { this.keyMaterial = keyMaterial; }
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }
    }
    */
}