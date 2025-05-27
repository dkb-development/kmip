# KMIP Server Architecture Documentation

## Overview

This document describes the modular, layered architecture of our Enterprise-grade KMIP (Key Management Interoperability Protocol) Server implementation. The architecture follows SOLID principles, separation of concerns, and provides a clean, maintainable codebase that is easy to extend and debug.

## Architecture Principles

- **Modular Design**: Each component has a single responsibility
- **Loose Coupling**: Components interact through well-defined interfaces
- **High Cohesion**: Related functionality is grouped together
- **Extensibility**: Easy to add new KMIP operations and protocol versions
- **Testability**: Each layer can be tested independently
- **Configuration-Driven**: Behavior controlled through unified configuration

## Layered Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                           │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   TCP Server    │  │   SSL/TLS       │  │   HTTP REST     │ │
│  │   (Primary)     │  │   (Optional)    │  │   (Future)      │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    PROTOCOL LAYER                               │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   KMIP Parser   │  │   KMIP Encoder  │  │   Message       │ │
│  │   (Decoder)     │  │   (Encoder)     │  │   Validator     │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                        │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Request       │  │   Operation     │  │   Response      │ │
│  │   Handler       │  │   Handlers      │  │   Builder       │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                               │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Key Mgmt      │  │   Crypto        │  │   Audit &       │ │
│  │   Service       │  │   Service       │  │   Logging       │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DATA ACCESS LAYER                           │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   In-Memory     │  │   Database      │  │   External KMS  │ │
│  │   Storage       │  │   Storage       │  │   Integration   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Configuration Management

**Location**: `com.kmip.server.config`

- **Unified Configuration**: Single `KmipServerConfig` class manages all settings
- **Adapter Pattern**: Component-specific config adapters (ParserConfig, EncoderConfig)
- **Environment-Driven**: Properties can be overridden via application.properties
- **Type Safety**: Strongly typed configuration with validation

```
KmipServerConfig
├── ServerSettings
├── TransportSettings
│   └── TcpSettings
├── ProtocolSettings
├── CodecSettings
│   ├── ParserSettings
│   └── EncoderSettings
├── SecuritySettings
└── ManagementSettings
```

### 2. Protocol Layer - The Heart of KMIP Implementation

**Location**: `com.kmip.server.protocol`

The Protocol Layer is the core of our KMIP server, responsible for implementing the complete KMIP 2.0 specification. It handles the complex TTLV (Tag-Type-Length-Value) encoding/decoding, message validation, and protocol compliance.

#### Protocol Layer Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    PROTOCOL LAYER DETAIL                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │   KMIP Parser   │    │   KMIP Encoder  │    │  Message    │ │
│  │   (Decoder)     │    │   (Encoder)     │    │ Validator   │ │
│  │                 │    │                 │    │             │ │
│  │ • TTLV Parsing  │    │ • TTLV Encoding │    │ • Structure │ │
│  │ • Type Safety   │    │ • Field Order   │    │ • Compliance│ │
│  │ • Error Handling│    │ • Spec Adherence│    │ • Integrity │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│           │                       │                     │      │
│           └───────────────────────┼─────────────────────┘      │
│                                   │                            │
│  ┌─────────────────┐    ┌─────────▼─────────┐    ┌─────────────┐ │
│  │   Tag Registry  │    │   Message Factory │    │  Protocol   │ │
│  │                 │    │                   │    │  Version    │ │
│  │ • Tag Mapping   │    │ • Message Creation│    │  Manager    │ │
│  │ • Type Registry │    │ • Response Builder│    │ • Version   │ │
│  │ • Validation    │    │ • Error Responses │    │   Negotiation│ │
│  └─────────────────┘    └───────────────────┘    └─────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### Core Components Deep Dive

##### 1. KMIP Parser (`codec.KmipParser`)

**Primary Responsibilities:**
- **TTLV Decoding**: Converts binary KMIP messages into structured Java objects
- **Type Safety**: Validates TTLV types using `KmipTtlvType` enum
- **Error Recovery**: Graceful handling of malformed messages
- **Performance**: Efficient parsing with minimal memory allocation

**Key Features:**
```java
// Type-safe parsing with enum validation
KmipTtlvType ttlvType = KmipTtlvType.fromCode(type);
switch (ttlvType) {
    case STRUCTURE:
        return parseStructure(valueBuffer);
    case INTEGER:
        return parseInteger(valueBuffer);
    case ENUMERATION:
        return parseEnumeration(valueBuffer);
    // ... other types
}
```

