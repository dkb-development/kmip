package com.kmip.server.protocol.codec.encoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kmip.server.protocol.codec.encoder.model.TypeEncoderInfo;

/**
 * Spring-managed registry for KMIP type encoders - RESTRUCTURED with POJOs.
 * 
 * This class manages all available type encoders using a POJO-based approach
 * instead of raw Maps. This provides better type safety, easier debugging,
 * and centralized information management.
 * 
 * ARCHITECTURAL IMPROVEMENTS:
 * - Replaced Maps with structured POJOs
 * - Better exception handling and validation
 * - Centralized statistics in TypeEncoderInfo
 * - Easier debugging with structured data
 * - Type-safe operations
 * - Value-based encoder lookup
 */
@Component
public class TypeEncoderRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(TypeEncoderRegistry.class);
    
    // POJO-based registry instead of raw maps
    private final com.kmip.server.protocol.codec.encoder.model.TypeEncoderRegistry registry;
    
    @Autowired(required = false)
    private List<KmipTypeEncoder> typeEncoders;
    
    /**
     * Constructor initializes the POJO-based registry.
     */
    public TypeEncoderRegistry() {
        this.registry = new com.kmip.server.protocol.codec.encoder.model.TypeEncoderRegistry();
    }
    
    /**
     * Initializes the registry by discovering and registering all type encoders.
     */
    @PostConstruct
    public void initialize() {
        log.info("Initializing KMIP Type Encoder Registry...");
        
        if (typeEncoders != null) {
            for (KmipTypeEncoder encoder : typeEncoders) {
                registerEncoder(encoder);
            }
        }
        
        // Register default encoders if none were found
        if (registry.isEmpty()) {
            log.warn("No type encoders found in Spring context, registering default encoders");
            registerDefaultEncoders();
        }
        
        log.info("Type Encoder Registry initialized with {} encoders: {}", 
                registry.size(), getRegisteredTypeNames());
    }
    
    /**
     * Registers a type encoder.
     * 
     * @param encoder the encoder to register
     */
    public void registerEncoder(KmipTypeEncoder encoder) {
        if (encoder == null) {
            log.warn("Attempted to register null encoder");
            return;
        }
        
        byte typeCode = encoder.getTypeCode();
        String typeName = encoder.getTypeName();
        
        // Check if encoder already exists
        if (registry.hasEncoder(typeCode)) {
            TypeEncoderInfo existing = registry.getEncoderInfo(typeCode);
            log.warn("Replacing existing encoder for type 0x{}: {} -> {}", 
                    String.format("%02X", typeCode), 
                    existing.getEncoderClassName(),
                    encoder.getClass().getSimpleName());
            
            // Remove existing encoder
            registry.removeEncoder(typeCode);
        }
        
        // Register new encoder using POJO approach
        try {
            TypeEncoderInfo encoderInfo = registry.registerEncoder(encoder);
            log.debug("Registered encoder for type 0x{} ({}): {}", 
                    String.format("%02X", typeCode), typeName, encoder.getClass().getSimpleName());
        } catch (IllegalArgumentException e) {
            log.error("Failed to register encoder for type 0x{}: {}", 
                    String.format("%02X", typeCode), e.getMessage());
        }
    }
    
    /**
     * Gets an encoder for the specified type code.
     * 
     * @param typeCode the KMIP type code
     * @return the encoder, or null if not found
     */
    public KmipTypeEncoder getEncoder(byte typeCode) {
        return registry.getEncoder(typeCode);
    }
    
    /**
     * Gets an encoder for the specified value.
     * 
     * @param value the value to encode
     * @return the encoder that can handle this value, or null if not found
     */
    public KmipTypeEncoder getEncoderForValue(Object value) {
        return registry.getEncoderForValue(value);
    }
    
    /**
     * Checks if an encoder is available for the specified type code.
     * 
     * @param typeCode the KMIP type code
     * @return true if an encoder is available
     */
    public boolean hasEncoder(byte typeCode) {
        return registry.hasEncoder(typeCode);
    }
    
    /**
     * Gets all registered type codes.
     * 
     * @return array of registered type codes
     */
    public byte[] getRegisteredTypes() {
        List<Byte> types = registry.getRegisteredTypes();
        byte[] result = new byte[types.size()];
        for (int i = 0; i < types.size(); i++) {
            result[i] = types.get(i);
        }
        return result;
    }
    
    /**
     * Gets names of all registered types.
     * 
     * @return comma-separated list of type names
     */
    public String getRegisteredTypeNames() {
        return registry.getAllEncoderInfoSorted().stream()
                .map(TypeEncoderInfo::getTypeName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("none");
    }
    
    /**
     * Records a successful encode operation.
     * 
     * @param typeCode the type that was encoded
     * @param encodeTimeMs the time taken to encode
     * @param encodedBytes the number of bytes encoded
     */
    public void recordEncodeSuccess(byte typeCode, long encodeTimeMs, int encodedBytes) {
        registry.recordEncodeSuccess(typeCode, encodeTimeMs, encodedBytes);
    }
    
    /**
     * Records an encode error.
     * 
     * @param typeCode the type that failed to encode
     * @param errorMessage the error message
     */
    public void recordEncodeError(byte typeCode, String errorMessage) {
        registry.recordEncodeError(typeCode, errorMessage);
    }
    
    /**
     * Gets encoding statistics using the POJO-based approach.
     * 
     * @return map of statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get overall registry statistics
        com.kmip.server.protocol.codec.encoder.model.TypeEncoderRegistry.RegistryStatistics regStats = 
                registry.getStatistics();
        
        stats.put("registeredTypes", regStats.getActiveEncoderCount());
        stats.put("totalEncodeOperations", regStats.getTotalEncodeOperations());
        stats.put("totalEncodeErrors", regStats.getTotalEncodeErrors());
        stats.put("totalEncodedBytes", regStats.getTotalEncodedBytes());
        stats.put("overallSuccessRate", regStats.getOverallSuccessRate());
        stats.put("averageEncodeTimeMs", regStats.getAverageEncodeTimeMs());
        stats.put("overallThroughputBytesPerSecond", regStats.getOverallThroughputBytesPerSecond());
        stats.put("creationTime", regStats.getCreationTime());
        stats.put("lastRegistrationTime", regStats.getLastRegistrationTime());
        
        // Get individual encoder statistics
        Map<String, Object> typeStats = new HashMap<>();
        for (TypeEncoderInfo info : registry.getAllEncoderInfoSorted()) {
            String typeName = info.getTypeName();
            typeStats.put(typeName + "_successCount", info.getEncodeSuccessCount());
            typeStats.put(typeName + "_errorCount", info.getEncodeErrorCount());
            typeStats.put(typeName + "_successRate", info.getSuccessRate());
            typeStats.put(typeName + "_avgEncodeTime", info.getAverageEncodeTimeMs());
            typeStats.put(typeName + "_throughput", info.getThroughputBytesPerSecond());
            typeStats.put(typeName + "_totalBytes", info.getTotalEncodedBytes());
            typeStats.put(typeName + "_lastUsed", info.getLastUsedTime());
        }
        stats.put("typeStatistics", typeStats);
        
        return stats;
    }
    
    /**
     * Registers default encoders for basic KMIP types.
     * This is a fallback when no encoders are found in the Spring context.
     */
    private void registerDefaultEncoders() {
        // Note: These would be actual implementations
        // For now, we'll create placeholder registrations
        log.info("Default encoders would be registered here");
        
        // Example of what would be registered:
        // registerEncoder(new StructureTypeEncoder());
        // registerEncoder(new IntegerTypeEncoder());
        // registerEncoder(new EnumerationTypeEncoder());
        // registerEncoder(new BooleanTypeEncoder());
        // registerEncoder(new TextStringTypeEncoder());
        // registerEncoder(new ByteStringTypeEncoder());
        // registerEncoder(new DateTimeTypeEncoder());
    }
    
    /**
     * Clears all registered encoders.
     * Mainly used for testing.
     */
    public void clear() {
        registry.clear();
        log.debug("Type encoder registry cleared");
    }
    
    /**
     * Gets the number of registered encoders.
     * 
     * @return the number of registered encoders
     */
    public int size() {
        return registry.size();
    }
    
    /**
     * Gets detailed information about a specific encoder.
     * 
     * @param typeCode the KMIP type code
     * @return detailed encoder information, or null if not found
     */
    public TypeEncoderInfo getEncoderInfo(byte typeCode) {
        return registry.getEncoderInfo(typeCode);
    }
    
    /**
     * Gets encoders with recent errors for debugging.
     * 
     * @param withinMinutes the time window in minutes
     * @return list of encoders with recent errors
     */
    public List<TypeEncoderInfo> getEncodersWithRecentErrors(int withinMinutes) {
        return registry.getEncodersWithRecentErrors(withinMinutes);
    }
    
    /**
     * Gets the most frequently used encoders.
     * 
     * @param limit the maximum number of encoders to return
     * @return list of most used encoders
     */
    public List<TypeEncoderInfo> getMostUsedEncoders(int limit) {
        return registry.getMostUsedEncoders(limit);
    }
    
    /**
     * Gets encoders with the highest throughput.
     * 
     * @param limit the maximum number of encoders to return
     * @return list of encoders with highest throughput
     */
    public List<TypeEncoderInfo> getHighestThroughputEncoders(int limit) {
        return registry.getHighestThroughputEncoders(limit);
    }
}
