package com.kmip.server.protocol.codec.encoder.model;

import com.kmip.server.protocol.codec.encoder.KmipTypeEncoder;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * POJO representing information about a registered KMIP type encoder.
 * 
 * This class encapsulates all information related to a specific type encoder,
 * including the encoder instance, statistics, and metadata. This approach
 * provides better type safety, easier debugging, and centralized information
 * compared to using multiple maps.
 */
public class TypeEncoderInfo {
    
    private final byte typeCode;
    private final String typeName;
    private final KmipTypeEncoder encoder;
    private final String encoderClassName;
    private final LocalDateTime registrationTime;
    
    // Statistics - using AtomicLong for thread safety
    private final AtomicLong encodeSuccessCount = new AtomicLong(0);
    private final AtomicLong encodeErrorCount = new AtomicLong(0);
    private final AtomicLong totalEncodeTimeMs = new AtomicLong(0);
    private final AtomicLong totalEncodedBytes = new AtomicLong(0);
    private volatile LocalDateTime lastUsedTime;
    private volatile String lastErrorMessage;
    private volatile LocalDateTime lastErrorTime;
    
    // Encoder capabilities
    private final boolean allowsNullValue;
    private final int requiredAlignment;
    
    /**
     * Creates a new TypeEncoderInfo instance.
     * 
     * @param encoder the type encoder instance
     */
    public TypeEncoderInfo(KmipTypeEncoder encoder) {
        if (encoder == null) {
            throw new IllegalArgumentException("Encoder cannot be null");
        }
        
        this.encoder = encoder;
        this.typeCode = encoder.getTypeCode();
        this.typeName = encoder.getTypeName();
        this.encoderClassName = encoder.getClass().getSimpleName();
        this.registrationTime = LocalDateTime.now();
        
        // Cache encoder capabilities for performance
        this.allowsNullValue = encoder.allowsNullValue();
        this.requiredAlignment = encoder.getRequiredAlignment();
    }
    
    /**
     * Records a successful encode operation.
     * 
     * @param encodeTimeMs the time taken to encode in milliseconds
     * @param encodedBytes the number of bytes encoded
     */
    public void recordEncodeSuccess(long encodeTimeMs, int encodedBytes) {
        encodeSuccessCount.incrementAndGet();
        totalEncodeTimeMs.addAndGet(encodeTimeMs);
        totalEncodedBytes.addAndGet(encodedBytes);
        lastUsedTime = LocalDateTime.now();
    }
    
    /**
     * Records an encode error.
     * 
     * @param errorMessage the error message
     */
    public void recordEncodeError(String errorMessage) {
        encodeErrorCount.incrementAndGet();
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
        return total > 0 ? (double) encodeSuccessCount.get() / total : 1.0;
    }
    
    /**
     * Gets the average encode time in milliseconds.
     * 
     * @return average encode time
     */
    public double getAverageEncodeTimeMs() {
        long successCount = encodeSuccessCount.get();
        return successCount > 0 ? (double) totalEncodeTimeMs.get() / successCount : 0.0;
    }
    
    /**
     * Gets the average bytes encoded per operation.
     * 
     * @return average bytes per operation
     */
    public double getAverageBytesPerOperation() {
        long successCount = encodeSuccessCount.get();
        return successCount > 0 ? (double) totalEncodedBytes.get() / successCount : 0.0;
    }
    
    /**
     * Gets the total number of operations (success + error).
     * 
     * @return total operations
     */
    public long getTotalOperations() {
        return encodeSuccessCount.get() + encodeErrorCount.get();
    }
    
    /**
     * Gets the encoding throughput in bytes per second.
     * 
     * @return throughput in bytes/second
     */
    public double getThroughputBytesPerSecond() {
        long totalTimeMs = totalEncodeTimeMs.get();
        if (totalTimeMs > 0) {
            return (double) totalEncodedBytes.get() / (totalTimeMs / 1000.0);
        }
        return 0.0;
    }
    
    /**
     * Checks if this encoder has been used recently.
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
     * Checks if this encoder has errors recently.
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
    
    public KmipTypeEncoder getEncoder() {
        return encoder;
    }
    
    public String getEncoderClassName() {
        return encoderClassName;
    }
    
    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }
    
    public long getEncodeSuccessCount() {
        return encodeSuccessCount.get();
    }
    
    public long getEncodeErrorCount() {
        return encodeErrorCount.get();
    }
    
    public long getTotalEncodeTimeMs() {
        return totalEncodeTimeMs.get();
    }
    
    public long getTotalEncodedBytes() {
        return totalEncodedBytes.get();
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
    
    public boolean isAllowsNullValue() {
        return allowsNullValue;
    }
    
    public int getRequiredAlignment() {
        return requiredAlignment;
    }
    
    @Override
    public String toString() {
        return String.format(
            "TypeEncoderInfo{typeCode=0x%02X, typeName='%s', encoder='%s', " +
            "successCount=%d, errorCount=%d, successRate=%.2f%%, avgEncodeTime=%.2fms, " +
            "throughput=%.2f bytes/sec}",
            typeCode, typeName, encoderClassName,
            encodeSuccessCount.get(), encodeErrorCount.get(),
            getSuccessRate() * 100, getAverageEncodeTimeMs(),
            getThroughputBytesPerSecond()
        );
    }
    
    /**
     * Creates a detailed status report for this encoder.
     * 
     * @return detailed status information
     */
    public String getDetailedStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Type Encoder Status ===\n");
        sb.append(String.format("Type: 0x%02X (%s)\n", typeCode, typeName));
        sb.append(String.format("Encoder: %s\n", encoderClassName));
        sb.append(String.format("Registered: %s\n", registrationTime));
        sb.append(String.format("Total Operations: %d\n", getTotalOperations()));
        sb.append(String.format("Success Count: %d\n", encodeSuccessCount.get()));
        sb.append(String.format("Error Count: %d\n", encodeErrorCount.get()));
        sb.append(String.format("Success Rate: %.2f%%\n", getSuccessRate() * 100));
        sb.append(String.format("Average Encode Time: %.2f ms\n", getAverageEncodeTimeMs()));
        sb.append(String.format("Average Bytes/Operation: %.2f\n", getAverageBytesPerOperation()));
        sb.append(String.format("Throughput: %.2f bytes/sec\n", getThroughputBytesPerSecond()));
        sb.append(String.format("Total Encoded: %d bytes\n", totalEncodedBytes.get()));
        sb.append(String.format("Last Used: %s\n", lastUsedTime != null ? lastUsedTime : "Never"));
        
        if (lastErrorMessage != null) {
            sb.append(String.format("Last Error: %s (%s)\n", lastErrorMessage, lastErrorTime));
        }
        
        sb.append(String.format("Capabilities: NullValue=%s, Alignment=%d\n",
                allowsNullValue, requiredAlignment));
        
        return sb.toString();
    }
}
