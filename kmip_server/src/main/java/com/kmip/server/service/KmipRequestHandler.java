package com.kmip.server.service;

import com.kmip.server.kmip.KmipMessage;
import com.kmip.server.kmip.TagValueUtil;
import com.kmip.server.kmip.KmipTagResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service // Mark as a Spring service
public class KmipRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(KmipRequestHandler.class);

    // --- Enums for Status/Reason (Defined at the top) ---
    public enum ResultStatus {
        SUCCESS(0x00000000),
        OPERATION_FAILED(0x00000001);
        // Add other statuses as needed

        private final int code;
        ResultStatus(int code) { this.code = code; }
        public int getCode() { return code; }

        // Helper to use the enum instance directly in addField if needed
        // (Requires KmipEncoder to handle the enum type or getCode() called explicitly)
        // @Override public String toString() { return Integer.toString(code); }
    }

    public enum ResultReason {
        // Subset of KMIP reasons - add more as needed (Table 336 in KMIP 1.4 Spec)
        ITEM_NOT_FOUND(0x00000001),
        OPERATION_NOT_SUPPORTED(0x00000006),
        INVALID_MESSAGE(0x00000009),
        PERMISSION_DENIED(0x0000000A),
        GENERAL_FAILURE(0x0000000F);

        private final int code;
        ResultReason(int code) { this.code = code; }
        public int getCode() { return code; }
         // Helper for logging/debugging
        // @Override public String toString() { return name() + "(0x" + Integer.toHexString(code) + ")"; }
    }
    // --- End Enums ---

    private final Map<Integer, OperationHandler> operationHandlerMap = new HashMap<>();

    // Inject all beans that implement OperationHandler
    @Autowired
    private List<OperationHandler> handlers;

    // Build the map of operation code to handler after injection
    @PostConstruct
    public void initHandlers() {
        log.info("Initializing KMIP operation handlers...");
        for (OperationHandler handler : handlers) {
            log.info("Registering handler for operation code: {}", handler.getOperationCode());
            operationHandlerMap.put(handler.getOperationCode(), handler);
        }
        log.info("KMIP operation handlers initialized.");
    }

    /**
     * Processes a parsed KMIP request message.
     *
     * @param requestMessage The parsed top-level request message.
     * @return A KmipMessage representing the full response message.
     */
    public KmipMessage processRequest(KmipMessage requestMessage) {
        // Initial checks
        if (requestMessage == null || requestMessage.getFields() == null || requestMessage.getFields().isEmpty()) {
            log.error("KmipRequestHandler received a null or empty requestMessage object.");
            return buildErrorResponse("Invalid Request Message Object Received", null, ResultReason.INVALID_MESSAGE);
        }
        log.debug("KmipRequestHandler processing request. Top-level tags: {}", requestMessage.getFields().keySet());

        // Extract header FIRST - needed for error responses too
        KmipMessage requestHeader = TagValueUtil.getRequestHeader(requestMessage).orElse(null);
        KmipMessage protocolVersion = null; // Initialize protocolVersion
        if (requestHeader == null) {
            log.error("Request message is missing the Request Header (Tag {}).", KmipTagResolver.TAG_REQUEST_HEADER);
            return buildErrorResponse("Missing Request Header", null, ResultReason.INVALID_MESSAGE);
        } else {
            // Extract Protocol Version from header (for response)
            protocolVersion = TagValueUtil.getProtocolVersionStructure(requestHeader).orElse(null);
        }

        // TODO: Handle multiple batch items based on header Batch Count
        // For now, assume one batch item
        KmipMessage batchItem = TagValueUtil.getBatchItem(requestMessage).orElse(null);
        if (batchItem == null) {
            log.error("Request message does not contain a Batch Item (Tag {} or {}). Check message structure.", KmipTagResolver.TAG_REQUEST_BATCH_ITEM, KmipTagResolver.TAG_RESPONSE_BATCH_ITEM);
            return buildErrorResponse("Missing Batch Item", protocolVersion, ResultReason.INVALID_MESSAGE); // Use extracted PV
        }

        Integer operationCode = TagValueUtil.getOperation(batchItem).orElse(null);
        if (operationCode == null) {
            log.error("Batch item does not contain an Operation (Tag {}).", KmipTagResolver.TAG_OPERATION);
            return buildErrorResponse("Missing Operation in Batch Item", protocolVersion, ResultReason.INVALID_MESSAGE);
        }

        OperationHandler handler = operationHandlerMap.get(operationCode);
        if (handler == null) {
            log.error("No handler found for operation code: {}", operationCode);
            return buildErrorResponse("Unsupported Operation: " + operationCode, protocolVersion, ResultReason.OPERATION_NOT_SUPPORTED);
        }

        log.info("Routing request to handler for operation code: {}", operationCode);
        KmipMessage requestPayload = TagValueUtil.getRequestPayload(batchItem).orElse(null);

        // TODO: Validate requestHeader and requestPayload structure before passing

        try {
            // Delegate to the specific handler
            // Pass the original requestHeader, not the duplicate declaration
            KmipMessage responsePayload = handler.handle(requestHeader, requestPayload);

            // Build the full success response message
            return buildSuccessResponse(protocolVersion, responsePayload, operationCode);

        } catch (KmipException e) {
            log.error("Error handling operation {}: {}", operationCode, e.getMessage(), e);
            ResultReason reason = e.getResultReason() != null ? e.getResultReason() : ResultReason.GENERAL_FAILURE;
            return buildErrorResponse(e.getMessage(), protocolVersion, reason);
        } catch (Exception e) {
            log.error("Unexpected error handling operation {}: {}", operationCode, e.getMessage(), e);
            return buildErrorResponse("Internal Server Error", protocolVersion, ResultReason.GENERAL_FAILURE);
        }
    }

    // --- Updated Response Builders ---

    private KmipMessage buildSuccessResponse(KmipMessage protocolVersion, KmipMessage responsePayload, int operationCode) {
        log.debug("Building SUCCESS response for operation {}", operationCode);

        // Validate response payload
        if (responsePayload != null && !responsePayload.getFields().containsKey(KmipTagResolver.TAG_OBJECT_TYPE)) {
            log.error("Response payload is missing required Object Type field");
            return buildErrorResponse("Internal Error: Missing Object Type", protocolVersion, ResultReason.GENERAL_FAILURE);
        }

        KmipMessage responseMessage = new KmipMessage();

        // 1. Build Response Header (Table 376)
        KmipMessage responseHeader = new KmipMessage();

        // Protocol Version (Table 383) - MUST be first field
        KmipMessage protocolVersionStructure = new KmipMessage();
        protocolVersionStructure.addField(KmipTagResolver.TAG_PROTOCOL_VERSION_MAJOR, 2); // KMIP v2.0
        protocolVersionStructure.addField(KmipTagResolver.TAG_PROTOCOL_VERSION_MINOR, 0);
        responseHeader.addField(KmipTagResolver.TAG_PROTOCOL_VERSION, protocolVersionStructure);

        // Time Stamp (Table 387) - MUST be second field
        responseHeader.addField(KmipTagResolver.TAG_TIMESTAMP, Instant.now());

        // Batch Count (Table 378) - MUST be third field
        responseHeader.addField(KmipTagResolver.TAG_BATCH_COUNT, 1);

        // Add Response Header to Response Message
        responseMessage.addField(KmipTagResolver.TAG_RESPONSE_HEADER, responseHeader);

        // 2. Build Response Batch Item (Table 377)
        KmipMessage responseBatchItem = new KmipMessage();

        // Operation (Table 382) - MUST be first field
        responseBatchItem.addField(KmipTagResolver.TAG_OPERATION, operationCode);

        // Result Status (Table 386) - MUST be second field
        responseBatchItem.addField(KmipTagResolver.TAG_RESULT_STATUS, ResultStatus.SUCCESS.getCode());

        // Result Message - Add a success message for the client
        // This is CRITICAL for PyKMIP client compatibility - it expects this field
        String successMessage = "Operation completed successfully";
        log.info("Adding result message to response: {}", successMessage);
        responseBatchItem.addField(KmipTagResolver.TAG_RESULT_MESSAGE, successMessage);

        // Response Payload (Table 379) - MUST be fourth field if present
        if (responsePayload != null && !responsePayload.getFields().isEmpty()) {
            log.debug("Adding response payload to batch item. Fields present: {}", responsePayload.getFields().keySet());
            responseBatchItem.addField(KmipTagResolver.TAG_RESPONSE_PAYLOAD, responsePayload);
        } else {
            log.debug("No response payload provided by handler for successful operation {}.", operationCode);
        }

        // Add Response Batch Item to Response Message
        responseMessage.addField(KmipTagResolver.TAG_RESPONSE_BATCH_ITEM, responseBatchItem);

        return responseMessage;
    }

    private KmipMessage buildErrorResponse(String errorMessage, KmipMessage protocolVersion, ResultReason reason) {
        log.warn("Building ERROR response: Status={}, Reason={}, Message='{}'", ResultStatus.OPERATION_FAILED, reason, errorMessage);
        KmipMessage responseMessage = new KmipMessage();

        // 1. Build Response Header (Best effort)
        KmipMessage responseHeader = new KmipMessage();
        if (protocolVersion != null) {
            responseHeader.addField(KmipTagResolver.TAG_PROTOCOL_VERSION, protocolVersion);
        } else {
            log.warn("Cannot include ProtocolVersion in error response header as it was missing from request.");
        }
        responseHeader.addField(KmipTagResolver.TAG_TIMESTAMP, Instant.now());
        responseHeader.addField(KmipTagResolver.TAG_BATCH_COUNT, 1);
        responseMessage.addField(KmipTagResolver.TAG_RESPONSE_HEADER, responseHeader);

        // 2. Build Response Batch Item
        KmipMessage responseBatchItem = new KmipMessage();

        // Operation MUST be first field - use CREATE (1) as a default valid operation
        responseBatchItem.addField(KmipTagResolver.TAG_OPERATION, 1); // Use CREATE (1) as default

        // Result Status MUST be second field
        responseBatchItem.addField(KmipTagResolver.TAG_RESULT_STATUS, ResultStatus.OPERATION_FAILED.getCode());

        // Result Reason MUST be third field if present
        if (reason != null) {
            responseBatchItem.addField(KmipTagResolver.TAG_RESULT_REASON, reason.getCode());
        }

        // Result Message MUST be fourth field if present
        if (errorMessage != null && !errorMessage.isEmpty()) {
            responseBatchItem.addField(KmipTagResolver.TAG_RESULT_MESSAGE, errorMessage);
        }

        responseMessage.addField(KmipTagResolver.TAG_RESPONSE_BATCH_ITEM, responseBatchItem);

        return responseMessage;
    }
     // --- End Updated Builders ---
}