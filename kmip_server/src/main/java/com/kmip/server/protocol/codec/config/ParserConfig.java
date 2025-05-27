package com.kmip.server.protocol.codec.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kmip.server.config.KmipServerConfig;

/**
 * Configuration adapter for the KMIP Parser.
 *
 * This class adapts the unified KmipServerConfig to provide parser-specific
 * configuration. It acts as a bridge between the unified configuration
 * and the parser component.
 */
@Component
public class ParserConfig {

    @Autowired
    private KmipServerConfig serverConfig;

    // Adapter methods that delegate to the unified configuration

    public int getMaxMessageSizeBytes() {
        return serverConfig.getCodec().getParser().getMaxMessageSizeBytes();
    }

    public int getMaxNestingDepth() {
        return serverConfig.getCodec().getParser().getMaxNestingDepth();
    }

    public boolean isValidatePaddingBytes() {
        return serverConfig.getCodec().getParser().isValidatePaddingBytes();
    }

    public boolean isAllowUnknownTypes() {
        return serverConfig.getCodec().getParser().isAllowUnknownTypes();
    }

    public boolean isStrictTagValidation() {
        return serverConfig.getCodec().getParser().isStrictTagValidation();
    }

    public int getBufferSizeBytes() {
        return serverConfig.getTransport().getTcp().getBufferSizeBytes();
    }

    public boolean isEnableDetailedLogging() {
        return serverConfig.getCodec().getParser().isEnableDetailedLogging();
    }

    public boolean isEnableStructureCaching() {
        // This setting is not in the unified config yet, return default
        return true;
    }

    public int getMaxFieldsPerStructure() {
        return serverConfig.getCodec().getParser().getMaxFieldsPerStructure();
    }

    public long getParseTimeoutMs() {
        return serverConfig.getCodec().getParser().getParseTimeoutMs();
    }

    @Override
    public String toString() {
        return "ParserConfig{" +
                "maxMessageSizeBytes=" + getMaxMessageSizeBytes() +
                ", maxNestingDepth=" + getMaxNestingDepth() +
                ", validatePaddingBytes=" + isValidatePaddingBytes() +
                ", allowUnknownTypes=" + isAllowUnknownTypes() +
                ", strictTagValidation=" + isStrictTagValidation() +
                ", bufferSizeBytes=" + getBufferSizeBytes() +
                ", enableDetailedLogging=" + isEnableDetailedLogging() +
                ", enableStructureCaching=" + isEnableStructureCaching() +
                ", maxFieldsPerStructure=" + getMaxFieldsPerStructure() +
                ", parseTimeoutMs=" + getParseTimeoutMs() +
                '}';
    }
}
