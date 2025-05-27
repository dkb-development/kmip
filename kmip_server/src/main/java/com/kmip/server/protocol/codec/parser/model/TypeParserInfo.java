package com.kmip.server.protocol.codec.parser.model;

import com.kmip.server.protocol.codec.parser.KmipTypeParser;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * POJO representing information about a registered KMIP type parser.
 * 
 * This class encapsulates all information related to a specific type parser,
 * including the parser instance, statistics, and metadata. This approach
 * provides better type safety, easier debugging, and centralized information
 * compared to using multiple maps.
 */
public class TypeParserInfo {
    
    private final byte typeCode;
    private final String typeName;
    private final KmipTypeParser parser;
    private final String parserClassName;
    private final LocalDateTime registrationTime;
    
    // Statistics - using AtomicLong for thread safety
    private final AtomicLong parseSuccessCount = new AtomicLong(0);
    private final AtomicLong parseErrorCount = new AtomicLong(0);
    private final AtomicLong totalParseTimeMs = new AtomicLong(0);
    private volatile LocalDateTime lastUsedTime;
    private volatile String lastErrorMessage;
    private volatile LocalDateTime lastErrorTime;
    
    // Parser capabilities
    private final boolean allowsEmptyValue;
    private final int minimumLength;
    private final int maximumLength;
    private final int requiredAlignment;
    
    /**
     * Creates a new TypeParserInfo instance.
     * 
     * @param parser the type parser instance
     */
    public TypeParserInfo(KmipTypeParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("Parser cannot be null");
        }
        
        this.parser = parser;
        this.typeCode = parser.getTypeCode();
        this.typeName = parser.getTypeName();
        this.parserClassName = parser.getClass().getSimpleName();
        this.registrationTime = LocalDateTime.now();
        
        // Cache parser capabilities for performance
        this.allowsEmptyValue = parser.allowsEmptyValue();
        this.minimumLength = parser.getMinimumLength();
        this.maximumLength = parser.getMaximumLength();
        this.requiredAlignment = parser.getRequiredAlignment();
    }
    
    /**
     * Records a successful parse operation.
     * 
     * @param parseTimeMs the time taken to parse in milliseconds
     */
    public void recordParseSuccess(long parseTimeMs) {
        parseSuccessCount.incrementAndGet();
        totalParseTimeMs.addAndGet(parseTimeMs);
        lastUsedTime = LocalDateTime.now();
    }
    
    /**
     * Records a parse error.
     * 
     * @param errorMessage the error message
     */
    public void recordParseError(String errorMessage) {
        parseErrorCount.incrementAndGet();
        lastErrorMessage = errorMessage;
        lastErrorTime = LocalDateTime.now();
        lastUsedTime = LocalDateTime.now();
    }
    
    /**
     * Gets the success rate as a percentage.
     * 
     * @return success rate (0.0 to 1.0)
     */
    public double getSuccessRate() {
        long total = getTotalOperations();
        return total > 0 ? (double) parseSuccessCount.get() / total : 1.0;
    }
    
    /**
     * Gets the average parse time in milliseconds.
     * 
     * @return average parse time
     */
    public double getAverageParseTimeMs() {
        long successCount = parseSuccessCount.get();
        return successCount > 0 ? (double) totalParseTimeMs.get() / successCount : 0.0;
    }
    
    /**
     * Gets the total number of operations (success + error).
     * 
     * @return total operations
     */
    public long getTotalOperations() {
        return parseSuccessCount.get() + parseErrorCount.get();
    }
    
    /**
     * Checks if this parser has been used recently.
     * 
     * @param withinMinutes the time window in minutes
     * @return true if used within the specified time
     */
    public boolean isUsedRecently(int withinMinutes) {
        if (lastUsedTime == null) {
            return false;
        }
        return lastUsedTime.isAfter(LocalDateTime.now().minusMinutes(withinMinutes));
    }
    
    /**
     * Checks if this parser has errors recently.
     * 
     * @param withinMinutes the time window in minutes
     * @return true if errors occurred within the specified time
     */
    public boolean hasRecentErrors(int withinMinutes) {
        if (lastErrorTime == null) {
            return false;
        }
        return lastErrorTime.isAfter(LocalDateTime.now().minusMinutes(withinMinutes));
    }
    
    // Getters
    
    public byte getTypeCode() {
        return typeCode;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    public KmipTypeParser getParser() {
        return parser;
    }
    
    public String getParserClassName() {
        return parserClassName;
    }
    
    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }
    
    public long getParseSuccessCount() {
        return parseSuccessCount.get();
    }
    
    public long getParseErrorCount() {
        return parseErrorCount.get();
    }
    
    public long getTotalParseTimeMs() {
        return totalParseTimeMs.get();
    }
    
    public LocalDateTime getLastUsedTime() {
        return lastUsedTime;
    }
    
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
    
    public LocalDateTime getLastErrorTime() {
        return lastErrorTime;
    }
    
    public boolean isAllowsEmptyValue() {
        return allowsEmptyValue;
    }
    
    public int getMinimumLength() {
        return minimumLength;
    }
    
    public int getMaximumLength() {
        return maximumLength;
    }
    
    public int getRequiredAlignment() {
        return requiredAlignment;
    }
    
    @Override
    public String toString() {
        return String.format(
            "TypeParserInfo{typeCode=0x%02X, typeName='%s', parser='%s', " +
            "successCount=%d, errorCount=%d, successRate=%.2f%%, avgParseTime=%.2fms}",
            typeCode, typeName, parserClassName,
            parseSuccessCount.get(), parseErrorCount.get(),
            getSuccessRate() * 100, getAverageParseTimeMs()
        );
    }
    
    /**
     * Creates a detailed status report for this parser.
     * 
     * @return detailed status information
     */
    public String getDetailedStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Type Parser Status ===\n");
        sb.append(String.format("Type: 0x%02X (%s)\n", typeCode, typeName));
        sb.append(String.format("Parser: %s\n", parserClassName));
        sb.append(String.format("Registered: %s\n", registrationTime));
        sb.append(String.format("Total Operations: %d\n", getTotalOperations()));
        sb.append(String.format("Success Count: %d\n", parseSuccessCount.get()));
        sb.append(String.format("Error Count: %d\n", parseErrorCount.get()));
        sb.append(String.format("Success Rate: %.2f%%\n", getSuccessRate() * 100));
        sb.append(String.format("Average Parse Time: %.2f ms\n", getAverageParseTimeMs()));
        sb.append(String.format("Last Used: %s\n", lastUsedTime != null ? lastUsedTime : "Never"));
        
        if (lastErrorMessage != null) {
            sb.append(String.format("Last Error: %s (%s)\n", lastErrorMessage, lastErrorTime));
        }
        
        sb.append(String.format("Capabilities: EmptyValue=%s, MinLen=%d, MaxLen=%d, Alignment=%d\n",
                allowsEmptyValue, minimumLength, maximumLength, requiredAlignment));
        
        return sb.toString();
    }
}
