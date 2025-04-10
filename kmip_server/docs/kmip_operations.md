# KMIP Operations

This document describes the KMIP operations implemented in our server.

## Create Operation

The Create operation is used to generate a new managed cryptographic object.

### Request

```
┌─────────────────────────────────────────────────────────┐
│                     Request Message                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │               Request Header                     │    │
│  ├─────────────────────────────────────────────────┤    │
│  │  Protocol Version: 2.0                          │    │
│  │  Batch Count: 1                                 │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │               Batch Item                         │    │
│  ├─────────────────────────────────────────────────┤    │
│  │  Operation: Create (0x01)                       │    │
│  │                                                 │    │
│  │  ┌─────────────────────────────────────────┐    │    │
│  │  │         Request Payload                 │    │    │
│  │  ├─────────────────────────────────────────┤    │    │
│  │  │  Object Type: Symmetric Key (0x02)      │    │    │
│  │  │                                         │    │    │
│  │  │  ┌─────────────────────────────────┐    │    │    │
│  │  │  │     Template Attribute          │    │    │    │
│  │  │  ├─────────────────────────────────┤    │    │    │
│  │  │  │                                 │    │    │    │
│  │  │  │  ┌─────────────────────────┐    │    │    │    │
│  │  │  │  │      Attribute          │    │    │    │    │
│  │  │  │  ├─────────────────────────┤    │    │    │    │
│  │  │  │  │ Attribute Name:         │    │    │    │    │
│  │  │  │  │ Cryptographic Algorithm │    │    │    │    │
│  │  │  │  │                         │    │    │    │    │
│  │  │  │  │ Attribute Value: AES    │    │    │    │    │
│  │  │  │  └─────────────────────────┘    │    │    │    │
│  │  │  │                                 │    │    │    │
│  │  │  │  ┌─────────────────────────┐    │    │    │    │
│  │  │  │  │      Attribute          │    │    │    │    │
│  │  │  │  ├─────────────────────────┤    │    │    │    │
│  │  │  │  │ Attribute Name:         │    │    │    │    │
│  │  │  │  │ Cryptographic Length    │    │    │    │    │
│  │  │  │  │                         │    │    │    │    │
│  │  │  │  │ Attribute Value: 256    │    │    │    │    │
│  │  │  │  └─────────────────────────┘    │    │    │    │
│  │  │  │                                 │    │    │    │
│  │  │  └─────────────────────────────────┘    │    │    │
│  │  │                                         │    │    │
│  │  └─────────────────────────────────────────┘    │    │
│  │                                                 │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Response

```
┌─────────────────────────────────────────────────────────┐
│                     Response Message                     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │               Response Header                    │    │
│  ├─────────────────────────────────────────────────┤    │
│  │  Protocol Version: 2.0                          │    │
│  │  Time Stamp: 2023-04-10T12:34:56Z               │    │
│  │  Batch Count: 1                                 │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │               Batch Item                         │    │
│  ├─────────────────────────────────────────────────┤    │
│  │  Operation: Create (0x01)                       │    │
│  │  Result Status: Success (0x00)                  │    │
│  │                                                 │    │
│  │  ┌─────────────────────────────────────────┐    │    │
│  │  │         Response Payload                │    │    │
│  │  ├─────────────────────────────────────────┤    │    │
│  │  │  Object Type: Symmetric Key (0x02)      │    │    │
│  │  │  Unique Identifier: "uuid-string"       │    │    │
│  │  └─────────────────────────────────────────┘    │    │
│  │                                                 │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Implementation

The Create operation is implemented in the `CreateOperationHandler` class. It:

1. Extracts the object type from the request payload
2. Extracts attributes from the template attribute (if present)
3. Generates a unique identifier (UUID)
4. Creates the key using the key management service
5. Returns the unique identifier in the response payload

## Get Operation

The Get operation is used to retrieve a managed cryptographic object.

### Request

