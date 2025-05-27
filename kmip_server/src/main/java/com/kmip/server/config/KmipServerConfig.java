package com.kmip.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Unified KMIP Server Configuration.
 * 
 * This class consolidates all configuration properties for the KMIP server,
 * providing a single point of configuration management. It includes settings
 * for all components: server, transport, protocol, codec, etc.
 * 
 * Configuration is organized into logical sections for better maintainability.
 */
@Configuration
@ConfigurationProperties(prefix = "kmip.server")
public class KmipServerConfig {
    
    // Server-level configuration
    private ServerSettings server = new ServerSettings();
    
    // Transport layer configuration
    private TransportSettings transport = new TransportSettings();
    
    // Protocol layer configuration
    private ProtocolSettings protocol = new ProtocolSettings();
    
    // Codec layer configuration
    private CodecSettings codec = new CodecSettings();
    
    // Security configuration
    private SecuritySettings security = new SecuritySettings();
    
    // Management and monitoring configuration
    private ManagementSettings management = new ManagementSettings();
    
    /**
     * Server-level settings.
     */
    public static class ServerSettings {
        private String name = "KMIP Server";
        private String version = "1.0.0";
        private boolean enableMetrics = true;
        private boolean enableHealthCheck = true;
        private int shutdownTimeoutSeconds = 30;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public boolean isEnableMetrics() { return enableMetrics; }
        public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
        
        public boolean isEnableHealthCheck() { return enableHealthCheck; }
        public void setEnableHealthCheck(boolean enableHealthCheck) { this.enableHealthCheck = enableHealthCheck; }
        
        public int getShutdownTimeoutSeconds() { return shutdownTimeoutSeconds; }
        public void setShutdownTimeoutSeconds(int shutdownTimeoutSeconds) { this.shutdownTimeoutSeconds = shutdownTimeoutSeconds; }
    }
    
    /**
     * Transport layer settings.
     */
    public static class TransportSettings {
        private TcpSettings tcp = new TcpSettings();
        
        public static class TcpSettings {
            private int port = 5696;
            private String bindAddress = "0.0.0.0";
            private int maxConnections = 100;
            private int connectionTimeoutMs = 30000;
            private int readTimeoutMs = 60000;
            private int writeTimeoutMs = 30000;
            private int bufferSizeBytes = 8192;
            private boolean enableKeepAlive = true;
            private boolean enableTcpNoDelay = true;
            
            // Getters and setters
            public int getPort() { return port; }
            public void setPort(int port) { this.port = port; }
            
            public String getBindAddress() { return bindAddress; }
            public void setBindAddress(String bindAddress) { this.bindAddress = bindAddress; }
            
            public int getMaxConnections() { return maxConnections; }
            public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
            
            public int getConnectionTimeoutMs() { return connectionTimeoutMs; }
            public void setConnectionTimeoutMs(int connectionTimeoutMs) { this.connectionTimeoutMs = connectionTimeoutMs; }
            
            public int getReadTimeoutMs() { return readTimeoutMs; }
            public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
            
            public int getWriteTimeoutMs() { return writeTimeoutMs; }
            public void setWriteTimeoutMs(int writeTimeoutMs) { this.writeTimeoutMs = writeTimeoutMs; }
            
            public int getBufferSizeBytes() { return bufferSizeBytes; }
            public void setBufferSizeBytes(int bufferSizeBytes) { this.bufferSizeBytes = bufferSizeBytes; }
            
            public boolean isEnableKeepAlive() { return enableKeepAlive; }
            public void setEnableKeepAlive(boolean enableKeepAlive) { this.enableKeepAlive = enableKeepAlive; }
            
            public boolean isEnableTcpNoDelay() { return enableTcpNoDelay; }
            public void setEnableTcpNoDelay(boolean enableTcpNoDelay) { this.enableTcpNoDelay = enableTcpNoDelay; }
        }
        
        public TcpSettings getTcp() { return tcp; }
        public void setTcp(TcpSettings tcp) { this.tcp = tcp; }
    }
    
    /**
     * Protocol layer settings.
     */
    public static class ProtocolSettings {
        private String version = "2.0";
        private int maxBatchItems = 100;
        private boolean strictCompliance = true;
        private boolean enableExtensions = false;
        private int operationTimeoutMs = 300000; // 5 minutes
        
        // Getters and setters
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public int getMaxBatchItems() { return maxBatchItems; }
        public void setMaxBatchItems(int maxBatchItems) { this.maxBatchItems = maxBatchItems; }
        
