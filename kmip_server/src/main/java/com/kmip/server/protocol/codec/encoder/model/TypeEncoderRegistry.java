package com.kmip.server.protocol.codec.encoder.model;

import com.kmip.server.protocol.codec.encoder.KmipTypeEncoder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * POJO-based registry for KMIP type encoders.
 * 
 * This class replaces the Map-based approach with a structured POJO design
 * that provides better type safety, easier debugging, and centralized
 * information management.
 */
public class TypeEncoderRegistry {
    
    private final LocalDateTime creationTime;
    private final Map<Byte, TypeEncoderInfo> encoderInfoMap;
    
    // Registry-level statistics
    private volatile long totalRegistrations = 0;
    private volatile long totalEncodeOperations = 0;
    private volatile long totalEncodeErrors = 0;
    private volatile long totalEncodedBytes = 0;
    private volatile LocalDateTime lastRegistrationTime;
    
    /**
     * Creates a new TypeEncoderRegistry.
     */
    public TypeEncoderRegistry() {
        this.creationTime = LocalDateTime.now();
        this.encoderInfoMap = new ConcurrentHashMap<>();
    }
    
    /**
     * Registers a type encoder.
     * 
     * @param encoder the encoder to register
     * @return the TypeEncoderInfo for the registered encoder
     * @throws IllegalArgumentException if encoder is null or type already registered
     */
    public TypeEncoderInfo registerEncoder(KmipTypeEncoder encoder) {
        if (encoder == null) {
            throw new IllegalArgumentException("Encoder cannot be null");
        }
        
        byte typeCode = encoder.getTypeCode();
        
        if (encoderInfoMap.containsKey(typeCode)) {
            throw new IllegalArgumentException(
                String.format("Encoder for type 0x%02X is already registered", typeCode));
        }
        
        TypeEncoderInfo encoderInfo = new TypeEncoderInfo(encoder);
        encoderInfoMap.put(typeCode, encoderInfo);
        
        totalRegistrations++;
        lastRegistrationTime = LocalDateTime.now();
        
        return encoderInfo;
    }
    
    /**
     * Gets encoder information for a specific type.
     * 
     * @param typeCode the KMIP type code
     * @return the encoder info, or null if not found
     */
    public TypeEncoderInfo getEncoderInfo(byte typeCode) {
        return encoderInfoMap.get(typeCode);
    }
    
    /**
     * Gets the encoder for a specific type.
     * 
     * @param typeCode the KMIP type code
     * @return the encoder, or null if not found
     */
    public KmipTypeEncoder getEncoder(byte typeCode) {
        TypeEncoderInfo info = encoderInfoMap.get(typeCode);
        return info != null ? info.getEncoder() : null;
    }
    
    /**
     * Gets the encoder for a specific value type.
     * 
     * @param value the value to encode
     * @return the encoder that can handle this value, or null if not found
     */
    public KmipTypeEncoder getEncoderForValue(Object value) {
        if (value == null) {
            return null;
        }
        
        for (TypeEncoderInfo info : encoderInfoMap.values()) {
            if (info.getEncoder().canEncode(value)) {
                return info.getEncoder();
            }
        }
        return null;
    }
    
    /**
     * Checks if an encoder is registered for the given type.
     * 
     * @param typeCode the KMIP type code
     * @return true if an encoder is registered
     */
    public boolean hasEncoder(byte typeCode) {
        return encoderInfoMap.containsKey(typeCode);
    }
    
    /**
     * Records a successful encode operation.
     * 
     * @param typeCode the type that was encoded
     * @param encodeTimeMs the time taken to encode
     * @param encodedBytes the number of bytes encoded
     */
    public void recordEncodeSuccess(byte typeCode, long encodeTimeMs, int encodedBytes) {
        TypeEncoderInfo info = encoderInfoMap.get(typeCode);
        if (info != null) {
            info.recordEncodeSuccess(encodeTimeMs, encodedBytes);
            totalEncodeOperations++;
            totalEncodedBytes += encodedBytes;
        }
    }
    
    /**
     * Records an encode error.
     * 
     * @param typeCode the type that failed to encode
     * @param errorMessage the error message
     */
    public void recordEncodeError(byte typeCode, String errorMessage) {
        TypeEncoderInfo info = encoderInfoMap.get(typeCode);
        if (info != null) {
            info.recordEncodeError(errorMessage);
            totalEncodeOperations++;
            totalEncodeErrors++;
        }
    }
    
