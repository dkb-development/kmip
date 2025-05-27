package com.kmip.server.transport.tcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the KMIP TCP server.
 *
 * This class contains all configurable parameters for the TCP transport layer,
 * allowing for easy customization without code changes.
 */
@ConfigurationProperties(prefix = "kmip.server.tcp")
public class TcpServerConfig {

    /**
     * The port number for the KMIP server to listen on.
     * Default is 5696 as per KMIP specification.
     */
    private int port = 5696;

    /**
     * The bind address for the server.
     * Default is "0.0.0.0" to listen on all interfaces.
     */
    private String bindAddress = "0.0.0.0";

    /**
     * Maximum number of concurrent client connections.
     * Default is 100.
     */
    private int maxConnections = 100;

    /**
     * Socket timeout in milliseconds.
     * Default is 30 seconds.
     */
    private int socketTimeoutMs = 30000;

    /**
     * Connection timeout in milliseconds.
     * Default is 10 seconds.
     */
    private int connectionTimeoutMs = 10000;

    /**
     * Maximum message size in bytes.
     * Default is 1MB.
     */
    private int maxMessageSizeBytes = 1024 * 1024;

    /**
     * Buffer size for reading data.
     * Default is 8KB.
     */
    private int bufferSizeBytes = 8192;

    /**
     * Whether to enable TCP keep-alive.
     * Default is true.
     */
    private boolean keepAlive = true;

    /**
     * Whether to enable TCP no-delay (disable Nagle's algorithm).
     * Default is true for low latency.
     */
    private boolean tcpNoDelay = true;

    /**
     * Thread pool configuration for handling client connections.
     */
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();

    /**
     * SSL/TLS configuration.
     */
    private SslConfig ssl = new SslConfig();

    /**
     * Logging configuration for the transport layer.
     */
    private LoggingConfig logging = new LoggingConfig();

    // Getters and Setters

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getSocketTimeoutMs() {
        return socketTimeoutMs;
    }

    public void setSocketTimeoutMs(int socketTimeoutMs) {
        this.socketTimeoutMs = socketTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public int getMaxMessageSizeBytes() {
        return maxMessageSizeBytes;
    }

    public void setMaxMessageSizeBytes(int maxMessageSizeBytes) {
        this.maxMessageSizeBytes = maxMessageSizeBytes;
    }

    public int getBufferSizeBytes() {
        return bufferSizeBytes;
    }

    public void setBufferSizeBytes(int bufferSizeBytes) {
        this.bufferSizeBytes = bufferSizeBytes;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public ThreadPoolConfig getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPoolConfig threadPool) {
        this.threadPool = threadPool;
    }

    public SslConfig getSsl() {
        return ssl;
    }

    public void setSsl(SslConfig ssl) {
        this.ssl = ssl;
    }

    public LoggingConfig getLogging() {
        return logging;
    }

    public void setLogging(LoggingConfig logging) {
        this.logging = logging;
    }

    /**
     * Thread pool configuration for client connection handling.
     */
    public static class ThreadPoolConfig {
        private int corePoolSize = 10;
        private int maximumPoolSize = 50;
        private long keepAliveTimeSeconds = 60;
        private int queueCapacity = 1000;

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public long getKeepAliveTimeSeconds() {
            return keepAliveTimeSeconds;
        }

        public void setKeepAliveTimeSeconds(long keepAliveTimeSeconds) {
            this.keepAliveTimeSeconds = keepAliveTimeSeconds;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
    }

    /**
     * SSL/TLS configuration.
     */
    public static class SslConfig {
        private boolean enabled = true;
        private boolean requireClientAuth = false;
        private String keyStorePath;
        private String keyStorePassword;
        private String keyStoreType = "JKS";
        private String trustStorePath;
        private String trustStorePassword;
        private String trustStoreType = "JKS";
        private String[] enabledProtocols = {"TLSv1.2", "TLSv1.3"};
        private String[] enabledCipherSuites;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isRequireClientAuth() {
            return requireClientAuth;
        }

        public void setRequireClientAuth(boolean requireClientAuth) {
            this.requireClientAuth = requireClientAuth;
        }

        public String getKeyStorePath() {
            return keyStorePath;
        }

        public void setKeyStorePath(String keyStorePath) {
            this.keyStorePath = keyStorePath;
        }

        public String getKeyStorePassword() {
            return keyStorePassword;
        }

        public void setKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
        }

        public String getKeyStoreType() {
            return keyStoreType;
        }

        public void setKeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
        }

        public String getTrustStorePath() {
            return trustStorePath;
        }

        public void setTrustStorePath(String trustStorePath) {
            this.trustStorePath = trustStorePath;
        }

        public String getTrustStorePassword() {
            return trustStorePassword;
        }

        public void setTrustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
        }

        public String getTrustStoreType() {
            return trustStoreType;
        }

        public void setTrustStoreType(String trustStoreType) {
            this.trustStoreType = trustStoreType;
        }

        public String[] getEnabledProtocols() {
            return enabledProtocols;
        }

        public void setEnabledProtocols(String[] enabledProtocols) {
            this.enabledProtocols = enabledProtocols;
        }

        public String[] getEnabledCipherSuites() {
            return enabledCipherSuites;
        }

        public void setEnabledCipherSuites(String[] enabledCipherSuites) {
            this.enabledCipherSuites = enabledCipherSuites;
        }
    }

    /**
     * Logging configuration for the transport layer.
     */
    public static class LoggingConfig {
        private boolean logConnections = true;
        private boolean logMessages = false;
        private boolean logMessageDetails = false;
        private int maxLoggedMessageSize = 1000;

        public boolean isLogConnections() {
            return logConnections;
        }

        public void setLogConnections(boolean logConnections) {
            this.logConnections = logConnections;
        }

        public boolean isLogMessages() {
            return logMessages;
        }

        public void setLogMessages(boolean logMessages) {
            this.logMessages = logMessages;
        }

        public boolean isLogMessageDetails() {
            return logMessageDetails;
        }

        public void setLogMessageDetails(boolean logMessageDetails) {
            this.logMessageDetails = logMessageDetails;
        }

        public int getMaxLoggedMessageSize() {
            return maxLoggedMessageSize;
        }

        public void setMaxLoggedMessageSize(int maxLoggedMessageSize) {
            this.maxLoggedMessageSize = maxLoggedMessageSize;
        }
    }
}
