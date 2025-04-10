# KMIP Get Operation Structure

## Overview
This document provides a visual breakdown of the KMIP message structure for the Get operation, based on successful communication between a PyKMIP client and our KMIP server implementation.

## KMIP Encoding Format

KMIP uses a **Tag-Type-Length-Value (TTLV)** encoding format:

```
┌────────────┬─────────┬──────────────┬───────────────────┐
│    Tag     │  Type   │    Length    │       Value       │
│  (3 bytes) │ (1 byte)│   (4 bytes)  │ (variable length) │
└────────────┴─────────┴──────────────┴───────────────────┘
```

- **Tag**: 3 bytes identifying the field (e.g., `0x42007B` for Response Message)
- **Type**: 1 byte identifying the data type:
  - `0x01`: Structure
  - `0x02`: Integer
  - `0x05`: Enumeration
  - `0x07`: Text String
  - `0x09`: DateTime
- **Length**: 4 bytes indicating the length of the value in bytes
- **Value**: Variable length data based on the type

## Message Header

Every KMIP message begins with a special 8-byte header:

```
┌────────────────────┬────────────────────┐
│   First 4 bytes    │   Next 4 bytes     │
│ (Tag + Type)       │   (Message Size)   │
└────────────────────┴────────────────────┘
```

The first 4 bytes typically contain the Tag (3 bytes) and Type (1 byte) of the first TTLV structure in the message.

## Get Operation Request Structure

Here's the hierarchical structure of a Get operation request from the PyKMIP client:

```
┌─ Message Header (8 bytes)
│   ├─ First 4 bytes: Tag (0x42007A) + Type (0x01)
│   └─ Next 4 bytes: Message Size
│
└─ Request Message (0x42007A)
    ├─ Request Header (0x420077)
    │   ├─ Protocol Version (0x420069)
    │   │   ├─ Protocol Version Major (0x42006A) = 2
    │   │   └─ Protocol Version Minor (0x42006B) = 0
    │   └─ Batch Count (0x42000D) = 1
    │
    └─ Batch Item (0x42000F)
        ├─ Operation (0x42005C) = Get (10)
        └─ Request Payload (0x420079)
            ├─ Unique Identifier (0x420094) = "f2c249c6-3877-4594-8151-2c7925ca5eeb"
            └─ Key Format Type (0x420042) - Optional
```

### Byte-by-Byte Breakdown of Get Request

A typical Get Key request has the following byte structure:

```
42 00 7A 01 00 00 00 90 42 00 77 01 00 00 00 38
42 00 69 01 00 00 00 20 42 00 6A 02 00 00 00 04
00 00 00 02 00 00 00 00 42 00 6B 02 00 00 00 04
00 00 00 00 00 00 00 00 42 00 0D 02 00 00 00 04
00 00 00 01 00 00 00 00 42 00 0F 01 00 00 00 48
42 00 5C 05 00 00 00 04 00 00 00 0A 00 00 00 00
42 00 79 01 00 00 00 30 42 00 94 07 00 00 00 24
66 32 63 32 34 39 63 36 2D 33 38 37 37 2D 34 35
39 34 2D 38 31 35 31 2D 32 63 37 39 32 35 63 61
35 65 65 62 00 00 00 00
```

Let's break this down into its components:

### Message Header
```
┌───────────────────┬───────────────────┐
│ 42 00 7A 01       │ 00 00 00 90       │
│ (Tag + Type)      │ (Message Size=144)│
└───────────────────┴───────────────────┘
```

### Request Message
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 7A  │ 01      │ 00 00 00 90   │ (Value contains all below)    │
│ (Tag)     │ (Type)  │ (Length=144)  │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Request Header
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 77  │ 01      │ 00 00 00 38   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=56)   │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Protocol Version
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 69  │ 01      │ 00 00 00 20   │ (Value contains next 2 items) │
│ (Tag)     │ (Type)  │ (Length=32)   │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Protocol Version Major
```
┌───────────┬─────────┬───────────────┬───────────────┬───────────────┐
│ 42 00 6A  │ 02      │ 00 00 00 04   │ 00 00 00 02   │ 00 00 00 00   │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value=2)     │ (Padding)     │
└───────────┴─────────┴───────────────┴───────────────┴───────────────┘
```

### Protocol Version Minor
```
┌───────────┬─────────┬───────────────┬───────────────┬───────────────┐
│ 42 00 6B  │ 02      │ 00 00 00 04   │ 00 00 00 00   │ 00 00 00 00   │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value=0)     │ (Padding)     │
└───────────┴─────────┴───────────────┴───────────────┴───────────────┘
```