**Error Handling Strategy:**
- **Detailed Context**: Each error includes tag, position, and expected vs. actual data
- **Graceful Degradation**: Unknown types can be handled based on configuration
- **Validation Layers**: Multiple validation checkpoints throughout parsing

**Configuration Options:**
```properties
kmip.codec.parser.maxMessageSizeBytes=1048576    # 1MB limit
kmip.codec.parser.maxNestingDepth=10             # Prevent stack overflow
kmip.codec.parser.allowUnknownTypes=true         # Forward compatibility
kmip.codec.parser.strictTagValidation=false      # Client compatibility
```

##### 2. KMIP Encoder (`codec.KmipEncoder`)

**Primary Responsibilities:**
- **TTLV Encoding**: Converts Java objects into KMIP-compliant binary format
- **Specification Compliance**: Ensures strict adherence to KMIP 2.0 standard
- **Field Ordering**: Maintains required field sequence per KMIP specification
- **Optimization**: Efficient encoding with buffer management

**Key Features:**
```java
// Structured encoding with validation
public byte[] encodeMessage(KmipMessage message) {
    validateMessageStructure(message);
    ByteBuffer buffer = allocateBuffer();

    for (Map.Entry<Integer, Object> field : message.getFields().entrySet()) {
        encodeField(buffer, field.getKey(), field.getValue());
    }

    return buffer.array();
}
```

**Validation Mechanisms:**
- **Field Ordering**: Validates KMIP-required field sequences
- **Type Consistency**: Ensures value types match TTLV type declarations
- **Length Validation**: Verifies length fields match actual data
- **Padding Compliance**: Adds required 8-byte alignment padding

**Performance Optimizations:**
```java
// Buffer pooling for high-throughput scenarios
private final ThreadLocal<ByteBuffer> bufferPool =
    ThreadLocal.withInitial(() -> ByteBuffer.allocate(8192));
```

##### 3. Message Structures (`message.KmipMessage`)

**Design Philosophy:**
- **Flexibility**: Generic structure supporting any KMIP message type
- **Type Safety**: Strongly typed accessors for common field types
- **Immutability**: Optional immutable message creation
- **Extensibility**: Easy addition of new field types

**Core Structure:**
```java
public class KmipMessage {
    private final Map<Integer, Object> fields = new LinkedHashMap<>();

    // Type-safe field access
    public Optional<String> getStringField(int tag) {
        return Optional.ofNullable(fields.get(tag))
                      .filter(String.class::isInstance)
                      .map(String.class::cast);
    }

    // Nested structure support
    public Optional<KmipMessage> getStructureField(int tag) {
        return Optional.ofNullable(fields.get(tag))
                      .filter(KmipMessage.class::isInstance)
                      .map(KmipMessage.class::cast);
    }
}
```

##### 4. Tag Registry System (`tag.KmipTagResolver`)

**Centralized Tag Management:**
- **Tag Definitions**: All KMIP tags defined in one location
- **Type Mapping**: Associates tags with expected TTLV types
- **Validation Rules**: Defines required vs. optional fields
- **Documentation**: Self-documenting tag definitions

**Tag Organization:**
```java
public class KmipTagResolver {
    // Request/Response Structure Tags
    public static final int TAG_REQUEST_MESSAGE = 0x420078;
    public static final int TAG_RESPONSE_MESSAGE = 0x42007B;
    public static final int TAG_REQUEST_HEADER = 0x420077;
    public static final int TAG_RESPONSE_HEADER = 0x42007A;

    // Operation Tags
    public static final int TAG_OPERATION = 0x42005C;
    public static final int TAG_RESULT_STATUS = 0x42007F;
    public static final int TAG_RESULT_REASON = 0x42007E;

    // Object Tags
    public static final int TAG_OBJECT_TYPE = 0x420057;
    public static final int TAG_UNIQUE_IDENTIFIER = 0x420094;
    public static final int TAG_SYMMETRIC_KEY = 0x42008F;
}
```

##### 5. Type Registry System (`codec.parser/encoder`)

**Type Safety Infrastructure:**
- **Parser Registry**: Maps TTLV types to parsing functions
- **Encoder Registry**: Maps Java types to encoding functions
- **Validation Registry**: Type-specific validation rules
- **Extension Points**: Easy addition of custom types