    /**
     * Gets all registered type codes.
     * 
     * @return sorted list of registered type codes
     */
    public List<Byte> getRegisteredTypes() {
        return encoderInfoMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all encoder information objects.
     * 
     * @return list of all encoder info objects
     */
    public List<TypeEncoderInfo> getAllEncoderInfo() {
        return new ArrayList<>(encoderInfoMap.values());
    }
    
    /**
     * Gets encoder information sorted by type code.
     * 
     * @return sorted list of encoder info objects
     */
    public List<TypeEncoderInfo> getAllEncoderInfoSorted() {
        return encoderInfoMap.values().stream()
                .sorted(Comparator.comparing(TypeEncoderInfo::getTypeCode))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets encoders with recent errors.
     * 
     * @param withinMinutes the time window in minutes
     * @return list of encoder info with recent errors
     */
    public List<TypeEncoderInfo> getEncodersWithRecentErrors(int withinMinutes) {
        return encoderInfoMap.values().stream()
                .filter(info -> info.hasRecentErrors(withinMinutes))
                .sorted(Comparator.comparing(TypeEncoderInfo::getLastErrorTime).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the most frequently used encoders.
     * 
     * @param limit the maximum number of encoders to return
     * @return list of most used encoders
     */
    public List<TypeEncoderInfo> getMostUsedEncoders(int limit) {
        return encoderInfoMap.values().stream()
                .sorted(Comparator.comparing(TypeEncoderInfo::getTotalOperations).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets encoders with the highest throughput.
     * 
     * @param limit the maximum number of encoders to return
     * @return list of encoders with highest throughput
     */
    public List<TypeEncoderInfo> getHighestThroughputEncoders(int limit) {
        return encoderInfoMap.values().stream()
                .filter(info -> info.getTotalOperations() > 0) // Only include used encoders
                .sorted(Comparator.comparing(TypeEncoderInfo::getThroughputBytesPerSecond).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets encoders with the lowest success rates.
     * 
     * @param limit the maximum number of encoders to return
     * @return list of encoders with lowest success rates
     */
    public List<TypeEncoderInfo> getLowestSuccessRateEncoders(int limit) {
        return encoderInfoMap.values().stream()
                .filter(info -> info.getTotalOperations() > 0) // Only include used encoders
                .sorted(Comparator.comparing(TypeEncoderInfo::getSuccessRate))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Removes an encoder from the registry.
     * 
     * @param typeCode the type code to remove
     * @return the removed encoder info, or null if not found
     */
    public TypeEncoderInfo removeEncoder(byte typeCode) {
        return encoderInfoMap.remove(typeCode);
    }
    
    /**
     * Clears all registered encoders.
     */
    public void clear() {
        encoderInfoMap.clear();
        totalRegistrations = 0;
        totalEncodeOperations = 0;
        totalEncodeErrors = 0;
        totalEncodedBytes = 0;
        lastRegistrationTime = null;
    }
    
    /**
     * Gets the number of registered encoders.
     * 
     * @return the number of registered encoders
     */
    public int size() {
        return encoderInfoMap.size();
    }
    
    /**
     * Checks if the registry is empty.
     * 
     * @return true if no encoders are registered
     */
    public boolean isEmpty() {
        return encoderInfoMap.isEmpty();
    }
    
    /**
     * Gets overall registry statistics.
     * 
     * @return registry statistics
     */
    public RegistryStatistics getStatistics() {
        return new RegistryStatistics(
            creationTime,
            totalRegistrations,
            totalEncodeOperations,
            totalEncodeErrors,
            totalEncodedBytes,
            lastRegistrationTime,
            encoderInfoMap.size(),
            calculateOverallSuccessRate(),
            calculateAverageEncodeTime(),
            calculateOverallThroughput()
        );
    }
    
    /**
     * Calculates the overall success rate across all encoders.
     * 
     * @return overall success rate (0.0 to 1.0)
     */
    private double calculateOverallSuccessRate() {
        if (totalEncodeOperations == 0) {
            return 1.0;
        }
        return (double) (totalEncodeOperations - totalEncodeErrors) / totalEncodeOperations;
    }
    
    /**
     * Calculates the average encode time across all encoders.
     * 
     * @return average encode time in milliseconds
     */
    private double calculateAverageEncodeTime() {
        long totalTime = encoderInfoMap.values().stream()
                .mapToLong(TypeEncoderInfo::getTotalEncodeTimeMs)
                .sum();
        
        long totalSuccessfulOperations = encoderInfoMap.values().stream()
                .mapToLong(TypeEncoderInfo::getEncodeSuccessCount)
                .sum();
        
        return totalSuccessfulOperations > 0 ? (double) totalTime / totalSuccessfulOperations : 0.0;
    }
    
    /**
     * Calculates the overall throughput across all encoders.
     * 
     * @return overall throughput in bytes per second
     */
    private double calculateOverallThroughput() {
        long totalTime = encoderInfoMap.values().stream()
                .mapToLong(TypeEncoderInfo::getTotalEncodeTimeMs)
                .sum();
        
        if (totalTime > 0) {
            return (double) totalEncodedBytes / (totalTime / 1000.0);
        }
        return 0.0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "TypeEncoderRegistry{encoders=%d, operations=%d, errors=%d, successRate=%.2f%%, " +
            "throughput=%.2f bytes/sec}",
            encoderInfoMap.size(), totalEncodeOperations, totalEncodeErrors,
            calculateOverallSuccessRate() * 100, calculateOverallThroughput()
        );
    }
    
    /**
     * POJO for registry-level statistics.
     */
    public static class RegistryStatistics {
        private final LocalDateTime creationTime;
        private final long totalRegistrations;
        private final long totalEncodeOperations;
        private final long totalEncodeErrors;
        private final long totalEncodedBytes;
        private final LocalDateTime lastRegistrationTime;
        private final int activeEncoderCount;
        private final double overallSuccessRate;
        private final double averageEncodeTimeMs;
        private final double overallThroughputBytesPerSecond;
        
        public RegistryStatistics(LocalDateTime creationTime, long totalRegistrations,
                                long totalEncodeOperations, long totalEncodeErrors,
                                long totalEncodedBytes, LocalDateTime lastRegistrationTime,
                                int activeEncoderCount, double overallSuccessRate,
                                double averageEncodeTimeMs, double overallThroughputBytesPerSecond) {
            this.creationTime = creationTime;
            this.totalRegistrations = totalRegistrations;
            this.totalEncodeOperations = totalEncodeOperations;
            this.totalEncodeErrors = totalEncodeErrors;
            this.totalEncodedBytes = totalEncodedBytes;
            this.lastRegistrationTime = lastRegistrationTime;
            this.activeEncoderCount = activeEncoderCount;
            this.overallSuccessRate = overallSuccessRate;
            this.averageEncodeTimeMs = averageEncodeTimeMs;
            this.overallThroughputBytesPerSecond = overallThroughputBytesPerSecond;
        }
        
        // Getters
        public LocalDateTime getCreationTime() { return creationTime; }
        public long getTotalRegistrations() { return totalRegistrations; }
        public long getTotalEncodeOperations() { return totalEncodeOperations; }
        public long getTotalEncodeErrors() { return totalEncodeErrors; }
        public long getTotalEncodedBytes() { return totalEncodedBytes; }
        public LocalDateTime getLastRegistrationTime() { return lastRegistrationTime; }
        public int getActiveEncoderCount() { return activeEncoderCount; }
        public double getOverallSuccessRate() { return overallSuccessRate; }
        public double getAverageEncodeTimeMs() { return averageEncodeTimeMs; }
        public double getOverallThroughputBytesPerSecond() { return overallThroughputBytesPerSecond; }
        
        @Override
        public String toString() {
            return String.format(
                "RegistryStatistics{encoders=%d, operations=%d, errors=%d, successRate=%.2f%%, " +
                "avgTime=%.2fms, throughput=%.2f bytes/sec, totalBytes=%d}",
                activeEncoderCount, totalEncodeOperations, totalEncodeErrors,
                overallSuccessRate * 100, averageEncodeTimeMs, 
                overallThroughputBytesPerSecond, totalEncodedBytes
            );
        }
    }
}
