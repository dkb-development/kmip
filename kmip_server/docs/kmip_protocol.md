# KMIP Protocol

The Key Management Interoperability Protocol (KMIP) is a standard for interoperable key management. It defines a protocol for the communication between key management systems and encryption systems.

## Overview

KMIP is developed by the Organization for the Advancement of Structured Information Standards (OASIS). It provides a comprehensive protocol for the full lifecycle of keys, from creation to destruction.

The protocol is designed to be:
- **Interoperable**: Works with different vendors' products
- **Comprehensive**: Covers the full lifecycle of keys
- **Secure**: Uses TLS for transport security
- **Extensible**: Can be extended for future needs

## KMIP Versions

| Version | Release Date | Key Features |
|---------|--------------|--------------|
| 1.0     | 2010         | Basic key management operations |
| 1.1     | 2013         | Enhanced key lifecycle management |
| 1.2     | 2014         | Enhanced cryptographic operations |
| 1.3     | 2015         | Enhanced key attributes |
| 1.4     | 2017         | Enhanced key lifecycle management |
| 2.0     | 2019         | Simplified object model, enhanced operations |
| 2.1     | 2021         | Enhanced key attributes, enhanced operations |

Our server implements KMIP 2.0.

## KMIP Architecture

KMIP defines a client-server architecture:

```
┌───────────────┐                 ┌───────────────┐
│               │                 │               │
│  KMIP Client  │◄───────────────►│  KMIP Server  │
│               │                 │               │
└───────────────┘                 └───────────────┘
```

- **KMIP Client**: Requests key management operations from the server
- **KMIP Server**: Performs key management operations and returns results to the client

## KMIP Operations

KMIP defines a set of operations for key management:

| Operation | Description |
|-----------|-------------|
| Create    | Create a new key |
| Get       | Retrieve a key |
| Destroy   | Delete a key |
| Locate    | Search for keys |
| Register  | Register an existing key |
| Revoke    | Revoke a key |
| Activate  | Activate a key |
| Encrypt   | Encrypt data using a key |
| Decrypt   | Decrypt data using a key |
| Sign      | Sign data using a key |
| Verify    | Verify a signature using a key |
| ... and more | |

## KMIP Objects

KMIP defines a set of objects that can be managed:

| Object Type | Description |
|-------------|-------------|
| Symmetric Key | A key for symmetric encryption |
| Public Key | A public key for asymmetric encryption |
| Private Key | A private key for asymmetric encryption |
| Certificate | A digital certificate |
| Secret Data | Secret data, such as a password |
| Split Key | A key split into multiple parts |
| Template | A template for creating objects |
| ... and more | |

## KMIP Attributes

KMIP defines a set of attributes that can be associated with objects:

| Attribute | Description |
|-----------|-------------|
| Cryptographic Algorithm | The algorithm used by the key |
| Cryptographic Length | The length of the key in bits |
| Cryptographic Usage Mask | The allowed uses of the key |
| Activation Date | The date when the key becomes active |
| Expiration Date | The date when the key expires |
| State | The state of the key in its lifecycle |
| ... and more | |

## KMIP Message Structure

KMIP messages are encoded using a Tag, Type, Length, Value (TTLV) format:

```
┌───────────┬──────┬────────┬───────────────────┬─────────┐
│    Tag    │ Type │ Length │       Value       │ Padding │
│  (4 bytes)│(1 B) │(4 bytes)│  (Length bytes)   │(0-7 B)  │
└───────────┴──────┴────────┴───────────────────┴─────────┘
```

KMIP messages have a hierarchical structure:

```
┌─────────────────────────────────────────────────────────┐
│                     Request Message                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │               Request Header                     │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │               Batch Item                         │    │
│  ├─────────────────────────────────────────────────┤    │
│  │                                                 │    │
│  │  ┌─────────────────────────────────────────┐    │    │
│  │  │           Operation                     │    │    │
│  │  └─────────────────────────────────────────┘    │    │
│  │                                                 │    │
│  │  ┌─────────────────────────────────────────┐    │    │
│  │  │         Request Payload                 │    │    │
│  │  └─────────────────────────────────────────┘    │    │
│  │                                                 │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## KMIP Transport

KMIP uses TLS for transport security. The default port for KMIP is 5696.

## KMIP Profiles

KMIP defines profiles for specific use cases:

| Profile | Description |
|---------|-------------|
| Baseline | Basic key management operations |
| Symmetric Key Lifecycle | Lifecycle management for symmetric keys |
| Asymmetric Key Lifecycle | Lifecycle management for asymmetric keys |
| Basic Cryptographic | Basic cryptographic operations |
| Advanced Cryptographic | Advanced cryptographic operations |
| Storage Array with Self-Encrypting Drives | Key management for storage arrays |
| ... and more | |

## KMIP Conformance

KMIP defines conformance levels for clients and servers:

| Conformance Level | Description |
|-------------------|-------------|
| KMIP Lite | Basic key management operations |
| KMIP Basic | Basic key management and cryptographic operations |
| KMIP Full | Full key management and cryptographic operations |

Our server aims to conform to the KMIP Basic level.

## References

- [OASIS KMIP Technical Committee](https://www.oasis-open.org/committees/kmip/)
- [KMIP 2.0 Specification](https://docs.oasis-open.org/kmip/kmip-spec/v2.0/os/kmip-spec-v2.0-os.html)
- [KMIP 2.0 Profiles](https://docs.oasis-open.org/kmip/kmip-profiles/v2.0/os/kmip-profiles-v2.0-os.html)
- [KMIP 2.0 Test Cases](https://docs.oasis-open.org/kmip/kmip-testcases/v2.0/os/kmip-testcases-v2.0-os.html)
- [KMIP 2.0 Usage Guide](https://docs.oasis-open.org/kmip/kmip-ug/v2.0/os/kmip-ug-v2.0-os.html)