**Registry Architecture:**
```java
// Parser type registry
public class TypeParserRegistry {
    private final Map<KmipTtlvType, TypeParser> parsers = new EnumMap<>();

    public void registerParser(KmipTtlvType type, TypeParser parser) {
        parsers.put(type, parser);
    }

    public Object parse(KmipTtlvType type, ByteBuffer buffer) {
        TypeParser parser = parsers.get(type);
        if (parser == null) {
            throw new KmipParseException("No parser for type: " + type);
        }
        return parser.parse(buffer);
    }
}
```

##### 6. Message Validator (`codec.validator.MessageValidator`)

**Comprehensive Validation:**
- **Structure Validation**: Ensures proper KMIP message hierarchy
- **Field Validation**: Validates required fields are present
- **Type Validation**: Confirms field types match expectations
- **Business Logic Validation**: Operation-specific validation rules

**Validation Layers:**
```java
public class MessageValidator {
    // Structural validation
    public void validateMessageStructure(KmipMessage message) {
        validateRequiredFields(message);
        validateFieldTypes(message);
        validateFieldOrdering(message);
    }

    // Operation-specific validation
    public void validateCreateRequest(KmipMessage payload) {
        requireField(payload, TAG_OBJECT_TYPE);
        validateObjectType(payload.getField(TAG_OBJECT_TYPE));
        // ... additional validation
    }
}
```

#### Protocol Layer Benefits

##### 1. **Enhanced Modularity**
- **Separation of Concerns**: Each component has a single, well-defined responsibility
- **Interface-Based Design**: Components interact through clean interfaces
- **Pluggable Architecture**: Easy to swap implementations (e.g., different encoders)

##### 2. **Superior Debugging Capabilities**
- **Detailed Error Context**: Every error includes precise location and context
- **Hex Dump Logging**: Visual inspection of binary message content
- **Validation Checkpoints**: Multiple validation layers pinpoint issues quickly
- **Performance Metrics**: Built-in timing and throughput monitoring

```java
// Example debug output
log.debug("Parsing TTLV field: tag=0x420057, type=ENUMERATION, length=4, value=0x00000002");
log.debug("Hex dump: 42 00 57 05 00 00 00 04 00 00 00 02 00 00 00 00");
```

##### 3. **Easy Protocol Extension**
- **Enum-Based Types**: New TTLV types added via enum extension
- **Registry Pattern**: New parsers/encoders registered dynamically
- **Version Management**: Protocol version-specific behavior
- **Backward Compatibility**: Graceful handling of older protocol versions

##### 4. **Protocol Version Flexibility**
- **Version Negotiation**: Automatic protocol version detection
- **Feature Flags**: Version-specific feature enablement
- **Compatibility Layers**: Support for multiple KMIP versions simultaneously
- **Migration Path**: Smooth upgrade path for protocol versions

```java
// Version-specific handling
if (protocolVersion.isNewerThan(KmipProtocolVersion.VERSION_1_4)) {
    // Use KMIP 2.0 features
    handleExtendedAttributes(message);
} else {
    // Fallback to KMIP 1.4 behavior
    handleLegacyAttributes(message);
}
```

#### Performance Characteristics

##### Memory Management
- **Buffer Pooling**: Reuse of byte buffers reduces GC pressure
- **Lazy Parsing**: Parse only required fields for better performance
- **Streaming Support**: Handle large messages without loading entirely into memory

##### Throughput Optimization
- **Parallel Processing**: Multiple messages can be processed concurrently
- **Caching**: Frequently used parsers and encoders are cached
- **Zero-Copy Operations**: Minimize data copying during processing

##### Scalability Features
- **Thread Safety**: All components are thread-safe for concurrent access
- **Resource Limits**: Configurable limits prevent resource exhaustion
- **Monitoring**: Built-in metrics for performance monitoring

### 3. Business Logic Layer

**Location**: `com.kmip.server.operation`

#### Request Handler (`KmipRequestHandler`)
- **Single Entry Point**: All KMIP requests flow through this handler
- **Operation Routing**: Dynamically routes requests to appropriate handlers
- **Error Management**: Centralized error handling and response building
- **Protocol Compliance**: Ensures KMIP 2.0 compliant responses

#### Operation Handlers
- **Strategy Pattern**: Each KMIP operation has its own handler
- **Interface-Based**: All handlers implement `OperationHandler` interface
- **Extensible**: Easy to add new operations by implementing the interface