        public boolean isStrictCompliance() { return strictCompliance; }
        public void setStrictCompliance(boolean strictCompliance) { this.strictCompliance = strictCompliance; }
        
        public boolean isEnableExtensions() { return enableExtensions; }
        public void setEnableExtensions(boolean enableExtensions) { this.enableExtensions = enableExtensions; }
        
        public int getOperationTimeoutMs() { return operationTimeoutMs; }
        public void setOperationTimeoutMs(int operationTimeoutMs) { this.operationTimeoutMs = operationTimeoutMs; }
    }
    
    /**
     * Codec layer settings.
     */
    public static class CodecSettings {
        private ParserSettings parser = new ParserSettings();
        private EncoderSettings encoder = new EncoderSettings();
        
        public static class ParserSettings {
            private int maxMessageSizeBytes = 1024 * 1024; // 1MB
            private int maxNestingDepth = 10;
            private boolean validatePaddingBytes = true;
            private boolean allowUnknownTypes = true;
            private boolean strictTagValidation = false;
            private int maxFieldsPerStructure = 1000;
            private long parseTimeoutMs = 30000;
            private boolean enableDetailedLogging = false;
            
            // Getters and setters
            public int getMaxMessageSizeBytes() { return maxMessageSizeBytes; }
            public void setMaxMessageSizeBytes(int maxMessageSizeBytes) { this.maxMessageSizeBytes = maxMessageSizeBytes; }
            
            public int getMaxNestingDepth() { return maxNestingDepth; }
            public void setMaxNestingDepth(int maxNestingDepth) { this.maxNestingDepth = maxNestingDepth; }
            
            public boolean isValidatePaddingBytes() { return validatePaddingBytes; }
            public void setValidatePaddingBytes(boolean validatePaddingBytes) { this.validatePaddingBytes = validatePaddingBytes; }
            
            public boolean isAllowUnknownTypes() { return allowUnknownTypes; }
            public void setAllowUnknownTypes(boolean allowUnknownTypes) { this.allowUnknownTypes = allowUnknownTypes; }
            
            public boolean isStrictTagValidation() { return strictTagValidation; }
            public void setStrictTagValidation(boolean strictTagValidation) { this.strictTagValidation = strictTagValidation; }
            
            public int getMaxFieldsPerStructure() { return maxFieldsPerStructure; }
            public void setMaxFieldsPerStructure(int maxFieldsPerStructure) { this.maxFieldsPerStructure = maxFieldsPerStructure; }
            
            public long getParseTimeoutMs() { return parseTimeoutMs; }
            public void setParseTimeoutMs(long parseTimeoutMs) { this.parseTimeoutMs = parseTimeoutMs; }
            
            public boolean isEnableDetailedLogging() { return enableDetailedLogging; }
            public void setEnableDetailedLogging(boolean enableDetailedLogging) { this.enableDetailedLogging = enableDetailedLogging; }
        }
        
        public static class EncoderSettings {
            private int maxMessageSizeBytes = 1024 * 1024; // 1MB
            private int maxNestingDepth = 10;
            private boolean validateFieldOrdering = true;
            private boolean allowUnknownTypes = false;
            private boolean strictTagValidation = true;
            private int maxFieldsPerStructure = 1000;
            private long encodeTimeoutMs = 30000;
            private boolean enableDetailedLogging = false;
            private boolean enableHexDumpLogging = false;
            
            // Getters and setters
            public int getMaxMessageSizeBytes() { return maxMessageSizeBytes; }
            public void setMaxMessageSizeBytes(int maxMessageSizeBytes) { this.maxMessageSizeBytes = maxMessageSizeBytes; }
            
            public int getMaxNestingDepth() { return maxNestingDepth; }
            public void setMaxNestingDepth(int maxNestingDepth) { this.maxNestingDepth = maxNestingDepth; }
            
            public boolean isValidateFieldOrdering() { return validateFieldOrdering; }
            public void setValidateFieldOrdering(boolean validateFieldOrdering) { this.validateFieldOrdering = validateFieldOrdering; }
            
            public boolean isAllowUnknownTypes() { return allowUnknownTypes; }
            public void setAllowUnknownTypes(boolean allowUnknownTypes) { this.allowUnknownTypes = allowUnknownTypes; }
            
            public boolean isStrictTagValidation() { return strictTagValidation; }
            public void setStrictTagValidation(boolean strictTagValidation) { this.strictTagValidation = strictTagValidation; }
            
