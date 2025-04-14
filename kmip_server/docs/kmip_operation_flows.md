# KMIP Operation Flows

This document provides a detailed flow map for the Create, Get, and Destroy operations in the KMIP server. It shows how a request flows through the various classes and methods from when it arrives from a client until the response is sent back.

## Common Flow for All Operations

1. **Client Request Arrival**
   - The client sends a KMIP request to the server using the PyKMIP client library
   - The request is sent over TLS to port 5696 (default KMIP port)

2. **Server Connection Handling**
   - `KmipTcpServer.start()` - Listens for incoming connections
   - `KmipTcpServer.handleClient(Socket clientSocket)` - Accepts the connection and processes the request
   - Reads the raw bytes from the client socket

3. **Message Parsing**
   - `KmipParser.parse(byte[] data, int length)` - Parses the raw bytes into a structured `KmipMessage`
   - `KmipParser.parseStructure(ByteBuffer buffer)` - Recursively parses the TTLV-encoded message
   - `KmipParser.parseValue(byte type, ByteBuffer valueBuffer, String tagString)` - Parses individual values based on their type

4. **Request Processing**
   - `KmipRequestHandler.processRequest(KmipMessage requestMessage)` - Processes the parsed request
   - Extracts the operation code from the request
   - Looks up the appropriate `OperationHandler` for the operation code
   - Delegates to the specific handler

5. **Response Encoding**
   - `KmipEncoder.encode(KmipMessage message, int rootTag)` - Encodes the response message into TTLV format
   - `KmipTcpServer.handleClient()` - Sends the encoded response back to the client

## Operation-Specific Flows

### 1. Create Symmetric Key Operation (Operation Code: 0x01)

1. **Request Handling**
   - `KmipRequestHandler.processRequest()` identifies the Create operation (code 0x01)
   - Delegates to `CreateOperationHandler.handle(KmipMessage requestHeader, KmipMessage requestPayload)`

2. **Request Processing in CreateOperationHandler**
   - `CreateOperationHandler.handle()` extracts parameters:
     - Object Type (must be Symmetric Key - 0x02)
     - Algorithm (e.g., AES)
     - Key Length (e.g., 256 bits)
     - Usage Mask (e.g., Encrypt, Decrypt)

3. **Key Creation**
   - `CreateOperationHandler` calls `KeyManagementService.createSymmetricKey(String algorithm, int keyLength, Map<String, Object> attributes)`
   - Implementation is provided by `InMemoryKeyManagementService.createSymmetricKey()`
   - `InMemoryKeyManagementService` uses Java's `KeyGenerator` to create a symmetric key
   - Generates a UUID for the key and stores the key in an in-memory map

4. **Response Building**
   - `CreateOperationHandler` builds a response payload with:
     - Object Type (0x420057) = Symmetric Key (0x02)
     - Unique Identifier (0x420094) = Generated UUID
   - `KmipRequestHandler.buildSuccessResponse()` builds the complete response message
   - Adds standard fields like Protocol Version, Timestamp, Batch Count
   - Sets Result Status to Success (0x00)

5. **Response Encoding and Sending**
   - `KmipEncoder.encode()` encodes the response message into TTLV format
   - `KmipTcpServer.handleClient()` sends the encoded response back to the client

### 2. Get Symmetric Key Operation (Operation Code: 0x0A)

1. **Request Handling**
   - `KmipRequestHandler.processRequest()` identifies the Get operation (code 0x0A)
   - Delegates to `GetOperationHandler.handle(KmipMessage requestHeader, KmipMessage requestPayload)`

2. **Request Processing in GetOperationHandler**
   - `GetOperationHandler.handle()` extracts the Unique Identifier from the request payload

3. **Key Retrieval**
   - `GetOperationHandler` calls `KeyManagementService.getSymmetricKey(String uniqueID)`
   - Implementation is provided by `InMemoryKeyManagementService.getSymmetricKey()`
   - Looks up the key in the in-memory map using the Unique Identifier
   - Returns the key if found, or an empty Optional if not found

4. **Response Building**
   - `GetOperationHandler` builds a response payload with:
     - Object Type (0x420057) = Symmetric Key (0x02)
     - Unique Identifier (0x420094) = Requested UUID
     - Symmetric Key (0x42008F) = Key material and attributes
   - `KmipRequestHandler.buildSuccessResponse()` builds the complete response message
   - Adds standard fields like Protocol Version, Timestamp, Batch Count
   - Sets Result Status to Success (0x00)

