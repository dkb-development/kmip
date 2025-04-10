package com.kmip.server.operation;

import com.kmip.server.core.exception.KmipException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles KMIP Create operations.
 */
@Component
public class CreateOperationHandler implements OperationHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateOperationHandler.class);
    private static final int OPERATION_CREATE = TagValueUtil.OPERATION_CREATE;
    private static final int OBJECT_TYPE_SYMMETRIC_KEY = TagValueUtil.OBJECT_TYPE_SYMMETRIC_KEY;

    private final KeyManagementService keyManagementService;

    /**
     * Creates a new CreateOperationHandler with the specified key management service.
     *
     * @param keyManagementService The key management service
     */
    @Autowired
    public CreateOperationHandler(@Qualifier("inMemoryKms") KeyManagementService keyManagementService) {
        this.keyManagementService = keyManagementService;
    }

    @Override
    public KmipMessage handle(KmipMessage request) {
        log.info("Handling Create operation");

        // Extract the request payload
        KmipMessage requestPayload = extractRequestPayload(request);

        // Extract the object type
        int objectType = extractObjectType(requestPayload);
        log.info("Object type: {} (0x{})", TagValueUtil.getObjectTypeName(objectType),
            Integer.toHexString(objectType));

        // Handle based on object type
        if (objectType == OBJECT_TYPE_SYMMETRIC_KEY) {
            return handleCreateSymmetricKey(requestPayload);
        } else {
            throw new KmipException("Unsupported object type: " + TagValueUtil.getObjectTypeName(objectType));
        }
    }

    @Override
    public int getOperationCode() {
        return OPERATION_CREATE;
    }

    private KmipMessage extractRequestPayload(KmipMessage request) {
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

        return (KmipMessage) payloadObj;
    }

    private int extractObjectType(KmipMessage requestPayload) {
        Object objectTypeObj = requestPayload.getFieldValue(KmipTagResolver.TAG_OBJECT_TYPE);
        if (objectTypeObj == null) {
            throw new KmipException("No object type found in request payload");
        }

        return (Integer) objectTypeObj;
    }

    private KmipMessage handleCreateSymmetricKey(KmipMessage requestPayload) {
        log.info("Creating symmetric key");

        // Extract attributes from the request payload
        int algorithm = 3; // Default to AES
        int length = 256; // Default to 256 bits

        // Extract template attribute if present
        Object templateAttributeObj = requestPayload.getFieldValue(KmipTagResolver.TAG_TEMPLATE_ATTRIBUTE);
        if (templateAttributeObj != null) {
            KmipMessage templateAttribute = (KmipMessage) templateAttributeObj;

            // Extract attributes
            // TODO: Extract attributes from template attribute
        }

        // Generate a unique identifier
        String uniqueIdentifier = UUID.randomUUID().toString();
        log.info("Generated unique identifier: {}", uniqueIdentifier);

        // Create the key
        Map<String, Object> attributes = new HashMap<>();
        String keyId = keyManagementService.createSymmetricKey("AES", length, attributes);
        log.info("Created symmetric key with algorithm: {}, length: {} bits",
            TagValueUtil.getCryptoAlgorithmName(algorithm), length);

        // Build the response payload
        KmipMessage responsePayload = new KmipMessage();
        responsePayload.addField(KmipTagResolver.TAG_OBJECT_TYPE, OBJECT_TYPE_SYMMETRIC_KEY);
        responsePayload.addField(KmipTagResolver.TAG_UNIQUE_IDENTIFIER, keyId);

        return responsePayload;
    }
}
