# KMIP Create Operation Structure

## Overview
This document provides a visual breakdown of the KMIP message structure for the Create operation, based on successful communication between a PyKMIP client and our KMIP server implementation.

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

## Create Operation Request Structure

Here's the hierarchical structure of a Create operation request from the PyKMIP client:

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
    │   ├─ Authentication (0x42000C) - Optional
    │   ├─ Maximum Response Size (0x42000D) - Optional
    │   ├─ Asynchronous Indicator (0x42000E) - Optional
    │   └─ Batch Count (0x42000D) = 1
    │
    └─ Batch Item (0x42000F)
        ├─ Operation (0x42005C) = Create (1)
        ├─ Unique Batch Item ID (0x420093) - Optional
        └─ Request Payload (0x420079)
            ├─ Object Type (0x420057) = Symmetric Key (2)
            └─ Template Attribute (0x420091)
                ├─ Attribute (0x420008) - Cryptographic Algorithm
                │   ├─ Attribute Name (0x42000A) = "Cryptographic Algorithm"
                │   └─ Attribute Value (0x42000B) = AES (3)
                │
                ├─ Attribute (0x420008) - Cryptographic Length
                │   ├─ Attribute Name (0x42000A) = "Cryptographic Length"
                │   └─ Attribute Value (0x42000B) = 256
                │
                └─ Attribute (0x420008) - Cryptographic Usage Mask
                    ├─ Attribute Name (0x42000A) = "Cryptographic Usage Mask"
                    └─ Attribute Value (0x42000B) = 12 (Encrypt + Decrypt)
```

### Byte-by-Byte Breakdown of Create Request

A typical Create Symmetric Key request has the following byte structure:

```
42 00 7A 01 00 00 00 D0 42 00 77 01 00 00 00 38
42 00 69 01 00 00 00 20 42 00 6A 02 00 00 00 04
00 00 00 02 00 00 00 00 42 00 6B 02 00 00 00 04
00 00 00 00 00 00 00 00 42 00 0D 02 00 00 00 04
00 00 00 01 00 00 00 00 42 00 0F 01 00 00 00 88
42 00 5C 05 00 00 00 04 00 00 00 01 00 00 00 00
42 00 79 01 00 00 00 78 42 00 57 05 00 00 00 04
00 00 00 02 00 00 00 00 42 00 91 01 00 00 00 68
42 00 08 01 00 00 00 20 42 00 0A 07 00 00 00 17
43 72 79 70 74 6F 67 72 61 70 68 69 63 20 41 6C
67 6F 72 69 74 68 6D 00 42 00 0B 05 00 00 00 04
00 00 00 03 00 00 00 00 42 00 08 01 00 00 00 20
42 00 0A 07 00 00 00 15 43 72 79 70 74 6F 67 72
61 70 68 69 63 20 4C 65 6E 67 74 68 00 00 00 00
42 00 0B 02 00 00 00 04 00 00 01 00 00 00 00 00
42 00 08 01 00 00 00 20 42 00 0A 07 00 00 00 18
43 72 79 70 74 6F 67 72 61 70 68 69 63 20 55 73
61 67 65 20 4D 61 73 6B 42 00 0B 02 00 00 00 04
00 00 00 0C 00 00 00 00
```

Let's break this down into its components:

### Message Header
```
┌───────────────────┬───────────────────┐
│ 42 00 7A 01       │ 00 00 00 D0       │
│ (Tag + Type)      │ (Message Size=208)│
└───────────────────┴───────────────────┘
```

### Request Message
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 7A  │ 01      │ 00 00 00 D0   │ (Value contains all below)    │
│ (Tag)     │ (Type)  │ (Length=208)  │                               │
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

### Key Components for Create Operation Request

1. **Object Type (0x420057)** - Specifies the type of object to create (Symmetric Key = 2)
2. **Template Attribute (0x420091)** - Contains attributes for the new key:
   - **Cryptographic Algorithm** - Specifies the algorithm (AES = 3)
   - **Cryptographic Length** - Specifies the key length (256 bits)
   - **Cryptographic Usage Mask** - Specifies allowed uses (Encrypt + Decrypt = 12)

## Create Operation Response Structure

Here's the hierarchical structure of a successful Create operation response:

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
        ├─ Operation (0x42005C) = Create (1)
        ├─ Result Status (0x42007F) = Success (0)
        └─ Response Payload (0x42007C)
            ├─ Object Type (0x420057) = Symmetric Key (2)
            └─ Unique Identifier (0x420094) = "c80229c4-cbc5-48c8-ab65-581dabf79d7f"
```

## Byte-by-Byte Breakdown of Create Response

The successful response from our server had the following byte structure:

```
42 00 7B 01 00 00 00 E8 42 00 7A 01 00 00 00 48
42 00 69 01 00 00 00 20 42 00 6A 02 00 00 00 04
00 00 00 02 00 00 00 00 42 00 6B 02 00 00 00 04
00 00 00 00 00 00 00 00 42 00 92 09 00 00 00 08
00 00 00 00 67 F8 16 B2 42 00 0D 02 00 00 00 04
00 00 00 01 00 00 00 00 42 00 0F 01 00 00 00 90
42 00 5C 05 ...
```

Let's break this down into its components:

### Message Header
```
┌───────────────────┬───────────────────┐
│ 42 00 7B 01       │ 00 00 00 E8       │
│ (Tag + Type)      │ (Message Size=232)│
└───────────────────┴───────────────────┘
```

### Response Message
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 7B  │ 01      │ 00 00 00 E8   │ (Value contains all below)    │
│ (Tag)     │ (Type)  │ (Length=232)  │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Response Header
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 7A  │ 01      │ 00 00 00 48   │ (Value contains next 3 items) │
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
│ 42 00 92  │ 09      │ 00 00 00 08   │ 00 00 00 00 67 F8 16 B2       │
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
│ 42 00 0F  │ 01      │ 00 00 00 90   │ (Value contains next items)   │
│ (Tag)     │ (Type)  │ (Length=144)  │                               │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

### Operation
```
┌───────────┬─────────┬───────────────┬───────────────────────────────┐
│ 42 00 5C  │ 05      │ 00 00 00 04   │ 00 00 00 01 (Create)          │
│ (Tag)     │ (Type)  │ (Length=4)    │ (Value)                       │
└───────────┴─────────┴───────────────┴───────────────────────────────┘
```

## Critical Fields for Create Operation

The PyKMIP client requires these fields in the response payload:

1. **Object Type (0x420057)** - Must be present with value 2 (Symmetric Key)
2. **Unique Identifier (0x420094)** - Must be present with a valid UUID string

## PyKMIP Client Expectations

Based on analysis of the PyKMIP client code, the client processes the response as follows:

1. **Read Header**: Reads first 8 bytes
   - First 4 bytes contain Tag (3 bytes) and Type (1 byte)
   - Next 4 bytes determine message size

2. **Read Message**: Reads exactly the number of bytes specified in the header

3. **Parse Structure**: Recursively parses the TTLV structure
   - Validates tags match expected values
   - Extracts values based on types
   - For Create operation, extracts Object Type and Unique Identifier

## Comparison with OASIS KMIP 2.0 Specification

The Create operation request and response structures adhere to the KMIP 2.0 Protocol standard as defined in the [OASIS KMIP 2.0 Specification](https://docs.oasis-open.org/kmip/kmip-spec/v2.0/os/kmip-spec-v2.0-os.html#_Toc6497437).

### Create Request Compliance

| OASIS Requirement | Our Implementation | Compliance |
|-------------------|-------------------|------------|
| Request Message with Protocol Version | Included (0x420069) with Major=2, Minor=0 | ✅ Compliant |
| Request Message with Batch Count | Included (0x42000D) with value=1 | ✅ Compliant |
| Batch Item with Operation | Included (0x42005C) with value=Create (1) | ✅ Compliant |
| Request Payload with Object Type | Included (0x420057) with value=Symmetric Key (2) | ✅ Compliant |
| Template Attribute with required attributes | Included algorithm, length, and usage mask | ✅ Compliant |

### Create Response Compliance

| OASIS Requirement | Our Implementation | Compliance |
|-------------------|-------------------|------------|
| Response Message with Protocol Version | Included (0x420069) with Major=2, Minor=0 | ✅ Compliant |
| Response Message with Timestamp | Included (0x420092) | ✅ Compliant |
| Response Message with Batch Count | Included (0x42000D) with value=1 | ✅ Compliant |
| Batch Item with Operation | Included (0x42005C) with value=Create (1) | ✅ Compliant |
| Batch Item with Result Status | Included (0x42007F) with value=Success (0) | ✅ Compliant |
| Response Payload with Object Type | Included (0x420057) with value=Symmetric Key (2) | ✅ Compliant |
| Response Payload with Unique Identifier | Included (0x420094) with UUID value | ✅ Compliant |

## Conclusion

The request and response structures for the Create operation match the expectations of both the PyKMIP client and the OASIS KMIP 2.0 specification. The key points are:

1. Both request and response include all required fields in the correct hierarchical structure
2. The tags, types, and lengths are properly formatted according to the TTLV encoding rules
3. The request includes the necessary attributes for creating a symmetric key
4. The response includes the required unique identifier for the created key

This structure should be maintained for all Create operation requests and responses to ensure compatibility with PyKMIP clients and adherence to the KMIP 2.0 standard.
