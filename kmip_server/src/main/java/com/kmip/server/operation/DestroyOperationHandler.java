package com.kmip.server.operation;

import com.kmip.server.core.exception.KmipException;
import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.KmipTagResolver;
import com.kmip.server.protocol.tag.TagValueUtil;
import com.kmip.server.service.KeyManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Handler for KMIP Destroy operation.
 * Permanently invalidates a managed object and renders it unusable.
 *
 * Based on KMIP 2.0 Specification Section 4.23
 * https://docs.oasis-open.org/kmip/kmip-spec/v2.0/os/kmip-spec-v2.0-os.html#_Toc6497785
 */
@Component
public class DestroyOperationHandler implements OperationHandler {

    private static final Logger log = LoggerFactory.getLogger(DestroyOperationHandler.class);

    // Operation code from KMIP 2.0 Specification Table 190 in Section 6.1.9
    private static final int OPERATION_DESTROY = 0x14; // 20 in decimal

    @Autowired
    @Qualifier("inMemoryKms")
    private KeyManagementService keyManagementService;

    @Override
    public KmipMessage handle(KmipMessage requestHeader, KmipMessage requestPayload) throws KmipException {
        log.info("Processing Destroy operation request");

        if (requestPayload == null) {
            throw new KmipException("Missing request payload");
        }

        // Extract the Unique Identifier from the request payload
        Optional<String> uniqueIdentifierOpt = TagValueUtil.getUniqueIdentifier(requestPayload);
        if (!uniqueIdentifierOpt.isPresent()) {
            throw new KmipException("Missing Unique Identifier in Destroy request");
        }

        String uniqueIdentifier = uniqueIdentifierOpt.get();
        log.info("Destroying key with ID: {}", uniqueIdentifier);

        // Call the service to destroy the key
        boolean destroyed = keyManagementService.destroySymmetricKey(uniqueIdentifier);

        if (!destroyed) {
            log.warn("Key with ID {} not found or could not be destroyed", uniqueIdentifier);
            throw new KmipException("Key not found or could not be destroyed: " + uniqueIdentifier);
        }

        // Create the response payload
        // According to KMIP 2.0 spec section 4.23, the response payload contains:
        // - Unique Identifier (required)
        KmipMessage responsePayload = new KmipMessage();

        // Add ONLY the Unique Identifier field
        // The PyKMIP client expects ONLY this field in the response payload
        // This is a deviation from our usual pattern of including Object Type in all response payloads
        responsePayload.addField(KmipTagResolver.TAG_UNIQUE_IDENTIFIER, uniqueIdentifier);

        // NOTE: We are NOT adding the Object Type field for the Destroy operation
        // because the PyKMIP client expects only the Unique Identifier field

        log.info("Key with ID {} successfully destroyed", uniqueIdentifier);
        return responsePayload;
    }

    @Override
    public int getOperationCode() {
        return OPERATION_DESTROY;
    }
}