### Batch Count
```
┌───────────┬─────────┬───────────────┬───────────────┬───────────────┐
│ 42 00 0D  │ 02      │ 00 00 00 04   │ 00 00 00 01   │ 00 00 00 00   │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value=1)     │ (Padding)     │
└───────────┴─────────┴───────────────┴───────────────┴───────────────┘
```

### Batch Item
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 0F  │ 01      │ 00 00 00 48   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=72)   │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Operation
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 5C  │ 05      │ 00 00 00 04   │ 00 00 00 0A (Get)             │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value)                       │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Request Payload
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 79  │ 01      │ 00 00 00 30   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=48)   │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Unique Identifier
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 94  │ 07      │ 00 00 00 24   │ 66 32 63 32...                │
│ (Tag)     │ (Type)  │ (Length=36)   │ (Value="f2c249c6-3877-...")   │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Key Components for Get Operation Request

1. **Operation (0x42005C)** - Specifies the operation to perform (Get = 10)
2. **Unique Identifier (0x420094)** - Specifies the ID of the key to retrieve

## Get Operation Response Structure

Here's the hierarchical structure of a successful Get operation response:

```
┌─ Message Header (8 bytes)
│   ├─ First 4 bytes: Tag (0x42007B) + Type (0x01)
│   └─ Next 4 bytes: Message Size
│
└─ Response Message (0x42007B)
    ├─ Response Header (0x42007A)
    │   ├─ Protocol Version (0x420069)
    │   │   ├─ Protocol Version Major (0x42006A) = 2
    │   │   └─ Protocol Version Minor (0x42006B) = 0
    │   ├─ Timestamp (0x420092)
    │   └─ Batch Count (0x42000D) = 1
    │
    └─ Response Batch Item (0x42000F)
        ├─ Operation (0x42005C) = Get (10)
        ├─ Result Status (0x42007F) = Success (0)
        └─ Response Payload (0x42007C)
            ├─ Object Type (0x420057) = Symmetric Key (2)
            ├─ Unique Identifier (0x420094) = "f2c249c6-3877-4594-8151-2c7925ca5eeb"
            └─ Symmetric Key (0x42008F)
                └─ Key Block (0x420040)
                    ├─ Key Format Type (0x420042) = Raw (1)
                    ├─ Key Value (0x420045)
                    │   └─ Key Material (0x420043) = [key bytes]
                    ├─ Cryptographic Algorithm (0x420028) = AES (3)
                    ├─ Cryptographic Length (0x42002A) = 256
                    └─ Cryptographic Usage Mask (0x420014) = [usage mask]
```

### Byte-by-Byte Breakdown of Get Response

A successful Get Key response has the following byte structure (partial):

```
42 00 7B 01 00 00 01 58 42 00 7A 01 00 00 00 48
42 00 69 01 00 00 00 20 42 00 6A 02 00 00 00 04
00 00 00 02 00 00 00 00 42 00 6B 02 00 00 00 04
00 00 00 00 00 00 00 00 42 00 92 09 00 00 00 08
00 00 00 00 67 F8 26 5D 42 00 0D 02 00 00 00 04
00 00 00 01 00 00 00 00 42 00 0F 01 00 00 01 00
42 00 5C 05 ...
```

### Message Header
```
┌───────────────────┬───────────────────┐
│ 42 00 7B 01       │ 00 00 01 58       │
│ (Tag + Type)      │ (Message Size=344)│
└───────────────────┴───────────────────┘
```

### Response Message
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 7B  │ 01      │ 00 00 01 58   │ (Value contains all below)    │
│ (Tag)     │ (Type)  │ (Length=344)  │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Response Header
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 7A  │ 01      │ 00 00 00 48   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=72)   │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Protocol Version
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 69  │ 01      │ 00 00 00 20   │ (Value contains next 2 items) │
│ (Tag)     │ (Type)  │ (Length=32)   │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Protocol Version Major
```
┌───────────┬─────────┬───────────────┬───────────────┬───────────────┐
│ 42 00 6A  │ 02      │ 00 00 00 04   │ 00 00 00 02   │ 00 00 00 00   │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value=2)     │ (Padding)     │
└───────────┴─────────┴───────────────┴───────────────┴───────────────┘
```

### Protocol Version Minor
```
┌───────────┬─────────┬───────────────┬───────────────┬───────────────┐
│ 42 00 6B  │ 02      │ 00 00 00 04   │ 00 00 00 00   │ 00 00 00 00   │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value=0)     │ (Padding)     │
└───────────┴─────────┴───────────────┴───────────────┴───────────────┘
```

