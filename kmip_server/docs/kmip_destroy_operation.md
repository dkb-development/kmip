# KMIP Destroy Operation

## Overview
The Destroy operation is used to permanently invalidate a Managed Object and render it unusable. This document provides details on the KMIP message structure for the Destroy operation, based on the KMIP 2.0 specification.

## References
- KMIP 2.0 Specification: https://docs.oasis-open.org/kmip/kmip-spec/v2.0/os/kmip-spec-v2.0-os.html
- Destroy Operation: Section 4.23
- Operation Value: 0x14 (Table 190 in Section 6.1.9)

## Specification Excerpt
From Section 4.23 of the KMIP 2.0 specification:

"This operation requests the server to destroy a Managed Object. The request payload contains the Unique Identifier of the Managed Object. The operation performs a logical deletion of the object. KMIP allows clients to associate security attributes with objects. Destroying the object may restrict an attacker's ability to access the security attributes and thus should be done when the object is no longer needed."

## Request Message Structure

```
Request Message (0x42007A)
├── Request Header (0x420077)
│   ├── Protocol Version (0x420069)
│   │   ├── Protocol Version Major (0x42006A) = 2
│   │   └── Protocol Version Minor (0x42006B) = 0
│   └── Batch Count (0x42000D) = 1
└── Batch Item (0x42000F)
    ├── Operation (0x42005C) = Destroy (0x14)
    └── Request Payload (0x420079)
        └── Unique Identifier (0x420094) = "key-id-to-destroy"
```

### Byte-Level Representation

#### Message Header
```
┌───────────────────┬───────────────────┐
│ 42 00 7A 01       │ 00 00 00 A8       │
│ (Tag + Type)      │ (Message Size=168)│
└───────────────────┴───────────────────┘
```

#### Request Message
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 7A  │ 01      │ 00 00 00 A8   │ (Value contains all below)    │
│ (Tag)     │ (Type)  │ (Length=168)  │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Request Header
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 77  │ 01      │ 00 00 00 38   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=56)   │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Protocol Version
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 69  │ 01      │ 00 00 00 20   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=32)   │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Protocol Version Major
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 6A  │ 02      │ 00 00 00 04   │ 00 00 00 02                   │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value=2)                     │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Protocol Version Minor
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 6B  │ 02      │ 00 00 00 04   │ 00 00 00 00                   │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value=0)                     │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Batch Count
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 0D  │ 02      │ 00 00 00 04   │ 00 00 00 01                   │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value=1)                     │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Batch Item
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 0F  │ 01      │ 00 00 00 60   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=96)   │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Operation
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 5C  │ 05      │ 00 00 00 04   │ 00 00 00 14                   │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value=0x14)                  │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Request Payload
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 79  │ 01      │ 00 00 00 48   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=72)   │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Unique Identifier
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 94  │ 07      │ 00 00 00 24   │ 6b 65 79 2d 69 64 2d 74 6f 2d │
│ (Tag)     │ (Type)  │ (Length=36)   │ 64 65 73 74 72 6f 79 ...      │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
                                        (Value="key-id-to-destroy")
```

## Response Message Structure

```
Response Message (0x42007B)
├── Response Header (0x42007A)
│   ├── Protocol Version (0x420069)
│   │   ├── Protocol Version Major (0x42006A) = 2
│   │   └── Protocol Version Minor (0x42006B) = 0
│   ├── Timestamp (0x420092)
│   └── Batch Count (0x42000D) = 1
└── Batch Item (0x42000F)
    ├── Operation (0x42005C) = Destroy (0x14)
    ├── Result Status (0x42007F) = Success (0) or Failure (1)
    ├── Result Message (0x420081) = "Operation completed successfully"
    └── Response Payload (0x42007C)
        └── Unique Identifier (0x420094) = "destroyed-key-id"
```

### Byte-Level Representation

#### Message Header
```
┌───────────────────┬───────────────────┐
│ 42 00 7B 01       │ 00 00 00 C0       │
│ (Tag + Type)      │ (Message Size=192)│
└───────────────────┴───────────────────┘
```

#### Response Message
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 7B  │ 01      │ 00 00 00 C0   │ (Value contains all below)    │
│ (Tag)     │ (Type)  │ (Length=192)  │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Response Header
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 7A  │ 01      │ 00 00 00 48   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=72)   │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Protocol Version (same as in request)

#### Timestamp
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 92  │ 09      │ 00 00 00 08   │ 00 00 00 00 67 FD 54 1C       │
│ (Tag)     │ (Type)  │ (Length=8)    │ (Value=Unix timestamp)        │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Batch Count (same as in request)

#### Response Batch Item
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 0F  │ 01      │ 00 00 00 68   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=104)  │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Operation (same as in request)

#### Result Status
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 7F  │ 05      │ 00 00 00 04   │ 00 00 00 00                   │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value=Success)               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Result Message
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 81  │ 07      │ 00 00 00 20   │ 4f 70 65 72 61 74 69 6f 6e 20 │
│ (Tag)     │ (Type)  │ (Length=32)   │ 63 6f 6d 70 6c 65 74 65 64 ... │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
                                        (Value="Operation completed successfully")
```

#### Response Payload
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 7C  │ 01      │ 00 00 00 30   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=48)   │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

#### Unique Identifier
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 94  │ 07      │ 00 00 00 24   │ 64 65 73 74 72 6f 79 65 64 2d │
│ (Tag)     │ (Type)  │ (Length=36)   │ 6b 65 79 2d 69 64 ...          │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
                                        (Value="destroyed-key-id")
```

## Tag Values Reference
All tag values are from the KMIP 2.0 specification, Table 173 in Section 6.1:

| Tag Name | Tag Value |
|----------|-----------|
| Request Message | 0x42007A |
| Response Message | 0x42007B |
| Request Header | 0x420077 |
| Response Header | 0x42007A |
| Protocol Version | 0x420069 |
| Protocol Version Major | 0x42006A |
| Protocol Version Minor | 0x42006B |
| Batch Count | 0x42000D |
| Batch Item | 0x42000F |
| Operation | 0x42005C |
| Request Payload | 0x420079 |
| Response Payload | 0x42007C |
| Object Type | 0x420057 |
| Unique Identifier | 0x420094 |
| Result Status | 0x42007F |
| Result Message | 0x420081 |
| Timestamp | 0x420092 |

## Implementation Notes

1. The Destroy operation permanently removes the key from the key store.
2. The response payload includes only:
   - Unique Identifier (0x420094) = The ID of the destroyed key
3. Unlike other operations, the Destroy operation response payload does NOT include the Object Type field.
4. This is a deviation from our usual pattern of including Object Type in all response payloads, but it's necessary for compatibility with the PyKMIP client.
5. If the key is not found, the operation returns a failure status with an appropriate error message.
6. The implementation follows the KMIP 2.0 specification for the Destroy operation.
