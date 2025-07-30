```xml
<ResponseMessage>
    <ResponseHeader>
        <ProtocolVersion>
            <ProtocolVersionMajor>2</ProtocolVersionMajor>
            <ProtocolVersionMinor>1</ProtocolVersionMinor>
        </ProtocolVersion>
        <TimeStamp>2024-01-15T10:30:00Z</TimeStamp>
        <BatchCount>4</BatchCount>
    </ResponseHeader>
    
    <!-- Operation 1: Create Key - SUCCESS -->
    <ResponseBatchItem>
        <Operation>Create</Operation>
        <UniqueBatchItemID>batch_001_item_001</UniqueBatchItemID>
        <ResultStatus>SUCCESS</ResultStatus>
        <ResultReason>OK</ResultReason>
        <ResultMessage>Key created successfully</ResultMessage>
        <ResponsePayload>
            <UniqueIdentifier>550e8400-e29b-41d4-a716-446655440001</UniqueIdentifier>
            <ObjectType>SymmetricKey</ObjectType>
            <!-- Additional key details -->
        </ResponsePayload>
    </ResponseBatchItem>
    
    <!-- Operation 2: Create Key - ERROR -->
    <ResponseBatchItem>
        <Operation>Create</Operation>
        <UniqueBatchItemID>batch_001_item_002</UniqueBatchItemID>
        <ResultStatus>OPERATION_FAILED</ResultStatus>
        <ResultReason>CRYPTOGRAPHIC_FAILURE</ResultReason>
        <ResultMessage>Invalid cryptographic algorithm specified</ResultMessage>
        <ResponsePayload>
            <!-- Empty for errors -->
        </ResponsePayload>
    </ResponseBatchItem>
    
    <!-- Operation 3: Get Key - SUCCESS -->
    <ResponseBatchItem>
        <Operation>Get</Operation>
        <UniqueBatchItemID>batch_001_item_003</UniqueBatchItemID>
        <ResultStatus>SUCCESS</ResultStatus>
        <ResultReason>OK</ResultReason>
        <ResultMessage>Key retrieved successfully</ResultMessage>
        <ResponsePayload>
            <UniqueIdentifier>550e8400-e29b-41d4-a716-446655440002</UniqueIdentifier>
            <ObjectType>SymmetricKey</ObjectType>
            <!-- Key data -->
        </ResponsePayload>
    </ResponseBatchItem>
    
    <!-- Operation 4: Destroy Key - ERROR -->
    <ResponseBatchItem>
        <Operation>Destroy</Operation>
        <UniqueBatchItemID>batch_001_item_004</UniqueBatchItemID>
        <ResultStatus>OPERATION_FAILED</ResultStatus>
        <ResultReason>ITEM_NOT_FOUND</ResultReason>
        <ResultMessage>Key with ID '550e8400-e29b-41d4-a716-446655440003' not found</ResultMessage>
        <ResponsePayload>
            <!-- Empty for errors -->
        </ResponsePayload>
    </ResponseBatchItem>
</ResponseMessage>


<ResponseMessage>
    <ResponseHeader>
        <ProtocolVersion>
            <ProtocolVersionMajor>2</ProtocolVersionMajor>
            <ProtocolVersionMinor>1</ProtocolVersionMinor>
        </ProtocolVersion>
        <TimeStamp>2024-01-15T10:32:00Z</TimeStamp>
        <BatchCount>1</BatchCount>
    </ResponseHeader>
    <ResponseBatchItem>
        <Operation>Destroy</Operation>
        <UniqueBatchItemID>12347</UniqueBatchItemID>
        <ResultStatus>OPERATION_FAILED</ResultStatus>
        <ResultReason>ILLEGAL_OBJECT_STATE</ResultReason>
        <ResultMessage>Key is already in DESTROYED state</ResultMessage>
        <ResponsePayload>
            <!-- Empty payload for error responses -->
        </ResponsePayload>
    </ResponseBatchItem>
</ResponseMessage>


<ResponseMessage>
    <ResponseHeader>
        <ProtocolVersion>
            <ProtocolVersionMajor>2</ProtocolVersionMajor>
            <ProtocolVersionMinor>1</ProtocolVersionMinor>
        </ProtocolVersion>
        <TimeStamp>2024-01-15T10:31:00Z</TimeStamp>
        <BatchCount>1</BatchCount>
    </ResponseHeader>
    <ResponseBatchItem>
        <Operation>Get</Operation>
        <UniqueBatchItemID>12346</UniqueBatchItemID>
        <ResultStatus>OPERATION_FAILED</ResultStatus>
        <ResultReason>ITEM_NOT_FOUND</ResultReason>
        <ResultMessage>Key with ID '550e8400-e29b-41d4-a716-446655440000' not found</ResultMessage>
        <ResponsePayload>
            <!-- Empty payload for error responses -->
        </ResponsePayload>
    </ResponseBatchItem>
</ResponseMessage>
```