```
OperationHandler Interface
├── CreateOperationHandler (KMIP Operation 0x000001)
├── GetOperationHandler (KMIP Operation 0x00000A)
├── DestroyOperationHandler (KMIP Operation 0x000014)
└── [Future Operations...]
```

### 4. Service Layer

**Location**: `com.kmip.server.service`

#### Key Management Service
- **Abstraction**: Interface-based design for multiple implementations
- **Current Implementations**:
  - `InMemoryKeyManagementService`: For development/testing
  - `ExternalKeyManagementService`: For production KMS integration
- **Strategy Pattern**: Switchable implementations via Spring profiles

### 5. Enumeration System

**Location**: `com.kmip.server.core.enums`

Comprehensive enum system replacing hardcoded constants:

- **KmipTtlvType**: TTLV type codes with validation methods
- **KmipObjectType**: Object type definitions with metadata
- **KmipCryptographicAlgorithm**: Algorithm codes with JCA mappings
- **KmipResultStatus**: Operation result status codes
- **KmipResultReason**: Detailed error reason codes
- **KmipProtocolVersion**: Protocol version management
- **KmipOperationType**: Operation code definitions

## Key Architectural Benefits

### 1. Modularity & Loose Coupling

```
┌─────────────────┐    Interface    ┌─────────────────┐
│   Operation     │◄──────────────►│   Key Mgmt      │
│   Handlers      │                │   Service       │
└─────────────────┘                └─────────────────┘
        │                                   │
        │ Uses                              │ Implements
        ▼                                   ▼
┌─────────────────┐                ┌─────────────────┐
│   KMIP Enums    │                │   Storage       │
│   & Constants   │                │   Backends      │
└─────────────────┘                └─────────────────┘
```

- Components interact through interfaces, not concrete implementations
- Easy to swap implementations (e.g., in-memory vs. database storage)
- Changes in one layer don't affect others

### 2. Enhanced Debugging Capabilities

- **Structured Logging**: Each layer logs at appropriate levels
- **Error Context**: Exceptions include detailed context information
- **Request Tracing**: Full request lifecycle tracking
- **Configuration Visibility**: Runtime configuration inspection

### 3. Easy Operation Extension

To add a new KMIP operation:

1. **Create Handler**: Implement `OperationHandler` interface
2. **Register Handler**: Add to operation handler registry
3. **Add Enum**: Define operation code in `KmipOperationType`
4. **Implement Logic**: Business logic in the handler
5. **Test**: Unit test the new handler

Example:
```java
@Component
public class NewOperationHandler implements OperationHandler {
    @Override
    public KmipMessage handle(KmipMessage header, KmipMessage payload) {
        // Implementation
    }

    @Override
    public int getOperationCode() {
        return KmipOperationType.NEW_OPERATION.getCode();
    }
}
```

### 4. Protocol Version Flexibility

- **Version Negotiation**: Built-in protocol version handling
- **Backward Compatibility**: Support for multiple KMIP versions
- **Feature Flags**: Version-specific feature enablement
- **Graceful Degradation**: Fallback to supported features

## Error Handling Strategy

### Hierarchical Error Management

```
┌─────────────────────────────────────────────────────────────┐
│                    Error Hierarchy                         │
├─────────────────────────────────────────────────────────────┤
│  KmipException (Base)                                      │
│  ├── KmipParseException (Protocol Layer)                   │
│  ├── KmipEncodeException (Protocol Layer)                  │
│  ├── KmipValidationException (Business Layer)              │
│  └── KmipServiceException (Service Layer)                  │
└─────────────────────────────────────────────────────────────┘
```

### Error Response Building

- **Consistent Format**: All errors follow KMIP 2.0 error response format
- **Detailed Reasons**: Uses `KmipResultReason` enum for specific error codes
- **Client Compatibility**: Ensures PyKMIP and other client compatibility

## Testing Strategy

### Layer-Specific Testing

- **Unit Tests**: Each component tested in isolation
- **Integration Tests**: Layer interaction testing
- **Protocol Tests**: KMIP compliance verification
- **Client Compatibility Tests**: Real client testing

### Mock Strategy

- **Service Layer Mocking**: Mock external dependencies
- **Configuration Mocking**: Test different configuration scenarios
- **Error Scenario Testing**: Comprehensive error path testing

## Future Extensibility

### Adding New Protocol Versions

