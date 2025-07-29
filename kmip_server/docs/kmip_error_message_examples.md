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