```xml
<!-- Client Request -->
<RequestBatchItem>
    <UniqueBatchItemID>client_generated_id_001</UniqueBatchItemID>
    <Operation>Create</Operation>
    <!-- ... -->
</RequestBatchItem>

<!-- Server Response -->
<ResponseBatchItem>
    <UniqueBatchItemID>client_generated_id_001</UniqueBatchItemID>  <!-- Echo back -->
    <Operation>Create</Operation>
    <!-- ... -->
</ResponseBatchItem>

```

```java
// Example generation patterns:
String generateUniqueBatchItemID(String batchId, int itemIndex) {
    return String.format("batch_%s_item_%03d", batchId, itemIndex);
}

// Or using UUID:
String generateUniqueBatchItemID() {
    return "item_" + UUID.randomUUID().toString().substring(0, 8);
}

// Or timestamp-based:
String generateUniqueBatchItemID() {
    return "item_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
}

// For batch processing
String generateUniqueBatchItemID(String batchId, int sequenceNumber) {
    return String.format("%s_%03d", batchId, sequenceNumber);
}
```


```java
public class BatchProcessor {
    
    public KmipResponse processBatch(KmipRequest request) {
        List<ResponseBatchItem> responseItems = new ArrayList<>();
        int batchCount = request.getBatchItems().size();
        
        for (int i = 0; i < batchCount; i++) {
            RequestBatchItem requestItem = request.getBatchItems().get(i);
            ResponseBatchItem responseItem = new ResponseBatchItem();
            
            // Preserve the UniqueBatchItemID from request
            responseItem.setUniqueBatchItemID(requestItem.getUniqueBatchItemID());
            responseItem.setOperation(requestItem.getOperation());
            
            try {
                // Process the operation
                OperationResult result = processOperation(requestItem);
                responseItem.setResultStatus(ResultStatus.SUCCESS);
                responseItem.setResultReason(ResultReason.OK);
                responseItem.setResponsePayload(result.getPayload());
            } catch (KmipException ex) {
                responseItem.setResultStatus(ResultStatus.OPERATION_FAILED);
                responseItem.setResultReason(ex.getReason());
                responseItem.setResultMessage(ex.getMessage());
            }
            
            responseItems.add(responseItem);
        }
        
        return new KmipResponse(batchCount, responseItems);
    }
}
```

```java
private ResponseBatchItem processSingleBatchItem(RequestBatchItem requestItem, int itemIndex) {
    ResponseBatchItem responseItem = new ResponseBatchItem();
    
    try {
        // Try to extract UniqueBatchItemID first
        String uniqueBatchItemID = extractUniqueBatchItemID(requestItem);
        responseItem.setUniqueBatchItemID(uniqueBatchItemID);
        
        // Continue with parsing and processing...
        
    } catch (ParsingException ex) {
        // If we can't extract UniqueBatchItemID, use position-based ID
        String fallbackID = "item_" + (itemIndex + 1);
        responseItem.setUniqueBatchItemID(fallbackID);
        
        responseItem.setResultStatus(ResultStatus.OPERATION_FAILED);
        responseItem.setResultReason(ResultReason.INVALID_MESSAGE);
        responseItem.setResultMessage("Failed to parse batch item: " + ex.getMessage());
    }
    
    return responseItem;
}

private String extractUniqueBatchItemID(RequestBatchItem requestItem) {
    try {
        return requestItem.getUniqueBatchItemID();
    } catch (Exception ex) {
        throw new ParsingException("Cannot extract UniqueBatchItemID: " + ex.getMessage());
    }
}
```