1. **Extend Enum**: Add new version to `KmipProtocolVersion`
2. **Feature Detection**: Implement version-specific features
3. **Backward Compatibility**: Maintain support for older versions
4. **Testing**: Comprehensive version compatibility testing

### Adding New Transport Protocols

1. **Interface Implementation**: Implement transport interface
2. **Configuration**: Add transport-specific configuration
3. **Registration**: Register with transport factory
4. **Testing**: Protocol-specific testing

### External System Integration

1. **Service Interface**: Implement service interfaces
2. **Configuration**: Add integration-specific settings
3. **Error Handling**: Map external errors to KMIP errors
4. **Monitoring**: Add integration-specific monitoring

## Performance Considerations

- **Connection Pooling**: Efficient connection management
- **Buffer Management**: Configurable buffer sizes for optimal memory usage
- **Caching Strategy**: Configurable caching for frequently accessed data
- **Async Processing**: Support for asynchronous operation handling

## Security Architecture

- **SSL/TLS Support**: Configurable transport security
- **Authentication**: Pluggable authentication mechanisms
- **Authorization**: Role-based access control
- **Audit Logging**: Comprehensive security event logging

## Monitoring & Observability

- **Metrics Collection**: JMX and custom metrics
- **Health Checks**: Component health monitoring
- **Performance Monitoring**: Operation timing and throughput
- **Error Rate Tracking**: Error frequency and patterns

## Development Guidelines

### Code Organization Best Practices

#### Package Structure
```
com.kmip.server
├── config/                 # Configuration management
├── core/
│   ├── enums/             # KMIP protocol enumerations
│   └── exception/         # Custom exception hierarchy
├── operation/             # KMIP operation handlers
├── protocol/
│   ├── codec/             # Encoding/decoding logic
│   ├── message/           # Message structures
│   └── tag/               # KMIP tag definitions
├── service/               # Business services
└── transport/
    └── tcp/               # Transport layer implementations
```

#### Naming Conventions
- **Classes**: PascalCase with descriptive names (e.g., `CreateOperationHandler`)
- **Methods**: camelCase with verb-noun pattern (e.g., `parseKmipMessage`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `OPERATION_CREATE`)
- **Enums**: PascalCase with descriptive values (e.g., `KmipTtlvType.STRUCTURE`)

### Implementation Patterns

#### 1. Strategy Pattern for Operations
```java
// Operation handler registration
@PostConstruct
public void initializeHandlers() {
    operationHandlerMap.put(KmipOperationType.CREATE.getCode(), createHandler);
    operationHandlerMap.put(KmipOperationType.GET.getCode(), getHandler);
    // Add new operations here
}
```

#### 2. Factory Pattern for Message Creation
```java
// Centralized message creation
public class KmipMessageFactory {
    public static KmipMessage createSuccessResponse(int operationCode) {
        // Standardized response creation
    }

    public static KmipMessage createErrorResponse(KmipResultReason reason) {
        // Standardized error response creation
    }
}
```

#### 3. Builder Pattern for Complex Objects
```java
// Configuration builder for complex setups
KmipServerConfig config = KmipServerConfig.builder()
    .withTcpPort(5696)
    .withMaxConnections(100)
    .withSslEnabled(true)
    .build();
```

## Debugging Guide

### Common Debugging Scenarios

#### 1. Message Parsing Issues
```
Error Location: KmipParser.parseValue()
Common Causes:
- Invalid TTLV type codes
- Incorrect message structure
- Buffer underflow/overflow

Debug Steps:
1. Enable detailed logging: kmip.codec.parser.enableDetailedLogging=true
2. Check hex dump of incoming message
3. Verify TTLV structure against KMIP spec
4. Validate tag-type-length alignment
```

#### 2. Operation Handler Errors
```
Error Location: Operation handlers
Common Causes:
- Missing required fields in request
- Invalid operation parameters
- Service layer exceptions

Debug Steps:
1. Check operation handler registration
2. Verify request payload structure
3. Enable service layer logging
4. Validate business logic flow
```

#### 3. Encoding Issues
```
Error Location: KmipEncoder.encode()
Common Causes:
- Invalid field ordering
- Missing required fields
- Type conversion errors

Debug Steps:
1. Enable hex dump logging: kmip.codec.encoder.enableHexDumpLogging=true
2. Compare output with KMIP specification
3. Verify field ordering requirements
4. Check type mappings
```

### Logging Configuration

