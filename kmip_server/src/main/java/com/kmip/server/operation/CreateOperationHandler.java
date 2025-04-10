package com.kmip.server.operation;

import com.kmip.server.core.exception.KmipException;
import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.KmipTagResolver;
import com.kmip.server.protocol.tag.TagValueUtil;
import com.kmip.server.service.KeyManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap; // For placeholder attributes map
import java.util.Map; // For placeholder attributes map
import java.util.Optional;

@Component
public class CreateOperationHandler implements OperationHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateOperationHandler.class);
    private static final int OPERATION_CREATE = 1;
    private static final int OBJECT_TYPE_SYMMETRIC_KEY = 2;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("inMemoryKms")
    private KeyManagementService keyManagementService; // Explicitly use InMemoryKMS

    @Override
    public KmipMessage handle(KmipMessage requestHeader, KmipMessage requestPayload) throws KmipException {
        log.info("Handling CREATE operation...");

        // 1. Extract Object Type
        Optional<Integer> objectTypeOpt = TagValueUtil.getObjectType(requestPayload);
        if (!objectTypeOpt.isPresent()) {
            throw new KmipException("Missing Object Type...");
        }
        Integer objectType = objectTypeOpt.get();
        log.info("Requested object type code: {}", objectType);

        if (objectType != OBJECT_TYPE_SYMMETRIC_KEY) {
            throw new KmipException("Unsupported object type for Create operation: " + objectType);
        }

        // 2. Extract Attributes from Template-Attribute (if present)
        Integer algoEnum = 3; // Default to AES (Enum 3)
        Integer keyLength = 256; // Default to 256 bits
        Integer usageMask = 0; // Default to no usage mask

        Optional<KmipMessage> templateAttributeOpt = TagValueUtil.getTemplateAttributeStructure(requestPayload);
        if (templateAttributeOpt.isPresent()) {
            KmipMessage templateAttribute = templateAttributeOpt.get();
            algoEnum = TagValueUtil.getCryptographicAlgorithm(templateAttribute).orElse(algoEnum);
            keyLength = TagValueUtil.getCryptographicLength(templateAttribute).orElse(keyLength);
            usageMask = TagValueUtil.getCryptographicUsageMask(templateAttribute).orElse(usageMask);
        } else {
            log.info("No Template-Attribute provided, using default values - Algorithm: AES (3), Length: 256, UsageMask: 0");
        }

        // Map KMIP Algorithm Enum to JCA Algorithm String
        String jcaAlgorithm = mapKmipAlgorithmToJca(algoEnum);

        log.info("Using Attributes - Algorithm: {} (Enum {}), Length: {}, UsageMask: {}",
                 jcaAlgorithm, algoEnum, keyLength, usageMask);

        // 3. Call KeyManagementService to create the key
        String uniqueID = keyManagementService.createSymmetricKey(jcaAlgorithm, keyLength, new HashMap<>());
        log.info("Key created via KeyManagementService. Assigned Unique ID: {}", uniqueID);

        // 4. Build Response Payload with strict ordering (Section 6.2 of KMIP 2.0 spec)
        KmipMessage responsePayload = new KmipMessage();

        // Object Type MUST be first (Required)
        log.info("Adding Object Type (0x420057): {} (SYMMETRIC_KEY)", OBJECT_TYPE_SYMMETRIC_KEY);
        responsePayload.addField(KmipTagResolver.TAG_OBJECT_TYPE, OBJECT_TYPE_SYMMETRIC_KEY);

        // Then add other fields
        log.info("Adding Unique Identifier (0x420094): {}", uniqueID);
        responsePayload.addField(KmipTagResolver.TAG_UNIQUE_IDENTIFIER, uniqueID);

        // Temporarily remove Template-Attribute to simplify the response
        // This will help us determine if the issue is with the Template-Attribute structure
        log.info("Simplified response: omitting Template-Attribute for troubleshooting");

        // Verify response payload structure
        if (!responsePayload.getFields().containsKey(KmipTagResolver.TAG_OBJECT_TYPE)) {
            log.error("Object Type field is missing from response payload after construction");
            throw new KmipException("Response payload missing required Object Type field");
        }

        // Debug log the exact order of fields
        log.info("Final response payload field order:");
        responsePayload.getFields().forEach((tag, values) -> {
            log.info("Tag: 0x{} -> {}", Integer.toHexString(tag), values);
        });

        return responsePayload;
    }

    @Override
    public int getOperationCode() {
        return OPERATION_CREATE;
    }

    // Simple mapping - enhance later based on KMIP spec Table 405
    private String mapKmipAlgorithmToJca(int kmipAlgorithmEnum) throws KmipException {
        switch (kmipAlgorithmEnum) {
            case 3: return "AES";
            // TODO: Add other mappings (DES3, Blowfish, etc.)
            default: throw new KmipException("Unsupported KMIP Cryptographic Algorithm Enum: " + kmipAlgorithmEnum);
        }
    }
}