```java
// Base KMIP Exception
public abstract class KmipException extends RuntimeException {
    private final KmipResultStatus status;
    private final KmipResultReason reason;
    private final String errorContext; // e.g., "header", "batch_item_2", "encoding"

    public KmipException(String message, KmipResultStatus status, 
                        KmipResultReason reason, String errorContext) {
        super(message);
        this.status = status;
        this.reason = reason;
        this.errorContext = errorContext;
    }
    
    // getters...
}

// Specific exception types
public class KmipHeaderParsingException extends KmipException {
    public KmipHeaderParsingException(String message) {
        super(message, KmipResultStatus.OPERATION_FAILED, 
              KmipResultReason.INVALID_MESSAGE, "header");
    }
}

public class KmipBatchItemParsingException extends KmipException {
    private final int itemIndex;
    
    public KmipBatchItemParsingException(String message, int itemIndex) {
        super(message, KmipResultStatus.OPERATION_FAILED, 
              KmipResultReason.INVALID_MESSAGE, "batch_item_" + itemIndex);
        this.itemIndex = itemIndex;
    }
}

public class KmipEncodingException extends KmipException {
    public KmipEncodingException(String message) {
        super(message, KmipResultStatus.OPERATION_FAILED, 
              KmipResultReason.ENCODING_ERROR, "encoding");
    }
}


public ByteBuffer handleRequest(ByteBuffer requestBuffer) {
    try {
        // Stage 1: Parse Request Header
        RequestMessage_v2_1 requestMessage = parseRequestHeader(requestBuffer);
        
        // Stage 2: Process Batch Items
        List<ResponseBatchItem> responseBatchItems = processBatchItems(requestBuffer, requestMessage);
        
        // Stage 3: Create Response Message
        ResponseMessage_v2_1 responseMessage = createResponseMessage(requestMessage, responseBatchItems);
        
        // Stage 4: Encode Response
        return encodeResponseMessage(responseMessage);
        
    } catch (KmipHeaderParsingException ex) {
        // Critical header parsing error - can't create proper response
        log.error("Header parsing error: {}", ex.getMessage(), ex);
        return createCriticalErrorResponse();
        
    } catch (Exception ex) {
        // Unexpected critical error
        log.error("Critical error in request handling: {}", ex.getMessage(), ex);
        return createCriticalErrorResponse();
    }
}

private RequestMessage_v2_1 parseRequestHeader(ByteBuffer requestBuffer) {
    try {
        // Parse header with minimal validation
        RequestMessage_v2_1 requestMessage = new RequestMessage_v2_1();
        
        // Extract essential header fields that are needed for response
        ProtocolVersion protocolVersion = extractProtocolVersion(requestBuffer);
        requestMessage.setProtocolVersion(protocolVersion);
        
        TimeStamp timeStamp = extractTimeStamp(requestBuffer);
        requestMessage.setTimeStamp(timeStamp);
        
        int batchCount = extractBatchCount(requestBuffer);
        requestMessage.setBatchCount(batchCount);
        
        // Extract other header fields...
        
        return requestMessage;
        
    } catch (Exception ex) {
        throw new KmipHeaderParsingException("Failed to parse request header: " + ex.getMessage());
    }
}

private List<ResponseBatchItem> processBatchItems(ByteBuffer requestBuffer, 
                                                 RequestMessage_v2_1 requestMessage) {
    List<ResponseBatchItem> responseBatchItems = new ArrayList<>();
    int expectedBatchCount = requestMessage.getBatchCount();
    
    try {
        // Try to parse all batch items
        List<RequestBatchItem> requestBatchItems = parseAllBatchItems(requestBuffer, expectedBatchCount);
        
        // Process each batch item
        for (int i = 0; i < requestBatchItems.size(); i++) {
            RequestBatchItem requestItem = requestBatchItems.get(i);
            ResponseBatchItem responseItem = processSingleBatchItem(requestItem, i);
            responseBatchItems.add(responseItem);
        }
        
    } catch (BatchItemParsingException ex) {
        // Handle partial batch item parsing failure
        log.warn("Partial batch parsing failure: {}", ex.getMessage());
        
        // Create error responses for failed items
        for (int i = ex.getFailedItemIndex(); i < expectedBatchCount; i++) {
            ResponseBatchItem errorItem = createErrorResponseItem(i, ex.getMessage());
            responseBatchItems.add(errorItem);
        }
    }
    
    return responseBatchItems;
}

private ResponseBatchItem processSingleBatchItem(RequestBatchItem requestItem, int itemIndex) {
    ResponseBatchItem responseItem = new ResponseBatchItem();
    
    try {
        // Extract UniqueBatchItemID (handle parsing errors)
        String uniqueBatchItemID = extractUniqueBatchItemID(requestItem);
        responseItem.setUniqueBatchItemID(uniqueBatchItemID);
        
        // Extract operation
        Operation operation = extractOperation(requestItem);
        responseItem.setOperation(operation);
        
        // Parse and process the batch item
        ParsedBatchItem parsedItem = parseBatchItem(requestItem);
        OperationResult result = executeOperation(parsedItem);
        
        // Set success response
        responseItem.setResultStatus(ResultStatus.SUCCESS);
        responseItem.setResultReason(ResultReason.OK);
        responseItem.setResponsePayload(result.getPayload());
        
    } catch (UniqueBatchItemIDParsingException ex) {
        // Use fallback ID
        String fallbackID = "item_" + (itemIndex + 1);
        responseItem.setUniqueBatchItemID(fallbackID);
        responseItem.setOperation(Operation.UNKNOWN); // or null
        
        responseItem.setResultStatus(ResultStatus.OPERATION_FAILED);
        responseItem.setResultReason(ResultReason.INVALID_MESSAGE);
        responseItem.setResultMessage("Cannot extract UniqueBatchItemID: " + ex.getMessage());
        
    } catch (BatchItemParsingException ex) {
        // Handle other parsing errors
        String uniqueBatchItemID = getUniqueBatchItemIDOrDefault(requestItem, itemIndex);
        responseItem.setUniqueBatchItemID(uniqueBatchItemID);
        responseItem.setOperation(getOperationOrDefault(requestItem));
        
        responseItem.setResultStatus(ResultStatus.OPERATION_FAILED);
        responseItem.setResultReason(ResultReason.INVALID_MESSAGE);
        responseItem.setResultMessage("Batch item parsing error: " + ex.getMessage());
        
    } catch (KmipProcessingException ex) {
        // Handle processing errors
        String uniqueBatchItemID = getUniqueBatchItemIDOrDefault(requestItem, itemIndex);
        responseItem.setUniqueBatchItemID(uniqueBatchItemID);
        responseItem.setOperation(getOperationOrDefault(requestItem));
        
        responseItem.setResultStatus(ResultStatus.OPERATION_FAILED);
        responseItem.setResultReason(ex.getReason());
        responseItem.setResultMessage(ex.getMessage());
    }
    
    return responseItem;
}

private ByteBuffer encodeResponseMessage(ResponseMessage_v2_1 responseMessage) {
    try {
        // Encode the response message
        return encoder.encode(responseMessage);
        
    } catch (Exception ex) {
        log.error("Encoding error: {}", ex.getMessage(), ex);
        
        // Try to create a minimal error response
        try {
            ResponseMessage_v2_1 errorResponse = createMinimalErrorResponse();
            return encoder.encode(errorResponse);
        } catch (Exception encodingEx) {
            // If even minimal encoding fails, return null or throw
            log.error("Critical encoding failure: {}", encodingEx.getMessage(), encodingEx);
            throw new KmipEncodingException("Failed to encode response: " + encodingEx.getMessage());
        }
    }
}

// Helper methods
private String getUniqueBatchItemIDOrDefault(RequestBatchItem requestItem, int itemIndex) {
    try {
        return requestItem.getUniqueBatchItemID();
    } catch (Exception ex) {
        return "item_" + (itemIndex + 1);
    }
}

private Operation getOperationOrDefault(RequestBatchItem requestItem) {
    try {
        return requestItem.getOperation();
    } catch (Exception ex) {
        return Operation.UNKNOWN; // or null
    }
}

private ResponseBatchItem createErrorResponseItem(int itemIndex, String errorMessage) {
    ResponseBatchItem errorItem = new ResponseBatchItem();
    errorItem.setUniqueBatchItemID("item_" + (itemIndex + 1));
    errorItem.setOperation(Operation.UNKNOWN);
    errorItem.setResultStatus(ResultStatus.OPERATION_FAILED);
    errorItem.setResultReason(ResultReason.INVALID_MESSAGE);
    errorItem.setResultMessage(errorMessage);
    return errorItem;
}


private ByteBuffer createCriticalErrorResponse() {
    try {
        // Create minimal error response with default values
        ResponseMessage_v2_1 errorResponse = new ResponseMessage_v2_1();
        
        // Set minimal header
        ResponseHeader header = new ResponseHeader();
        header.setProtocolVersion(new ProtocolVersion(2, 1)); // Default version
        header.setTimeStamp(new TimeStamp(System.currentTimeMillis()));
        header.setBatchCount(1);
        errorResponse.setResponseHeader(header);
        
        // Create single error batch item
        ResponseBatchItem errorItem = new ResponseBatchItem();
        errorItem.setUniqueBatchItemID("error_item");
        errorItem.setOperation(Operation.UNKNOWN);
        errorItem.setResultStatus(ResultStatus.OPERATION_FAILED);
        errorItem.setResultReason(ResultReason.GENERAL_FAILURE);
        errorItem.setResultMessage("Critical server error");
        
        errorResponse.setResponseBatchItems(Arrays.asList(errorItem));
        
        return encoder.encode(errorResponse);
        
    } catch (Exception ex) {
        log.error("Failed to create critical error response: {}", ex.getMessage(), ex);
        // Return null or throw - client will see connection error
        return null;
    }
}
```