#### Log Levels by Component
```properties
# Root logger
logging.level.com.kmip.server=INFO

# Protocol layer - detailed parsing/encoding
logging.level.com.kmip.server.protocol=DEBUG

# Operation handlers - business logic
logging.level.com.kmip.server.operation=INFO

# Service layer - external integrations
logging.level.com.kmip.server.service=WARN

# Transport layer - connection management
logging.level.com.kmip.server.transport=INFO
```

#### Structured Logging Format
```json
{
  "timestamp": "2024-01-15T10:30:00.000Z",
  "level": "INFO",
  "component": "CreateOperationHandler",
  "operation": "CREATE",
  "requestId": "req-12345",
  "message": "Successfully created key",
  "keyId": "key-67890",
  "algorithm": "AES",
  "keyLength": 256
}
```

## Performance Optimization

### Memory Management
- **Buffer Pooling**: Reuse byte buffers for encoding/decoding
- **Object Pooling**: Pool frequently created objects
- **Garbage Collection**: Minimize object allocation in hot paths

### Connection Handling
- **Connection Pooling**: Efficient TCP connection management
- **Keep-Alive**: Reduce connection overhead
- **Timeout Management**: Prevent resource leaks

### Caching Strategy
```java
// Example: Operation handler caching
@Cacheable(value = "operationHandlers", key = "#operationCode")
public OperationHandler getHandler(int operationCode) {
    return operationHandlerMap.get(operationCode);
}
```

## Security Implementation

### Transport Security
```yaml
# SSL/TLS Configuration
kmip:
  server:
    security:
      enableSsl: true
      keystorePath: "/path/to/keystore.jks"
      keystorePassword: "${KEYSTORE_PASSWORD}"
      requireClientAuth: true
```

### Authentication & Authorization
```java
// Example: Role-based operation access
@PreAuthorize("hasRole('KMIP_ADMIN') or hasRole('KEY_MANAGER')")
public KmipMessage handleCreateOperation(KmipMessage request) {
    // Implementation
}
```

### Audit Logging
```java
// Security event logging
@EventListener
public void handleSecurityEvent(SecurityEvent event) {
    auditLogger.info("Security event: {} by user: {} at: {}",
        event.getType(), event.getUser(), event.getTimestamp());
}
```

## Testing Framework

### Unit Testing Structure
```java
@ExtendWith(MockitoExtension.class)
class CreateOperationHandlerTest {

    @Mock
    private KeyManagementService keyService;

    @InjectMocks
    private CreateOperationHandler handler;

    @Test
    void shouldCreateSymmetricKey() {
        // Given
        KmipMessage request = createValidCreateRequest();

        // When
        KmipMessage response = handler.handle(null, request);

        // Then
        assertThat(response).isNotNull();
        verify(keyService).createSymmetricKey(any(), anyInt(), anyInt());
    }
}
```

### Integration Testing
```java
@SpringBootTest
@TestPropertySource(properties = {
    "kmip.server.transport.tcp.port=0", // Random port
    "kmip.server.codec.parser.enableDetailedLogging=true"
})
class KmipServerIntegrationTest {

    @Test
    void shouldHandleCompleteCreateGetFlow() {
        // Test complete operation flow
    }
}
```

### Client Compatibility Testing
```python
# PyKMIP client compatibility test
def test_create_and_get_key():
    client = ProxyKmipClient(hostname='localhost', port=5696)

    # Create key
    key_id = client.create(
        algorithm=CryptographicAlgorithm.AES,
        length=256,
        usage_mask=CryptographicUsageMask.ENCRYPT | CryptographicUsageMask.DECRYPT
    )

    # Get key
    key = client.get(key_id)
    assert key is not None
```

## Deployment Considerations

### Configuration Management
```yaml
# Production configuration
spring:
  profiles:
    active: production

kmip:
  server:
    transport:
      tcp:
        port: 5696
        maxConnections: 1000
    protocol:
      maxBatchItems: 50
    security:
      enableSsl: true
```

### Monitoring Setup
```yaml
# Actuator endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info,prometheus
  endpoint:
    health:
      show-details: always
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jre-slim
COPY kmip-server.jar /app/
EXPOSE 5696
ENTRYPOINT ["java", "-jar", "/app/kmip-server.jar"]
```

This architecture provides a solid foundation for building a production-ready KMIP server that can scale, evolve, and integrate with various enterprise systems while maintaining strict protocol compliance and high reliability.