5. **Response Encoding and Sending**
   - `KmipEncoder.encode()` encodes the response message into TTLV format
   - `KmipTcpServer.handleClient()` sends the encoded response back to the client

### 3. Destroy Symmetric Key Operation (Operation Code: 0x14)

1. **Request Handling**
   - `KmipRequestHandler.processRequest()` identifies the Destroy operation (code 0x14)
   - Delegates to `DestroyOperationHandler.handle(KmipMessage requestHeader, KmipMessage requestPayload)`

2. **Request Processing in DestroyOperationHandler**
   - `DestroyOperationHandler.handle()` extracts the Unique Identifier from the request payload

3. **Key Destruction**
   - `DestroyOperationHandler` calls `KeyManagementService.destroySymmetricKey(String uniqueID)`
   - Implementation is provided by `InMemoryKeyManagementService.destroySymmetricKey()`
   - Removes the key from the in-memory map using the Unique Identifier
   - Returns true if the key was found and removed, false otherwise

4. **Response Building**
   - `DestroyOperationHandler` builds a response payload with:
     - Unique Identifier (0x420094) = Destroyed key's UUID
     - Note: For PyKMIP compatibility, the Object Type field is omitted
   - `KmipRequestHandler.buildSuccessResponse()` builds the complete response message
   - Special handling in `buildSuccessResponse()` to skip Object Type validation for Destroy operation
   - Adds standard fields like Protocol Version, Timestamp, Batch Count
   - Sets Result Status to Success (0x00)

5. **Response Encoding and Sending**
   - `KmipEncoder.encodeResponsePayload()` has special handling for Destroy operation responses
   - Detects Destroy operation by checking if the payload contains only a Unique Identifier field
   - Encodes only the Unique Identifier field for Destroy operation responses
   - `KmipTcpServer.handleClient()` sends the encoded response back to the client

## Class Relationships and Dependencies

- **KmipTcpServer**: Entry point for client connections
  - Depends on: KmipParser, KmipRequestHandler, KmipEncoder

- **KmipParser**: Parses KMIP messages from raw bytes
  - Depends on: KmipMessage, KmipTagResolver

- **KmipRequestHandler**: Routes requests to appropriate handlers
  - Depends on: OperationHandler implementations, KmipTagResolver

- **OperationHandler**: Interface for operation-specific handlers
  - Implementations: CreateOperationHandler, GetOperationHandler, DestroyOperationHandler
  - Each depends on: KeyManagementService, KmipTagResolver

- **KeyManagementService**: Interface for key management operations
  - Implementation: InMemoryKeyManagementService
  - Future implementation: ExternalKeyManagementService

- **KmipEncoder**: Encodes KMIP messages to raw bytes
  - Depends on: KmipMessage, KmipTagResolver

- **KmipMessage**: Represents a KMIP message structure
  - Core data structure used throughout the system

- **KmipTagResolver**: Resolves KMIP tags to their names and values
  - Utility class used throughout the system

## Sequence Diagrams

### Create Operation Sequence

```
Client                  KmipTcpServer           KmipParser           KmipRequestHandler      CreateOperationHandler    InMemoryKeyManagementService
  |                         |                       |                       |                         |                           |
  | KMIP Create Request     |                       |                       |                         |                           |
  |------------------------>|                       |                       |                         |                           |
  |                         | read(buffer)          |                       |                         |                           |
  |                         |---------------------->|                       |                         |                           |
  |                         |                       | parse(buffer)         |                       |                           |
  |                         |                       |---------------------> |                         |                           |
  |                         |                       |                       | processRequest()        |                           |
  |                         |                       |                       |-----------------------> |                           |
  |                         |                       |                       |                         | handle()                  |
  |                         |                       |                       |                         |-------------------------> |
  |                         |                       |                       |                         |                           | createSymmetricKey()
  |                         |                       |                       |                         |                           |----------------
  |                         |                       |                       |                         |                           |               |
  |                         |                       |                       |                         |                           | Generate key |
  |                         |                       |                       |                         |                           |<---------------
  |                         |                       |                       |                         |                           | Store key
  |                         |                       |                       |                         |                           |----------------
  |                         |                       |                       |                         |                           |               |
  |                         |                       |                       |                         |                           | Generate UUID |
  |                         |                       |                       |                         |<--------------------------|<---------------
  |                         |                       |                       |                         | Build response payload    |
  |                         |                       |                       |<------------------------|                           |
  |                         |                       |                       | buildSuccessResponse()  |                           |
  |                         |<----------------------|<----------------------|                         |                           |
  |                         | encode()              |                       |                         |                           |
  |                         |----------------       |                       |                         |                           |
  |                         |               |       |                       |                         |                           |
  |                         | Encode TTLV   |       |                       |                         |                           |
  |                         |<---------------       |                       |                         |                           |
  | KMIP Create Response    |                       |                       |                         |                           |
  |<------------------------|                       |                       |                         |                           |
```

