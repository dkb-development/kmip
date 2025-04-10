package com.kmip.server.operation;

import com.kmip.server.core.exception.KmipException;
import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.KmipTagResolver;
import com.kmip.server.protocol.tag.TagValueUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles KMIP requests by delegating to the appropriate operation handler.
 */
@Component
public class KmipRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(KmipRequestHandler.class);
    private final Map<Integer, OperationHandler> operationHandlers = new HashMap<>();

    /**
     * Creates a new KmipRequestHandler with the specified operation handlers.
     *
     * @param handlers The operation handlers
     */
    @Autowired
    public KmipRequestHandler(List<OperationHandler> handlers) {
        for (OperationHandler handler : handlers) {
            operationHandlers.put(handler.getOperationCode(), handler);
            log.info("Registered operation handler for operation code 0x{}: {}",
                Integer.toHexString(handler.getOperationCode()), handler.getClass().getSimpleName());
        }
    }

    /**
     * Handles a KMIP request.
     *
     * @param request The KMIP request message
     * @return The KMIP response message
     */
    public KmipMessage handleRequest(KmipMessage request) {
        try {
            // Extract the operation from the request
            int operation = extractOperation(request);
            log.info("Handling request for operation: {} (0x{})",
                TagValueUtil.getOperationName(operation), Integer.toHexString(operation));

            // Find the appropriate handler
            OperationHandler handler = operationHandlers.get(operation);
            if (handler == null) {
                throw new KmipException("Unsupported operation: " + TagValueUtil.getOperationName(operation),
                    TagValueUtil.RESULT_REASON_OPERATION_NOT_SUPPORTED);
            }

            // Handle the request
            KmipMessage responsePayload = handler.handle(request);

            // Build the response message
            return buildResponseMessage(request, responsePayload, TagValueUtil.RESULT_STATUS_SUCCESS, null);
        } catch (KmipException e) {
            log.error("KMIP operation failed: {}", e.getMessage());
            return buildResponseMessage(request, null, TagValueUtil.RESULT_STATUS_OPERATION_FAILED, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error handling KMIP request", e);
            return buildResponseMessage(request, null, TagValueUtil.RESULT_STATUS_OPERATION_FAILED,
                "Internal server error: " + e.getMessage());
        }
    }

    private int extractOperation(KmipMessage request) {
        // Extract the operation from the batch item
        List<Object> batchItems = request.getFieldValues(KmipTagResolver.TAG_REQUEST_BATCH_ITEM);
        if (batchItems.isEmpty()) {
            throw new KmipException("No batch items found in request");
        }

        KmipMessage batchItem = (KmipMessage) batchItems.get(0);
        Object operationObj = batchItem.getFieldValue(KmipTagResolver.TAG_OPERATION);
        if (operationObj == null) {
            throw new KmipException("No operation found in batch item");
        }

        return (Integer) operationObj;
    }

    private KmipMessage buildResponseMessage(KmipMessage request, KmipMessage responsePayload,
                                            int resultStatus, String resultMessage) {
        KmipMessage response = new KmipMessage();

        // Build the response header
        KmipMessage responseHeader = new KmipMessage();

        // Add protocol version
        KmipMessage protocolVersion = new KmipMessage();
        protocolVersion.addField(KmipTagResolver.TAG_PROTOCOL_VERSION_MAJOR, 2); // KMIP 2.0
        protocolVersion.addField(KmipTagResolver.TAG_PROTOCOL_VERSION_MINOR, 0);
        responseHeader.addField(KmipTagResolver.TAG_PROTOCOL_VERSION, protocolVersion);

        // Add timestamp
        responseHeader.addField(KmipTagResolver.TAG_TIME_STAMP, Instant.now());

        // Add batch count
        responseHeader.addField(KmipTagResolver.TAG_BATCH_COUNT, 1);

        // Add the response header to the response
        response.addField(KmipTagResolver.TAG_RESPONSE_HEADER, responseHeader);

        // Build the batch item
        KmipMessage batchItem = new KmipMessage();

        // Extract the operation from the request
        List<Object> requestBatchItems = request.getFieldValues(KmipTagResolver.TAG_REQUEST_BATCH_ITEM);
        if (!requestBatchItems.isEmpty()) {
            KmipMessage requestBatchItem = (KmipMessage) requestBatchItems.get(0);
            Object operationObj = requestBatchItem.getFieldValue(KmipTagResolver.TAG_OPERATION);
            if (operationObj != null) {
                batchItem.addField(KmipTagResolver.TAG_OPERATION, operationObj);
            }
        }

        // Add result status
        batchItem.addField(KmipTagResolver.TAG_RESULT_STATUS, resultStatus);

        // Add result message if provided
        if (resultMessage != null) {
            batchItem.addField(KmipTagResolver.TAG_RESULT_MESSAGE, resultMessage);
        }

        // Add response payload if provided
        if (responsePayload != null) {
            batchItem.addField(KmipTagResolver.TAG_RESPONSE_PAYLOAD, responsePayload);
        }

        // Add the batch item to the response
        response.addField(KmipTagResolver.TAG_RESPONSE_BATCH_ITEM, batchItem);

        return response;
    }
}