### Timestamp
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 92  │ 09      │ 00 00 00 08   │ 00 00 00 00 67 F8 26 5D       │
│ (Tag)     │ (Type)  │ (Length=8)    │ (Value=Timestamp)             │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Batch Count
```
┌───────────┬─────────┬───────────────┬───────────────┬───────────────┐
│ 42 00 0D  │ 02      │ 00 00 00 04   │ 00 00 00 01   │ 00 00 00 00   │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value=1)     │ (Padding)     │
└───────────┴─────────┴───────────────┴───────────────┴───────────────┘
```

### Response Batch Item
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 0F  │ 01      │ 00 00 01 00   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=256)  │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Operation
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 5C  │ 05      │ 00 00 00 04   │ 00 00 00 0A (Get)             │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value)                       │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Key Components for Get Operation Response

1. **Object Type (0x420057)** - Specifies the type of object retrieved (Symmetric Key = 2)
2. **Unique Identifier (0x420094)** - Returns the ID of the retrieved key
3. **Symmetric Key (0x42008F)** - Contains the key data and attributes:
   - **Key Block (0x420040)** - Contains the key value and metadata
   - **Key Format Type (0x420042)** - Specifies the format (Raw = 1)
   - **Key Value (0x420045)** - Contains the actual key material
   - **Cryptographic Algorithm (0x420028)** - Specifies the algorithm (AES = 3)
   - **Cryptographic Length (0x42002A)** - Specifies the key length (256 bits)
   - **Cryptographic Usage Mask (0x420014)** - Specifies allowed uses

## Comparison with OASIS KMIP 2.0 Specification

The Get operation request and response structures adhere to the KMIP 2.0 Protocol standard as defined in the [OASIS KMIP 2.0 Specification](https://docs.oasis-open.org/kmip/kmip-spec/v2.0/os/kmip-spec-v2.0-os.html#_Toc6497437).

### Get Request Compliance

| OASIS Requirement | Our Implementation | Compliance |
|-------------------|-------------------|------------|
| Request Message with Protocol Version | Included (0x420069) with Major=2, Minor=0 | ✅ Compliant |
| Request Message with Batch Count | Included (0x42000D) with value=1 | ✅ Compliant |
| Batch Item with Operation | Included (0x42005C) with value=Get (10) | ✅ Compliant |
| Request Payload with Unique Identifier | Included (0x420094) with UUID value | ✅ Compliant |

### Get Response Compliance

| OASIS Requirement | Our Implementation | Compliance |
|-------------------|-------------------|------------|
| Response Message with Protocol Version | Included (0x420069) with Major=2, Minor=0 | ✅ Compliant |
| Response Message with Timestamp | Included (0x420092) | ✅ Compliant |
| Response Message with Batch Count | Included (0x42000D) with value=1 | ✅ Compliant |
| Batch Item with Operation | Included (0x42005C) with value=Get (10) | ✅ Compliant |
| Batch Item with Result Status | Included (0x42007F) with value=Success (0) | ✅ Compliant |
| Response Payload with Object Type | Included (0x420057) with value=Symmetric Key (2) | ✅ Compliant |
| Response Payload with Unique Identifier | Included (0x420094) with UUID value | ✅ Compliant |
| Response Payload with Symmetric Key | Included (0x42008F) with key data | ✅ Compliant |

## PyKMIP Client Expectations

Based on analysis of the PyKMIP client code, the client processes the Get response as follows:

1. **Read Header**: Reads first 8 bytes
   - First 4 bytes contain Tag (3 bytes) and Type (1 byte)
   - Next 4 bytes determine message size

2. **Read Message**: Reads exactly the number of bytes specified in the header

3. **Parse Structure**: Recursively parses the TTLV structure
   - Validates tags match expected values
   - Extracts values based on types
   - For Get operation, extracts Object Type, Unique Identifier, and Symmetric Key data

## Conclusion

The request and response structures for the Get operation match the expectations of the PyKMIP client and adhere to the OASIS KMIP 2.0 specification. The key points are:

1. The request includes the unique identifier of the key to retrieve
2. The response includes the key data and all required metadata
3. All messages follow the correct hierarchical structure
4. The tags, types, and lengths are properly formatted according to the TTLV encoding rules

This structure should be maintained for all Get operation requests and responses to ensure compatibility with PyKMIP clients and adherence to the KMIP 2.0 standard.
