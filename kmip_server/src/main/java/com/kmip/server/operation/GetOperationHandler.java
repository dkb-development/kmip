package com.kmip.server.operation;

import com.kmip.server.core.exception.KmipException;
import com.kmip.server.protocol.codec.PyKmipCompatEncoder;
import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.KmipTagResolver;
import com.kmip.server.protocol.tag.TagValueUtil;
import com.kmip.server.service.KeyManagementService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Handles KMIP Get operations.
 */
@Component
public class GetOperationHandler implements OperationHandler {

    private static final Logger log = LoggerFactory.getLogger(GetOperationHandler.class);
    private static final int OPERATION_GET = TagValueUtil.OPERATION_GET;
    private static final int OBJECT_TYPE_SYMMETRIC_KEY = TagValueUtil.OBJECT_TYPE_SYMMETRIC_KEY;

    private final KeyManagementService keyManagementService;
    private final PyKmipCompatEncoder pyKmipCompatEncoder;

    /**
     * Creates a new GetOperationHandler with the specified key management service.
     *
     * @param keyManagementService The key management service
     * @param pyKmipCompatEncoder The PyKMIP compatibility encoder
     */
    @Autowired
    public GetOperationHandler(
            @Qualifier("inMemoryKms") KeyManagementService keyManagementService,
            PyKmipCompatEncoder pyKmipCompatEncoder) {
        this.keyManagementService = keyManagementService;
        this.pyKmipCompatEncoder = pyKmipCompatEncoder;
    }

    @Override
    public KmipMessage handle(KmipMessage request) {
        log.info("Handling Get operation");

        // 1. Extract the unique identifier from the request
        String uniqueIdentifier = extractUniqueIdentifier(request);
        log.info("Retrieving object with unique identifier: {}", uniqueIdentifier);

        // 2. Retrieve the key from the key management service
        Optional<SecretKey> keyOpt = keyManagementService.getSymmetricKey(uniqueIdentifier);
        if (!keyOpt.isPresent()) {
            throw new KmipException("Key not found: " + uniqueIdentifier,
                TagValueUtil.RESULT_REASON_ITEM_NOT_FOUND);
        }
        SecretKey key = keyOpt.get();
        log.info("Retrieved key: algorithm={}, format={}, encoded length={} bytes",
            key.getAlgorithm(), key.getFormat(), key.getEncoded().length);

        // 3. Use PyKmipCompatEncoder to create a response payload that is compatible with PyKMIP
        KmipMessage responsePayload = new KmipMessage();

        try {
            // Create a TTLV-encoded byte array for the response payload
            byte[] encodedPayload = pyKmipCompatEncoder.createGetResponsePayload(
                OBJECT_TYPE_SYMMETRIC_KEY,
                uniqueIdentifier,
                key.getEncoded(),
                3, // AES
                key.getEncoded().length * 8, // Length in bits
                12 // Encrypt | Decrypt
            );

            // Log the encoded payload for debugging
            log.info("Encoded payload hex dump: {}", pyKmipCompatEncoder.bytesToHex(encodedPayload));

            // For now, we'll still use the standard KmipMessage approach
            // But we'll keep the PyKmipCompatEncoder code for future reference

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

            // The order of fields in the Key Block is critical for PyKMIP compatibility
            // 1. Key Format Type MUST be first
            keyBlock.addField(KmipTagResolver.TAG_KEY_FORMAT_TYPE, 1); // Raw format

            // 2. Key Value with Key Material MUST be second
            KmipMessage keyValue = new KmipMessage();
            keyValue.addField(KmipTagResolver.TAG_KEY_MATERIAL, key.getEncoded());
            keyBlock.addField(KmipTagResolver.TAG_KEY_VALUE, keyValue);

            // 3. Cryptographic Algorithm MUST be third
            keyBlock.addField(KmipTagResolver.TAG_CRYPTOGRAPHIC_ALGORITHM, 3); // AES

            // 4. Cryptographic Length MUST be fourth
            keyBlock.addField(KmipTagResolver.TAG_CRYPTOGRAPHIC_LENGTH, key.getEncoded().length * 8);

            // 5. Cryptographic Usage Mask MUST be fifth
            keyBlock.addField(KmipTagResolver.TAG_CRYPTOGRAPHIC_USAGE_MASK, 12); // Encrypt | Decrypt

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

    private String extractUniqueIdentifier(KmipMessage request) {
        // Extract the batch item
        List<Object> batchItems = request.getFieldValues(KmipTagResolver.TAG_REQUEST_BATCH_ITEM);
        if (batchItems.isEmpty()) {
            throw new KmipException("No batch items found in request");
        }

        KmipMessage batchItem = (KmipMessage) batchItems.get(0);

        // Extract the request payload
        Object payloadObj = batchItem.getFieldValue(KmipTagResolver.TAG_REQUEST_PAYLOAD);
        if (payloadObj == null) {
            throw new KmipException("No request payload found in batch item");
        }

        KmipMessage payload = (KmipMessage) payloadObj;

        // Extract the unique identifier
        Object uniqueIdentifierObj = payload.getFieldValue(KmipTagResolver.TAG_UNIQUE_IDENTIFIER);
        if (uniqueIdentifierObj == null) {
            throw new KmipException("No unique identifier found in request payload");
        }

        return (String) uniqueIdentifierObj;
    }
}
