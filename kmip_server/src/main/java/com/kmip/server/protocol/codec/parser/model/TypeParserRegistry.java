package com.kmip.server.protocol.codec.parser.model;

import com.kmip.server.protocol.codec.parser.KmipTypeParser;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * POJO-based registry for KMIP type parsers.
 * 
 * This class replaces the Map-based approach with a structured POJO design
 * that provides better type safety, easier debugging, and centralized
 * information management.
 */
public class TypeParserRegistry {
    
    private final LocalDateTime creationTime;
    private final Map<Byte, TypeParserInfo> parserInfoMap;
    
    // Registry-level statistics
    private volatile long totalRegistrations = 0;
    private volatile long totalParseOperations = 0;
    private volatile long totalParseErrors = 0;
    private volatile LocalDateTime lastRegistrationTime;
    
    /**
     * Creates a new TypeParserRegistry.
     */
    public TypeParserRegistry() {
        this.creationTime = LocalDateTime.now();
        this.parserInfoMap = new ConcurrentHashMap<>();
    }
    
    /**
     * Registers a type parser.
     * 
     * @param parser the parser to register
     * @return the TypeParserInfo for the registered parser
     * @throws IllegalArgumentException if parser is null or type already registered
     */
    public TypeParserInfo registerParser(KmipTypeParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("Parser cannot be null");
        }
        
        byte typeCode = parser.getTypeCode();
        
        if (parserInfoMap.containsKey(typeCode)) {
            throw new IllegalArgumentException(
                String.format("Parser for type 0x%02X is already registered", typeCode));
        }
        
        TypeParserInfo parserInfo = new TypeParserInfo(parser);
        parserInfoMap.put(typeCode, parserInfo);
        
        totalRegistrations++;
        lastRegistrationTime = LocalDateTime.now();
        