### Get Operation Sequence

```
Client                  KmipTcpServer           KmipParser           KmipRequestHandler      GetOperationHandler       InMemoryKeyManagementService
  |                         |                       |                       |                         |                           |
  | KMIP Get Request        |                       |                       |                         |                           |
  |------------------------>|                       |                       |                         |                           |
  |                         | read(buffer)          |                       |                         |                           |
  |                         |---------------------->|                       |                         |                           |
  |                         |                       | parse(buffer)         |                       |                           |
  |                         |                       |---------------------> |                         |                           |
  |                         |                       |                       | processRequest()        |                           |
  |                         |                       |                       |-----------------------> |                           |
  |                         |                       |                       |                         | handle()                  |
  |                         |                       |                       |                         |-------------------------> |
  |                         |                       |                       |                         |                           | getSymmetricKey()
  |                         |                       |                       |                         |                           |----------------
  |                         |                       |                       |                         |                           |               |
  |                         |                       |                       |                         |                           | Lookup key    |
  |                         |                       |                       |                         |<--------------------------|<---------------
  |                         |                       |                       |                         | Build response payload    |
  |                         |                       |                       |<------------------------|                           |
  |                         |                       |                       | buildSuccessResponse()  |                           |
  |                         |<----------------------|<----------------------|                         |                           |
  |                         | encode()              |                       |                         |                           |
  |                         |----------------       |                       |                         |                           |
  |                         |               |       |                       |                         |                           |
  |                         | Encode TTLV   |       |                       |                         |                           |
  |                         |<---------------       |                       |                         |                           |
  | KMIP Get Response       |                       |                       |                         |                           |
  |<------------------------|                       |                       |                         |                           |
```

### Destroy Operation Sequence

```
Client                  KmipTcpServer           KmipParser           KmipRequestHandler      DestroyOperationHandler   InMemoryKeyManagementService
  |                         |                       |                       |                         |                           |
  | KMIP Destroy Request    |                       |                       |                         |                           |
  |------------------------>|                       |                       |                         |                           |
  |                         | read(buffer)          |                       |                         |                           |
  |                         |---------------------->|                       |                         |                           |
  |                         |                       | parse(buffer)         |                       |                           |
  |                         |                       |---------------------> |                         |                           |
  |                         |                       |                       | processRequest()        |                           |
  |                         |                       |                       |-----------------------> |                           |
  |                         |                       |                       |                         | handle()                  |
  |                         |                       |                       |                         |-------------------------> |
  |                         |                       |                       |                         |                           | destroySymmetricKey()
  |                         |                       |                       |                         |                           |----------------
  |                         |                       |                       |                         |                           |               |
  |                         |                       |                       |                         |                           | Remove key    |
  |                         |                       |                       |                         |<--------------------------|<---------------
  |                         |                       |                       |                         | Build response payload    |
  |                         |                       |                       |<------------------------|                           |
  |                         |                       |                       | buildSuccessResponse()  |                           |
  |                         |<----------------------|<----------------------| (special handling)      |                           |
  |                         | encode()              |                       |                         |                           |
  |                         | (special handling)    |                       |                         |                           |
  |                         |----------------       |                       |                         |                           |
  |                         |               |       |                       |                         |                           |
  |                         | Encode TTLV   |       |                       |                         |                           |
  |                         |<---------------       |                       |                         |                           |
  | KMIP Destroy Response   |                       |                       |                         |                           |
  |<------------------------|                       |                       |                         |                           |
```
