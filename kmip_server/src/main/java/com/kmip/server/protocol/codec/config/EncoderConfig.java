package com.kmip.server.protocol.codec.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kmip.server.config.KmipServerConfig;

/**
 * Configuration adapter for the KMIP Encoder.
 *
 * This class adapts the unified KmipServerConfig to provide encoder-specific
 * configuration. It acts as a bridge between the unified configuration
 * and the encoder component.
 */
@Component
public class EncoderConfig {

    @Autowired
    private KmipServerConfig serverConfig;

    // Adapter methods that delegate to the unified configuration

    public int getMaxMessageSizeBytes() {
        return serverConfig.getCodec().getEncoder().getMaxMessageSizeBytes();
    }

    public int getMaxNestingDepth() {
        return serverConfig.getCodec().getEncoder().getMaxNestingDepth();
    }

    public boolean isValidateFieldOrdering() {
        return serverConfig.getCodec().getEncoder().isValidateFieldOrdering();
    }

    public boolean isAllowUnknownTypes() {
        return serverConfig.getCodec().getEncoder().isAllowUnknownTypes();
    }

    public boolean isStrictTagValidation() {
        return serverConfig.getCodec().getEncoder().isStrictTagValidation();
    }

    public int getBufferSizeBytes() {
        return serverConfig.getTransport().getTcp().getBufferSizeBytes();
    }

    public boolean isEnableDetailedLogging() {
        return serverConfig.getCodec().getEncoder().isEnableDetailedLogging();
    }

    public boolean isEnableHexDumpLogging() {
        return serverConfig.getCodec().getEncoder().isEnableHexDumpLogging();
    }

    public int getMaxFieldsPerStructure() {
        return serverConfig.getCodec().getEncoder().getMaxFieldsPerStructure();
    }

    public long getEncodeTimeoutMs() {
        return serverConfig.getCodec().getEncoder().getEncodeTimeoutMs();
    }

    public boolean isEnableOptimizedEncoding() {
        // This setting is not in the unified config yet, return default
        return true;
    }

    public boolean isValidatePaddingBytes() {
        // This setting is not in the unified config yet, return default
        return true;
    }

    public boolean isEnableStructureCaching() {
        // This setting is not in the unified config yet, return default
        return true;
    }

    @Override
    public String toString() {
        return "EncoderConfig{" +
                "maxMessageSizeBytes=" + getMaxMessageSizeBytes() +
                ", maxNestingDepth=" + getMaxNestingDepth() +
                ", validateFieldOrdering=" + isValidateFieldOrdering() +
                ", allowUnknownTypes=" + isAllowUnknownTypes() +
                ", strictTagValidation=" + isStrictTagValidation() +
                ", bufferSizeBytes=" + getBufferSizeBytes() +
                ", enableDetailedLogging=" + isEnableDetailedLogging() +
                ", enableHexDumpLogging=" + isEnableHexDumpLogging() +
                ", maxFieldsPerStructure=" + getMaxFieldsPerStructure() +
                ", encodeTimeoutMs=" + getEncodeTimeoutMs() +
                ", enableOptimizedEncoding=" + isEnableOptimizedEncoding() +
                ", validatePaddingBytes=" + isValidatePaddingBytes() +
                ", enableStructureCaching=" + isEnableStructureCaching() +
                '}';
    }
}