        return parserInfo;
    }
    
    /**
     * Gets parser information for a specific type.
     * 
     * @param typeCode the KMIP type code
     * @return the parser info, or null if not found
     */
    public TypeParserInfo getParserInfo(byte typeCode) {
        return parserInfoMap.get(typeCode);
    }
    
    /**
     * Gets the parser for a specific type.
     * 
     * @param typeCode the KMIP type code
     * @return the parser, or null if not found
     */
    public KmipTypeParser getParser(byte typeCode) {
        TypeParserInfo info = parserInfoMap.get(typeCode);
        return info != null ? info.getParser() : null;
    }
    
    /**
     * Checks if a parser is registered for the given type.
     * 
     * @param typeCode the KMIP type code
     * @return true if a parser is registered
     */
    public boolean hasParser(byte typeCode) {
        return parserInfoMap.containsKey(typeCode);
    }
    
    /**
     * Records a successful parse operation.
     * 
     * @param typeCode the type that was parsed
     * @param parseTimeMs the time taken to parse
     */
    public void recordParseSuccess(byte typeCode, long parseTimeMs) {
        TypeParserInfo info = parserInfoMap.get(typeCode);
        if (info != null) {
            info.recordParseSuccess(parseTimeMs);
            totalParseOperations++;
        }
    }
    
    /**
     * Records a parse error.
     * 
     * @param typeCode the type that failed to parse
     * @param errorMessage the error message
     */
    public void recordParseError(byte typeCode, String errorMessage) {
        TypeParserInfo info = parserInfoMap.get(typeCode);
        if (info != null) {
            info.recordParseError(errorMessage);
            totalParseOperations++;
            totalParseErrors++;
        }
    }
    
    /**
     * Gets all registered type codes.
     * 
     * @return sorted list of registered type codes
     */
    public List<Byte> getRegisteredTypes() {
        return parserInfoMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all parser information objects.
     * 
     * @return list of all parser info objects
     */
    public List<TypeParserInfo> getAllParserInfo() {
        return new ArrayList<>(parserInfoMap.values());
    }
    
    /**
     * Gets parser information sorted by type code.
     * 
     * @return sorted list of parser info objects
     */
    public List<TypeParserInfo> getAllParserInfoSorted() {
        return parserInfoMap.values().stream()
                .sorted(Comparator.comparing(TypeParserInfo::getTypeCode))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets parsers with recent errors.
     * 
     * @param withinMinutes the time window in minutes
     * @return list of parser info with recent errors
     */
    public List<TypeParserInfo> getParsersWithRecentErrors(int withinMinutes) {
        return parserInfoMap.values().stream()
                .filter(info -> info.hasRecentErrors(withinMinutes))
                .sorted(Comparator.comparing(TypeParserInfo::getLastErrorTime).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the most frequently used parsers.
     * 
     * @param limit the maximum number of parsers to return
     * @return list of most used parsers
     */
    public List<TypeParserInfo> getMostUsedParsers(int limit) {
        return parserInfoMap.values().stream()
                .sorted(Comparator.comparing(TypeParserInfo::getTotalOperations).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets parsers with the lowest success rates.
     * 
     * @param limit the maximum number of parsers to return
     * @return list of parsers with lowest success rates
     */
    public List<TypeParserInfo> getLowestSuccessRateParsers(int limit) {
        return parserInfoMap.values().stream()
                .filter(info -> info.getTotalOperations() > 0) // Only include used parsers
                .sorted(Comparator.comparing(TypeParserInfo::getSuccessRate))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Removes a parser from the registry.
     * 
     * @param typeCode the type code to remove
     * @return the removed parser info, or null if not found
     */
    public TypeParserInfo removeParser(byte typeCode) {
        return parserInfoMap.remove(typeCode);
    }
    
    /**
     * Clears all registered parsers.
     */
    public void clear() {
        parserInfoMap.clear();
        totalRegistrations = 0;
        totalParseOperations = 0;
        totalParseErrors = 0;
        lastRegistrationTime = null;
    }
    
    /**
     * Gets the number of registered parsers.
     * 
     * @return the number of registered parsers
     */
    public int size() {
        return parserInfoMap.size();
    }
    
    /**
     * Checks if the registry is empty.
     * 
     * @return true if no parsers are registered
     */
    public boolean isEmpty() {
        return parserInfoMap.isEmpty();
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
            totalParseOperations,
            totalParseErrors,
            lastRegistrationTime,
            parserInfoMap.size(),
            calculateOverallSuccessRate(),
            calculateAverageParseTime()
        );
    }
    
    /**
     * Calculates the overall success rate across all parsers.
     * 
     * @return overall success rate (0.0 to 1.0)
     */
    private double calculateOverallSuccessRate() {
        if (totalParseOperations == 0) {
            return 1.0;
        }
        return (double) (totalParseOperations - totalParseErrors) / totalParseOperations;
    }
    
    /**
     * Calculates the average parse time across all parsers.
     * 
     * @return average parse time in milliseconds
     */
    private double calculateAverageParseTime() {
        long totalTime = parserInfoMap.values().stream()
                .mapToLong(TypeParserInfo::getTotalParseTimeMs)
                .sum();
        
        long totalSuccessfulOperations = parserInfoMap.values().stream()
                .mapToLong(TypeParserInfo::getParseSuccessCount)
                .sum();
        
        return totalSuccessfulOperations > 0 ? (double) totalTime / totalSuccessfulOperations : 0.0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "TypeParserRegistry{parsers=%d, operations=%d, errors=%d, successRate=%.2f%%}",
            parserInfoMap.size(), totalParseOperations, totalParseErrors,
            calculateOverallSuccessRate() * 100
        );
    }
    
    /**
     * POJO for registry-level statistics.
     */
    public static class RegistryStatistics {
        private final LocalDateTime creationTime;
        private final long totalRegistrations;
        private final long totalParseOperations;
        private final long totalParseErrors;
        private final LocalDateTime lastRegistrationTime;
        private final int activeParserCount;
        private final double overallSuccessRate;
        private final double averageParseTimeMs;
        
        public RegistryStatistics(LocalDateTime creationTime, long totalRegistrations,
                                long totalParseOperations, long totalParseErrors,
                                LocalDateTime lastRegistrationTime, int activeParserCount,
                                double overallSuccessRate, double averageParseTimeMs) {
            this.creationTime = creationTime;
            this.totalRegistrations = totalRegistrations;
            this.totalParseOperations = totalParseOperations;
            this.totalParseErrors = totalParseErrors;
            this.lastRegistrationTime = lastRegistrationTime;
            this.activeParserCount = activeParserCount;
            this.overallSuccessRate = overallSuccessRate;
            this.averageParseTimeMs = averageParseTimeMs;
        }
        
        // Getters
        public LocalDateTime getCreationTime() { return creationTime; }
        public long getTotalRegistrations() { return totalRegistrations; }
        public long getTotalParseOperations() { return totalParseOperations; }
        public long getTotalParseErrors() { return totalParseErrors; }
        public LocalDateTime getLastRegistrationTime() { return lastRegistrationTime; }
        public int getActiveParserCount() { return activeParserCount; }
        public double getOverallSuccessRate() { return overallSuccessRate; }
        public double getAverageParseTimeMs() { return averageParseTimeMs; }
        
        @Override
        public String toString() {
            return String.format(
                "RegistryStatistics{parsers=%d, operations=%d, errors=%d, successRate=%.2f%%, avgTime=%.2fms}",
                activeParserCount, totalParseOperations, totalParseErrors,
                overallSuccessRate * 100, averageParseTimeMs
            );
        }
    }
}