```
┌─────────────────────────────────────────────────────────┐
│                     Request Message                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │               Request Header                     │    │
│  ├─────────────────────────────────────────────────┤    │
│  │  Protocol Version: 2.0                          │    │
│  │  Batch Count: 1                                 │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │               Batch Item                         │    │
│  ├─────────────────────────────────────────────────┤    │
│  │  Operation: Get (0x0A)                          │    │
│  │                                                 │    │
│  │  ┌─────────────────────────────────────────┐    │    │
│  │  │         Request Payload                 │    │    │
│  │  ├─────────────────────────────────────────┤    │    │
│  │  │  Unique Identifier: "uuid-string"       │    │    │
│  │  └─────────────────────────────────────────┘    │    │
│  │                                                 │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Response

```
┌─────────────────────────────────────────────────────────┐
│                     Response Message                     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │               Response Header                    │    │
│  ├─────────────────────────────────────────────────┤    │
│  │  Protocol Version: 2.0                          │    │
│  │  Time Stamp: 2023-04-10T12:34:56Z               │    │
│  │  Batch Count: 1                                 │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │               Batch Item                         │    │
│  ├─────────────────────────────────────────────────┤    │
│  │  Operation: Get (0x0A)                          │    │
│  │  Result Status: Success (0x00)                  │    │
│  │                                                 │    │
│  │  ┌─────────────────────────────────────────┐    │    │
│  │  │         Response Payload                │    │    │
│  │  ├─────────────────────────────────────────┤    │    │
│  │  │  Object Type: Symmetric Key (0x02)      │    │    │
│  │  │  Unique Identifier: "uuid-string"       │    │    │
│  │  │                                         │    │    │
│  │  │  ┌─────────────────────────────────┐    │    │    │
│  │  │  │       Symmetric Key             │    │    │    │
│  │  │  ├─────────────────────────────────┤    │    │    │
│  │  │  │                                 │    │    │    │
│  │  │  │  ┌─────────────────────────┐    │    │    │    │
│  │  │  │  │      Key Block          │    │    │    │    │
│  │  │  │  ├─────────────────────────┤    │    │    │    │
│  │  │  │  │ Key Format Type: Raw    │    │    │    │    │
│  │  │  │  │                         │    │    │    │    │
│  │  │  │  │ ┌───────────────────┐   │    │    │    │    │
│  │  │  │  │ │    Key Value      │   │    │    │    │    │
│  │  │  │  │ ├───────────────────┤   │    │    │    │    │
│  │  │  │  │ │   Key Material    │   │    │    │    │    │
│  │  │  │  │ └───────────────────┘   │    │    │    │    │
│  │  │  │  │                         │    │    │    │    │
│  │  │  │  │ Crypto Algorithm: AES   │    │    │    │    │
│  │  │  │  │ Crypto Length: 256      │    │    │    │    │
│  │  │  │  │ Usage Mask: 12          │    │    │    │    │
│  │  │  │  └─────────────────────────┘    │    │    │    │
│  │  │  │                                 │    │    │    │
│  │  │  └─────────────────────────────────┘    │    │    │
│  │  │                                         │    │    │
│  │  └─────────────────────────────────────────┘    │    │
│  │                                                 │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Implementation

The Get operation is implemented in the `GetOperationHandler` class. It:

1. Extracts the unique identifier from the request payload
2. Retrieves the key from the key management service
3. Builds a response payload with the key and its attributes
4. Returns the response payload

## Future Operations

The following operations are planned for future implementation:

1. **Destroy**: Delete a managed cryptographic object
2. **Locate**: Search for managed cryptographic objects
3. **Register**: Register an existing cryptographic object
4. **Revoke**: Revoke a managed cryptographic object
5. **Activate**: Activate a managed cryptographic object
6. **Encrypt**: Encrypt data using a managed cryptographic object
7. **Decrypt**: Decrypt data using a managed cryptographic object

## KMIP Operation Codes

| Operation | Code (Hex) | Code (Dec) | Implemented |
|-----------|------------|------------|-------------|
| Create    | 0x01       | 1          | Yes         |
| Get       | 0x0A       | 10         | Yes         |
| Destroy   | 0x14       | 20         | No          |
| Locate    | 0x08       | 8          | No          |
| Register  | 0x0B       | 11         | No          |
| Revoke    | 0x0C       | 12         | No          |
| Activate  | 0x18       | 24         | No          |
| Encrypt   | 0x0D       | 13         | No          |
| Decrypt   | 0x0E       | 14         | No          |
