package com.kmip.server.protocol.codec.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kmip.server.protocol.codec.parser.model.TypeParserInfo;

/**
 * Spring-managed registry for KMIP type parsers - RESTRUCTURED with POJOs.
 *
 * This class manages all available type parsers using a POJO-based approach
 * instead of raw Maps. This provides better type safety, easier debugging,
 * and centralized information management.
 *
 * ARCHITECTURAL IMPROVEMENTS:
 * - Replaced Maps with structured POJOs
 * - Better exception handling and validation
 * - Centralized statistics in TypeParserInfo
 * - Easier debugging with structured data
 * - Type-safe operations
 */
@Component
public class TypeParserRegistry {

    private static final Logger log = LoggerFactory.getLogger(TypeParserRegistry.class);

    // POJO-based registry instead of raw maps
    private final com.kmip.server.protocol.codec.parser.model.TypeParserRegistry registry;

    @Autowired(required = false)
    private List<KmipTypeParser> typeParsers;

    /**
     * Constructor initializes the POJO-based registry.
     */
    public TypeParserRegistry() {
        this.registry = new com.kmip.server.protocol.codec.parser.model.TypeParserRegistry();
    }

    /**
     * Initializes the registry by discovering and registering all type parsers.
     */
    @PostConstruct
    public void initialize() {
        log.info("Initializing KMIP Type Parser Registry...");

        if (typeParsers != null) {
            for (KmipTypeParser parser : typeParsers) {
                registerParser(parser);
            }
        }

        // Register default parsers if none were found
        if (registry.isEmpty()) {
            log.warn("No type parsers found in Spring context, registering default parsers");
            registerDefaultParsers();
        }

        log.info("Type Parser Registry initialized with {} parsers: {}",
                registry.size(), getRegisteredTypeNames());
    }

    /**
     * Registers a type parser.
     *
     * @param parser the parser to register
     */
    public void registerParser(KmipTypeParser parser) {
        if (parser == null) {
            log.warn("Attempted to register null parser");
            return;
        }

        byte typeCode = parser.getTypeCode();
        String typeName = parser.getTypeName();

        // Check if parser already exists
        if (registry.hasParser(typeCode)) {
            TypeParserInfo existing = registry.getParserInfo(typeCode);
            log.warn("Replacing existing parser for type 0x{}: {} -> {}",
                    String.format("%02X", typeCode),
                    existing.getParserClassName(),
                    parser.getClass().getSimpleName());

            // Remove existing parser
            registry.removeParser(typeCode);
        }

        // Register new parser using POJO approach
        try {
            TypeParserInfo parserInfo = registry.registerParser(parser);
            log.debug("Registered parser for type 0x{} ({}): {}",
                    String.format("%02X", typeCode), typeName, parser.getClass().getSimpleName());
        } catch (IllegalArgumentException e) {
            log.error("Failed to register parser for type 0x{}: {}",
                    String.format("%02X", typeCode), e.getMessage());
        }
    }

    /**
     * Gets a parser for the specified type code.
     *
     * @param typeCode the KMIP type code
     * @return the parser, or null if not found
     */
    public KmipTypeParser getParser(byte typeCode) {
        return registry.getParser(typeCode);
    }

    /**
     * Checks if a parser is available for the specified type code.
     *
     * @param typeCode the KMIP type code
     * @return true if a parser is available
     */
    public boolean hasParser(byte typeCode) {
        return registry.hasParser(typeCode);
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
        return registry.getAllParserInfoSorted().stream()
                .map(TypeParserInfo::getTypeName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("none");
    }

    /**
     * Records a successful parse operation.
     *
     * @param typeCode the type that was parsed
     */
    public void recordParseSuccess(byte typeCode) {
        registry.recordParseSuccess(typeCode, 0); // Default parse time
    }

    /**
     * Records a parse error.
     *
     * @param typeCode the type that failed to parse
     */
    public void recordParseError(byte typeCode) {
        registry.recordParseError(typeCode, "Parse error occurred");
    }

    /**
     * Gets parsing statistics using the POJO-based approach.
     *
     * @return map of statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Get overall registry statistics
        com.kmip.server.protocol.codec.parser.model.TypeParserRegistry.RegistryStatistics regStats =
                registry.getStatistics();

        stats.put("registeredTypes", regStats.getActiveParserCount());
        stats.put("totalParseOperations", regStats.getTotalParseOperations());
        stats.put("totalParseErrors", regStats.getTotalParseErrors());
        stats.put("overallSuccessRate", regStats.getOverallSuccessRate());
        stats.put("averageParseTimeMs", regStats.getAverageParseTimeMs());
        stats.put("creationTime", regStats.getCreationTime());
        stats.put("lastRegistrationTime", regStats.getLastRegistrationTime());

        // Get individual parser statistics
        Map<String, Object> typeStats = new HashMap<>();
        for (TypeParserInfo info : registry.getAllParserInfoSorted()) {
            String typeName = info.getTypeName();
            typeStats.put(typeName + "_successCount", info.getParseSuccessCount());
            typeStats.put(typeName + "_errorCount", info.getParseErrorCount());
            typeStats.put(typeName + "_successRate", info.getSuccessRate());
            typeStats.put(typeName + "_avgParseTime", info.getAverageParseTimeMs());
            typeStats.put(typeName + "_lastUsed", info.getLastUsedTime());
        }
        stats.put("typeStatistics", typeStats);

        return stats;
    }

    /**
     * Registers default parsers for basic KMIP types.
     * This is a fallback when no parsers are found in the Spring context.
     */
    private void registerDefaultParsers() {
        // Note: These would be actual implementations
        // For now, we'll create placeholder registrations
        log.info("Default parsers would be registered here");

        // Example of what would be registered:
        // registerParser(new StructureTypeParser());
        // registerParser(new IntegerTypeParser());
        // registerParser(new EnumerationTypeParser());
        // registerParser(new BooleanTypeParser());
        // registerParser(new TextStringTypeParser());
        // registerParser(new ByteStringTypeParser());
        // registerParser(new DateTimeTypeParser());
    }

    /**
     * Clears all registered parsers.
     * Mainly used for testing.
     */
    public void clear() {
        registry.clear();
        log.debug("Type parser registry cleared");
    }

    /**
     * Gets the number of registered parsers.
     *
     * @return the number of registered parsers
     */
    public int size() {
        return registry.size();
    }

    /**
     * Gets detailed information about a specific parser.
     *
     * @param typeCode the KMIP type code
     * @return detailed parser information, or null if not found
     */
    public TypeParserInfo getParserInfo(byte typeCode) {
        return registry.getParserInfo(typeCode);
    }

    /**
     * Gets parsers with recent errors for debugging.
     *
     * @param withinMinutes the time window in minutes
     * @return list of parsers with recent errors
     */
    public List<TypeParserInfo> getParsersWithRecentErrors(int withinMinutes) {
        return registry.getParsersWithRecentErrors(withinMinutes);
    }

    /**
     * Gets the most frequently used parsers.
     *
     * @param limit the maximum number of parsers to return
     * @return list of most used parsers
     */
    public List<TypeParserInfo> getMostUsedParsers(int limit) {
        return registry.getMostUsedParsers(limit);
    }
}
