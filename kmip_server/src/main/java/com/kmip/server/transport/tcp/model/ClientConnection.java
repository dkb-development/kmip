package com.kmip.server.transport.tcp.model;

import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a client connection to the KMIP server.
 * 
 * This class encapsulates information about a connected client,
 * including connection details, statistics, and state.
 */
public class ClientConnection {
    
    private static final AtomicLong connectionIdGenerator = new AtomicLong(0);
    
    private final long connectionId;
    private final Socket socket;
    private final String remoteAddress;
    private final int remotePort;
    private final LocalDateTime connectionTime;
    private final String clientIdentifier;
    
    // Connection statistics
    private volatile long bytesReceived = 0;
    private volatile long bytesSent = 0;
    private volatile long requestsProcessed = 0;
    private volatile LocalDateTime lastActivityTime;
    private volatile boolean isActive = true;
    
    /**
     * Creates a new ClientConnection instance.
     * 
     * @param socket the client socket
     */
    public ClientConnection(Socket socket) {
        this.connectionId = connectionIdGenerator.incrementAndGet();
        this.socket = socket;
        this.remoteAddress = socket.getInetAddress().getHostAddress();
        this.remotePort = socket.getPort();
        this.connectionTime = LocalDateTime.now();
        this.lastActivityTime = this.connectionTime;
        this.clientIdentifier = generateClientIdentifier();
    }
    
    /**
     * Gets the unique connection ID.
     * 
     * @return the connection ID
     */
    public long getConnectionId() {
        return connectionId;
    }
    
    /**
     * Gets the client socket.
     * 
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }
    
    /**
     * Gets the remote client address.
     * 
     * @return the remote address
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }
    
    /**
     * Gets the remote client port.
     * 
     * @return the remote port
     */
    public int getRemotePort() {
        return remotePort;
    }
    
    /**
     * Gets the connection establishment time.
     * 
     * @return the connection time
     */
    public LocalDateTime getConnectionTime() {
        return connectionTime;
    }
    
    /**
     * Gets the client identifier.
     * 
     * @return the client identifier
     */
    public String getClientIdentifier() {
        return clientIdentifier;
    }
    
    /**
     * Gets the total bytes received from this client.
     * 
     * @return bytes received
     */
    public long getBytesReceived() {
        return bytesReceived;
    }
    
    /**
     * Gets the total bytes sent to this client.
     * 
     * @return bytes sent
     */
    public long getBytesSent() {
        return bytesSent;
    }
    
    /**
     * Gets the total number of requests processed for this client.
     * 
     * @return requests processed
     */
    public long getRequestsProcessed() {
        return requestsProcessed;
    }
    
    /**
     * Gets the last activity time.
     * 
     * @return the last activity time
     */
    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }
    
    /**
     * Checks if the connection is active.
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return isActive && !socket.isClosed();
    }
    
    /**
     * Updates the bytes received counter.
     * 
     * @param bytes the number of bytes received
     */
    public void addBytesReceived(long bytes) {
        this.bytesReceived += bytes;
        updateLastActivity();
    }
    
    /**
     * Updates the bytes sent counter.
     * 
     * @param bytes the number of bytes sent
     */
    public void addBytesSent(long bytes) {
        this.bytesSent += bytes;
        updateLastActivity();
    }
    
    /**
     * Increments the requests processed counter.
     */
    public void incrementRequestsProcessed() {
        this.requestsProcessed++;
        updateLastActivity();
    }
    
    /**
     * Updates the last activity time to now.
     */
    public void updateLastActivity() {
        this.lastActivityTime = LocalDateTime.now();
    }
    
    /**
     * Marks the connection as inactive.
     */
    public void markInactive() {
        this.isActive = false;
    }
    
    /**
     * Gets the connection duration in seconds.
     * 
     * @return connection duration in seconds
     */
    public long getConnectionDurationSeconds() {
        return java.time.Duration.between(connectionTime, LocalDateTime.now()).getSeconds();
    }
    
    /**
     * Gets the idle time in seconds since last activity.
     * 
     * @return idle time in seconds
     */
    public long getIdleTimeSeconds() {
        return java.time.Duration.between(lastActivityTime, LocalDateTime.now()).getSeconds();
    }
    
    /**
     * Gets a formatted connection string.
     * 
     * @return formatted connection string
     */
    public String getConnectionString() {
        return String.format("%s:%d", remoteAddress, remotePort);
    }
    
    /**
     * Generates a unique client identifier.
     * 
     * @return client identifier
     */
    private String generateClientIdentifier() {
        return String.format("CLIENT-%d-%s-%d", 
            connectionId, 
            remoteAddress.replace(".", ""), 
            remotePort);
    }
    
    /**
     * Gets connection statistics as a formatted string.
     * 
     * @return connection statistics
     */
    public String getStatistics() {
        return String.format(
            "Connection[%d]: %s, Duration: %ds, Requests: %d, RX: %d bytes, TX: %d bytes, Idle: %ds",
            connectionId,
            getConnectionString(),
            getConnectionDurationSeconds(),
            requestsProcessed,
            bytesReceived,
            bytesSent,
            getIdleTimeSeconds()
        );
    }
    
    @Override
    public String toString() {
        return String.format("ClientConnection[id=%d, address=%s, active=%s]", 
            connectionId, getConnectionString(), isActive());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClientConnection that = (ClientConnection) obj;
        return connectionId == that.connectionId;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(connectionId);
    }
}
