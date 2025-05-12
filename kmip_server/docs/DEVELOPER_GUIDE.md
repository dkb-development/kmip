# KMIP Server Developer's Guide

## Table of Contents
1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Architecture Overview](#architecture-overview)
4. [Development Setup](#development-setup)
5. [Implementing New KMIP Operations](#implementing-new-kmip-operations)
6. [Testing](#testing)
7. [Debugging](#debugging)
8. [Best Practices](#best-practices)
9. [Related Documentation](#related-documentation)

## Introduction

This guide is designed to help developers understand, maintain, and extend the KMIP (Key Management Interoperability Protocol) server implementation. The server is built using Java Spring Boot and implements the KMIP protocol for key management operations.

### Prerequisites
- Java 17 or higher
- Maven 3.8 or higher
- Understanding of KMIP protocol (see [KMIP Protocol Guide](kmip_protocol.md))
- Basic knowledge of Spring Boot

## Project Structure

### Directory Structure
```
kmip_server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── [package structure]
│   │   └── resources/
│   │       ├── application.properties
│   │       └── certificates/
│   └── test/
│       └── java/
│           └── com/
│               └── [test package structure]
├── docs/
│   ├── architecture.md
│   ├── kmip_operations.md
│   └── [other documentation]
└── pom.xml
```

### Package Structure and Layer Mapping

```
com.kmip.server/
├── protocol/                    # Protocol Layer
│   ├── ttlv/                   # TTLV encoding/decoding
│   │   ├── TTLVDecoder.java
│   │   ├── TTLVEncoder.java
│   │   └── TTLVMessage.java
│   ├── kmip/                   # KMIP message handling
│   │   ├── KMIPMessage.java
│   │   ├── KMIPRequest.java
│   │   └── KMIPResponse.java
│   └── exception/              # Protocol exceptions
│
├── operation/                  # Operation Layer
│   ├── base/                  # Base operation classes
│   │   ├── Operation.java
│   │   ├── OperationHandler.java
│   │   └── OperationFactory.java
│   ├── create/               # Create operation
│   │   ├── CreateOperation.java
│   │   └── CreateHandler.java
│   ├── get/                  # Get operation
│   │   ├── GetOperation.java
│   │   └── GetHandler.java
│   ├── destroy/              # Destroy operation
│   │   ├── DestroyOperation.java
│   │   └── DestroyHandler.java
│   └── registry/             # Operation registry
│
├── service/                   # Service Layer
│   ├── key/                  # Key management
│   │   ├── KeyService.java
│   │   └── KeyFactory.java
│   ├── crypto/               # Cryptographic operations
│   │   ├── CryptoService.java
│   │   └── CryptoProvider.java
│   └── auth/                 # Authentication
│       ├── AuthService.java
│       └── SecurityContext.java
│
├── storage/                   # Storage Layer
│   ├── repository/           # Data repositories
│   │   ├── KeyRepository.java
│   │   └── ObjectRepository.java
│   ├── entity/               # Database entities
│   │   ├── KeyEntity.java
│   │   └── ObjectEntity.java
│   └── config/               # Storage configuration
│
├── config/                    # Application Configuration
│   ├── ServerConfig.java
│   ├── SecurityConfig.java
│   └── DatabaseConfig.java
│
└── util/                      # Utilities
    ├── Constants.java
    ├── ValidationUtils.java
    └── SecurityUtils.java
```

### Layer Responsibilities

1. **Protocol Layer** (`com.kmip.server.protocol`)
   - Handles low-level KMIP protocol implementation
   - Manages TTLV encoding/decoding
   - Processes incoming/outgoing messages
   - Validates message structure
   - Located in: `protocol/` directory

2. **Operation Layer** (`com.kmip.server.operation`)
   - Implements KMIP operations
   - Each operation has its own package
   - Handles operation-specific logic
   - Manages operation registration
   - Located in: `operation/` directory

3. **Service Layer** (`com.kmip.server.service`)
   - Implements business logic
   - Manages key lifecycle
   - Handles cryptographic operations
   - Provides authentication/authorization
   - Located in: `service/` directory

4. **Storage Layer** (`com.kmip.server.storage`)
   - Manages data persistence
   - Handles database operations
   - Defines data models
   - Manages object lifecycle
   - Located in: `storage/` directory

### Key Components

1. **Protocol Components**
   - `TTLVDecoder/Encoder`: Handles TTLV format conversion
   - `KMIPMessage`: Base class for KMIP messages
   - `KMIPRequest/Response`: Request/response handling

2. **Operation Components**
   - `Operation`: Base interface for all operations
   - `OperationHandler`: Processes operation requests
   - `OperationFactory`: Creates operation instances
   - Operation-specific implementations (Create, Get, Destroy)

3. **Service Components**
   - `KeyService`: Manages key operations
   - `CryptoService`: Handles cryptographic functions
   - `AuthService`: Manages authentication

4. **Storage Components**
   - `KeyRepository`: Manages key persistence
   - `ObjectRepository`: Handles object storage
   - Entity classes for database mapping

### Configuration

- Application properties: `src/main/resources/application.properties`
- SSL certificates: `src/main/resources/certificates/`
- Database configuration: `config/DatabaseConfig.java`
- Security settings: `config/SecurityConfig.java`

## Architecture Overview

The KMIP server follows a layered architecture:

1. **Protocol Layer**
   - Handles KMIP message encoding/decoding
   - Implements TTLV (Tag-Type-Length-Value) format
   - Located in the protocol package

2. **Operation Layer**
   - Implements individual KMIP operations
   - Each operation is a separate service
   - Follows the operation flow defined in [KMIP Operation Flows](kmip_operation_flows.md)

3. **Service Layer**
   - Core business logic
   - Key management operations
   - Cryptographic operations

4. **Storage Layer**
   - Persistence of keys and objects
   - Database interactions

For detailed architecture, see [Architecture Documentation](architecture.md).

## Development Setup

1. Clone the repository
2. Install dependencies:
   ```bash
   mvn clean install
   ```
3. Configure the server:
   - Review `application.properties` or `application.yml`
   - Set up SSL certificates
   - Configure database connection

4. Run the server:
   ```bash
   mvn spring-boot:run
   ```

## Implementing New KMIP Operations

To implement a new KMIP operation:

1. **Create Operation Service**
   ```java
   @Service
   public class NewOperationService {
       // Implement operation logic
   }
   ```

2. **Define Operation Request/Response**
   ```java
   public class NewOperationRequest {
       // Define request structure
   }

   public class NewOperationResponse {
       // Define response structure
   }
   ```

3. **Implement Operation Handler**
   ```java
   @Component
   public class NewOperationHandler implements OperationHandler {
       @Override
       public OperationResponse handle(OperationRequest request) {
           // Implement operation handling
       }
   }
   ```

4. **Register Operation**
   - Add operation to the operation registry
   - Update operation factory

5. **Add Tests**
   - Unit tests for operation logic
   - Integration tests for operation flow
   - Client tests using pykmip

See [KMIP Operations Guide](kmip_operations.md) for detailed examples.

## Testing

### Unit Testing
- Use JUnit 5 for unit tests
- Mock dependencies using Mockito
- Test both success and failure scenarios

### Integration Testing
- Use Spring Boot test framework
- Test with actual database
- Verify operation flows

### End-to-End Testing
- Test different operation scenarios
- Verify protocol compliance
- Use KMIP protocol testing tools

## Debugging

1. **Logging**
   - Use SLF4J for logging
   - Configure log levels in `application.properties`
   - Check logs for operation flow

2. **Protocol Debugging**
   - Enable TTLV debugging
   - Use Wireshark for network analysis
   - Check message structure

3. **Common Issues**
   - SSL/TLS configuration
   - Database connection
   - Operation validation
   - Protocol version mismatch

## Best Practices

1. **Code Organization**
   - Follow package structure
   - Use consistent naming conventions
   - Document public APIs

2. **Error Handling**
   - Use proper exception hierarchy
   - Provide meaningful error messages
   - Log errors appropriately

3. **Security**
   - Follow security best practices
   - Validate all inputs
   - Use proper encryption
   - Secure key storage

4. **Performance**
   - Use connection pooling
   - Implement caching where appropriate
   - Monitor resource usage

## Related Documentation

- [KMIP Protocol Guide](kmip_protocol.md) - Protocol details
- [Architecture Documentation](architecture.md) - System architecture
- [Operation Flows](kmip_operation_flows.md) - Operation implementation flows
- [Message Structure](kmip_message_structure.md) - KMIP message format
- Individual operation guides:
  - [Create Operation](kmip_create_operation.md)
  - [Get Operation](kmip_get_operation.md)
  - [Destroy Operation](kmip_destroy_operation.md)

## Contributing

1. Follow the coding standards
2. Write tests for new features
3. Update documentation
4. Submit pull requests with clear descriptions
5. Ensure all tests pass
6. Update relevant documentation

## Support

For questions or issues:
1. Check existing documentation
2. Review issue tracker
3. Contact the development team

---

This guide is a living document. Please contribute to keep it up to date with the latest changes and best practices. 