            public int getMaxFieldsPerStructure() { return maxFieldsPerStructure; }
            public void setMaxFieldsPerStructure(int maxFieldsPerStructure) { this.maxFieldsPerStructure = maxFieldsPerStructure; }
            
            public long getEncodeTimeoutMs() { return encodeTimeoutMs; }
            public void setEncodeTimeoutMs(long encodeTimeoutMs) { this.encodeTimeoutMs = encodeTimeoutMs; }
            
            public boolean isEnableDetailedLogging() { return enableDetailedLogging; }
            public void setEnableDetailedLogging(boolean enableDetailedLogging) { this.enableDetailedLogging = enableDetailedLogging; }
            
            public boolean isEnableHexDumpLogging() { return enableHexDumpLogging; }
            public void setEnableHexDumpLogging(boolean enableHexDumpLogging) { this.enableHexDumpLogging = enableHexDumpLogging; }
        }
        
        public ParserSettings getParser() { return parser; }
        public void setParser(ParserSettings parser) { this.parser = parser; }
        
        public EncoderSettings getEncoder() { return encoder; }
        public void setEncoder(EncoderSettings encoder) { this.encoder = encoder; }
    }
    
    /**
     * Security settings.
     */
    public static class SecuritySettings {
        private boolean enableSsl = false;
        private String keystorePath = "";
        private String keystorePassword = "";
        private String truststorePath = "";
        private String truststorePassword = "";
        private boolean requireClientAuth = false;
        
        // Getters and setters
        public boolean isEnableSsl() { return enableSsl; }
        public void setEnableSsl(boolean enableSsl) { this.enableSsl = enableSsl; }
        
        public String getKeystorePath() { return keystorePath; }
        public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }
        
        public String getKeystorePassword() { return keystorePassword; }
        public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }
        
        public String getTruststorePath() { return truststorePath; }
        public void setTruststorePath(String truststorePath) { this.truststorePath = truststorePath; }
        
        public String getTruststorePassword() { return truststorePassword; }
        public void setTruststorePassword(String truststorePassword) { this.truststorePassword = truststorePassword; }
        
        public boolean isRequireClientAuth() { return requireClientAuth; }
        public void setRequireClientAuth(boolean requireClientAuth) { this.requireClientAuth = requireClientAuth; }
    }
    
    /**
     * Management and monitoring settings.
     */
    public static class ManagementSettings {
        private boolean enableJmx = true;
        private boolean enableStatistics = true;
        private int statisticsIntervalSeconds = 60;
        private boolean enableHealthEndpoint = true;
        private boolean enableMetricsEndpoint = true;
        
        // Getters and setters
        public boolean isEnableJmx() { return enableJmx; }
        public void setEnableJmx(boolean enableJmx) { this.enableJmx = enableJmx; }
        
        public boolean isEnableStatistics() { return enableStatistics; }
        public void setEnableStatistics(boolean enableStatistics) { this.enableStatistics = enableStatistics; }
        
        public int getStatisticsIntervalSeconds() { return statisticsIntervalSeconds; }
        public void setStatisticsIntervalSeconds(int statisticsIntervalSeconds) { this.statisticsIntervalSeconds = statisticsIntervalSeconds; }
        
        public boolean isEnableHealthEndpoint() { return enableHealthEndpoint; }
        public void setEnableHealthEndpoint(boolean enableHealthEndpoint) { this.enableHealthEndpoint = enableHealthEndpoint; }
        
        public boolean isEnableMetricsEndpoint() { return enableMetricsEndpoint; }
        public void setEnableMetricsEndpoint(boolean enableMetricsEndpoint) { this.enableMetricsEndpoint = enableMetricsEndpoint; }
    }
    
    // Main getters and setters
    public ServerSettings getServer() { return server; }
    public void setServer(ServerSettings server) { this.server = server; }
    
    public TransportSettings getTransport() { return transport; }
    public void setTransport(TransportSettings transport) { this.transport = transport; }
    
    public ProtocolSettings getProtocol() { return protocol; }
    public void setProtocol(ProtocolSettings protocol) { this.protocol = protocol; }
    
    public CodecSettings getCodec() { return codec; }
    public void setCodec(CodecSettings codec) { this.codec = codec; }
    
    public SecuritySettings getSecurity() { return security; }
    public void setSecurity(SecuritySettings security) { this.security = security; }
    
    public ManagementSettings getManagement() { return management; }
    public void setManagement(ManagementSettings management) { this.management = management; }
}
