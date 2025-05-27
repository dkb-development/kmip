package com.kmip.server.transport.tcp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.RejectedExecutionException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.kmip.server.core.exception.KmipTransportException;
import com.kmip.server.core.exception.KmipProtocolException;
import com.kmip.server.core.enums.KmipOperationType;
import com.kmip.server.core.enums.ServerState;
import com.kmip.server.protocol.handler.KmipProtocolHandler;
import com.kmip.server.protocol.codec.KmipParser;
import com.kmip.server.protocol.codec.KmipEncoder;
import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.core.exception.KmipParseException;
import com.kmip.server.transport.tcp.model.ClientConnection;
import com.kmip.server.transport.tcp.config.TcpServerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KMIP TCP Server - Transport Layer (Part-1)
 *
 * This class implements the TCP transport layer for the KMIP server.
 * It handles:
 * - TCP/SSL connection management
 * - Message framing and basic I/O
 * - Connection lifecycle management
 * - Error handling at transport level
 *
 * Business logic is delegated to KmipProtocolHandler (Part-2).
 *
 * Architecture:
 * Part-1 (Transport): KmipTcpServer -> handles network, SSL, basic I/O
 * Part-2 (Protocol): KmipProtocolHandler -> handles KMIP operations, business logic
 */
@Component
public class KmipTcpServer implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(KmipTcpServer.class);

    // Configuration and dependencies
    private final TcpServerConfig config;
    private final SSLContext sslContext;
    private final KmipProtocolHandler protocolHandler;
    private final KmipParser kmipParser;
    private final KmipEncoder kmipEncoder;

    // Server state management
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile ServerState serverState = ServerState.NOT_INITIALIZED;
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    // Network components
    private SSLServerSocket serverSocket;
    private ExecutorService connectionExecutor;
    private ExecutorService clientHandlerExecutor;

    // Connection tracking
    private final Map<Long, ClientConnection> activeClientConnections = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Constructor with dependency injection.
     *
     * @param config TCP server configuration
     * @param sslContext SSL context for secure connections
     * @param protocolHandler KMIP protocol handler for business logic
     * @param kmipParser KMIP message parser
     * @param kmipEncoder KMIP message encoder
     */
    public KmipTcpServer(TcpServerConfig config,
                        SSLContext sslContext,
                        KmipProtocolHandler protocolHandler,
                        KmipParser kmipParser,
                        KmipEncoder kmipEncoder) {
        this.config = config;
        this.sslContext = sslContext;
        this.protocolHandler = protocolHandler;
        this.kmipParser = kmipParser;
        this.kmipEncoder = kmipEncoder;

        log.info("KMIP TCP Server initialized with configuration: port={}, maxConnections={}",
                config.getPort(), config.getMaxConnections());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
        start();
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    /**
     * Initializes the server components.
     *
     * @throws KmipTransportException if initialization fails
     */
    private void initialize() throws KmipTransportException {
        try {
            serverState = ServerState.INITIALIZING;
            log.info("Initializing KMIP TCP Server...");

            // Initialize thread pools
            initializeThreadPools();

            // Initialize SSL server socket
            initializeServerSocket();

            serverState = ServerState.INITIALIZED;
            log.info("KMIP TCP Server initialized successfully");

        } catch (Exception e) {
            serverState = ServerState.ERROR;
            throw new KmipTransportException(
                KmipTransportException.TransportErrorType.CONNECTION_FAILED,
                "Failed to initialize KMIP TCP Server", e);
        }
    }

    /**
     * Starts the server and begins accepting connections.
     *
     * @throws KmipTransportException if server startup fails
     */
    public void start() throws KmipTransportException {
        if (!running.compareAndSet(false, true)) {
            log.warn("KMIP TCP Server is already running");
            return;
        }

        try {
            serverState = ServerState.STARTING;
            log.info("Starting KMIP TCP Server on port {}", config.getPort());

            // Start connection acceptor in separate thread
            connectionExecutor.submit(this::acceptConnections);

            serverState = ServerState.RUNNING;
            log.info("KMIP TCP Server started successfully and accepting connections");

        } catch (Exception e) {
            running.set(false);
            serverState = ServerState.ERROR;
            throw new KmipTransportException(
                KmipTransportException.TransportErrorType.CONNECTION_FAILED,
                "Failed to start KMIP TCP Server", e);
        }
    }

    /**
     * Initializes thread pools for connection handling.
     */
    private void initializeThreadPools() {
        TcpServerConfig.ThreadPoolConfig threadPoolConfig = config.getThreadPool();

        // Connection acceptor thread pool (single thread)
        connectionExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "KMIP-Connection-Acceptor");
            t.setDaemon(false);
            return t;
        });

        // Client handler thread pool
        clientHandlerExecutor = new java.util.concurrent.ThreadPoolExecutor(
            threadPoolConfig.getCorePoolSize(),
            threadPoolConfig.getMaximumPoolSize(),
            threadPoolConfig.getKeepAliveTimeSeconds(),
            TimeUnit.SECONDS,
            new java.util.concurrent.LinkedBlockingQueue<>(threadPoolConfig.getQueueCapacity()),
            r -> {
                Thread t = new Thread(r, "KMIP-Client-Handler-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            }
        );

        log.info("Thread pools initialized - Core: {}, Max: {}, Queue: {}",
                threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getQueueCapacity());
    }

    /**
     * Initializes the SSL server socket.
     *
     * @throws IOException if socket initialization fails
     */
    private void initializeServerSocket() throws IOException {
        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
        serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(config.getPort());

        // Configure socket options
        serverSocket.setReuseAddress(true);
        serverSocket.setSoTimeout(config.getConnectionTimeoutMs());

        // Configure SSL options
        if (config.getSsl().isRequireClientAuth()) {
            serverSocket.setNeedClientAuth(true);
        } else {
            serverSocket.setNeedClientAuth(false);
        }

        // Set enabled protocols and cipher suites if specified
        if (config.getSsl().getEnabledProtocols() != null) {
            serverSocket.setEnabledProtocols(config.getSsl().getEnabledProtocols());
        }

        if (config.getSsl().getEnabledCipherSuites() != null) {
            serverSocket.setEnabledCipherSuites(config.getSsl().getEnabledCipherSuites());
        }

        log.info("SSL Server socket initialized on port {} with protocols: {}",
                config.getPort(),
                Arrays.toString(serverSocket.getEnabledProtocols()));
    }

    /**
     * Main connection acceptance loop.
     * Runs in a separate thread to accept incoming connections.
     */
    private void acceptConnections() {
        log.info("Connection acceptor started, listening for connections...");

        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                // Check connection limit
                if (activeConnections.get() >= config.getMaxConnections()) {
                    log.warn("Maximum connections ({}) reached, rejecting new connections",
                            config.getMaxConnections());
                    Thread.sleep(1000); // Brief pause before checking again
                    continue;
                }

                // Accept new connection
                Socket clientSocket = serverSocket.accept();

                // Create client connection wrapper
                ClientConnection clientConnection = new ClientConnection(clientSocket);
                activeClientConnections.put(clientConnection.getConnectionId(), clientConnection);
                activeConnections.incrementAndGet();

                if (config.getLogging().isLogConnections()) {
                    log.info("New client connection accepted: {}", clientConnection);
                }

                // Notify protocol handler
                protocolHandler.onConnectionEstablished(clientConnection);

                // Handle client in separate thread
                clientHandlerExecutor.submit(() -> handleClient(clientConnection));

            } catch (SocketTimeoutException e) {
                // Normal timeout, continue loop
                continue;
            } catch (IOException e) {
                if (running.get()) {
                    log.error("Error accepting client connection", e);
                } else {
                    log.debug("Server stopped, connection acceptor exiting");
                    break;
                }
            } catch (InterruptedException e) {
                log.info("Connection acceptor interrupted, exiting");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Unexpected error in connection acceptor", e);
            }
        }

        log.info("Connection acceptor stopped");
    }

    /**
     * Gets operation name from operation type using enum.
     *
     * @param operationType the operation type code
     * @return the operation name
     */
    private String getOperationName(int operationType) {
        KmipOperationType operation = KmipOperationType.fromCode(operationType);
        return operation.getDisplayName();
    }

    /**
     * Handles a client connection.
     * This method processes KMIP messages from a connected client.
     *
     * @param clientConnection the client connection to handle
     */
    private void handleClient(ClientConnection clientConnection) {
        Socket clientSocket = clientConnection.getSocket();

        try {
            // Configure socket options
            clientSocket.setSoTimeout(config.getSocketTimeoutMs());
            clientSocket.setKeepAlive(config.isKeepAlive());
            clientSocket.setTcpNoDelay(config.isTcpNoDelay());

            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            byte[] buffer = new byte[config.getBufferSizeBytes()];

            if (config.getLogging().isLogConnections()) {
                log.info("Started handling client: {}", clientConnection);
            }

            while (clientConnection.isActive() && !Thread.currentThread().isInterrupted()) {
                try {
                    int bytesRead = in.read(buffer);
                    if (bytesRead == -1) {
                        // Client closed connection
                        break;
                    }

                    if (bytesRead == 0) {
                        continue;
                    }

                    // Check message size limit
                    if (bytesRead > config.getMaxMessageSizeBytes()) {
                        throw new KmipTransportException(
                            KmipTransportException.TransportErrorType.MESSAGE_TOO_LARGE,
                            "Message size exceeds maximum allowed size",
                            clientConnection.getRemoteAddress(),
                            clientConnection.getRemotePort()
                        );
                    }

                    clientConnection.addBytesReceived(bytesRead);

                    if (config.getLogging().isLogMessages()) {
                        log.debug("Received KMIP message from {}: {} bytes",
                                clientConnection.getConnectionString(), bytesRead);
                    }

                    // Process the message through protocol handler
                    processClientMessage(clientConnection, buffer, bytesRead, out);

                } catch (KmipTransportException e) {
                    log.error("Transport error processing message from {}: {}",
                            clientConnection.getConnectionString(), e.getMessage());
                    // Send error response if possible
                    sendErrorResponse(clientConnection, e, out);
                    break;
                } catch (SocketTimeoutException e) {
                    log.debug("Socket timeout for client {}", clientConnection.getConnectionString());
                    // Continue loop for timeout
                } catch (EOFException e) {
                    log.info("Client {} closed connection", clientConnection.getConnectionString());
                    break;
                } catch (IOException e) {
                    log.error("I/O error handling client {}: {}",
                            clientConnection.getConnectionString(), e.getMessage());
                    break;
                } catch (Exception e) {
                    log.error("Unexpected error processing message from {}",
                            clientConnection.getConnectionString(), e);
                    break;
                }
            }

        } catch (IOException e) {
            handleConnectionError(clientConnection, e);
        } finally {
            cleanupClientConnection(clientConnection);
        }
    }

    /**
     * Processes a KMIP message from a client.
     * This method delegates to the protocol handler for actual processing.
     *
     * @param clientConnection the client connection
     * @param buffer the message buffer
     * @param bytesRead the number of bytes read
     * @param out the output stream for sending responses
     * @throws KmipTransportException if there's a transport-level error
     */
    private void processClientMessage(ClientConnection clientConnection, byte[] buffer,
                                    int bytesRead, DataOutputStream out) throws KmipTransportException {
        try {
            // This is where we would parse the KMIP message and delegate to protocol handler
            // For now, we'll implement a placeholder that shows the architecture

            clientConnection.incrementRequestsProcessed();

            if (config.getLogging().isLogMessageDetails()) {
                log.debug("Processing KMIP message from {}: {} bytes",
                        clientConnection.getConnectionString(), bytesRead);
            }

            // Parse the KMIP message
            KmipMessage requestMessage = kmipParser.parse(buffer, bytesRead);

            if (config.getLogging().isLogMessageDetails()) {
                log.debug("Parsed KMIP request message from {}", clientConnection.getConnectionString());
            }

            // Process the request through the protocol handler
            KmipMessage responseMessage = protocolHandler.processRequest(requestMessage, clientConnection);

            if (responseMessage != null) {
                // Encode the response message
                // Need to determine the correct root tag for encoding
                byte[] responseBytes = kmipEncoder.encode(responseMessage, 0x42007B); // TAG_RESPONSE_MESSAGE

                // Send the response back to the client
                out.write(responseBytes);
                out.flush();

                clientConnection.addBytesSent(responseBytes.length);

                if (config.getLogging().isLogMessages()) {
                    log.debug("Sent KMIP response to {}: {} bytes",
                            clientConnection.getConnectionString(), responseBytes.length);
                }
            } else {
                log.warn("No response message generated for request from {}",
                        clientConnection.getConnectionString());
            }

        } catch (KmipParseException e) {
            log.error("Error parsing KMIP message from {}: {}",
                    clientConnection.getConnectionString(), e.getMessage());
            throw new KmipTransportException(
                KmipTransportException.TransportErrorType.PROTOCOL_VIOLATION,
                "Error parsing KMIP message: " + e.getMessage(), e,
                clientConnection.getRemoteAddress(),
                clientConnection.getRemotePort()
            );
        } catch (KmipProtocolException e) {
            log.error("Protocol error processing message from {}: {}",
                    clientConnection.getConnectionString(), e.getMessage());
            throw new KmipTransportException(
                KmipTransportException.TransportErrorType.PROTOCOL_VIOLATION,
                "Protocol error: " + e.getMessage(), e,
                clientConnection.getRemoteAddress(),
                clientConnection.getRemotePort()
            );
        } catch (IOException e) {
            log.error("I/O error processing message from {}: {}",
                    clientConnection.getConnectionString(), e.getMessage());
            throw new KmipTransportException(
                KmipTransportException.TransportErrorType.CONNECTION_FAILED,
                "I/O error processing message: " + e.getMessage(), e,
                clientConnection.getRemoteAddress(),
                clientConnection.getRemotePort()
            );
        } catch (Exception e) {
            log.error("Unexpected error processing message from {}: {}",
                    clientConnection.getConnectionString(), e.getMessage(), e);
            throw new KmipTransportException(
                KmipTransportException.TransportErrorType.PROTOCOL_VIOLATION,
                "Unexpected error processing KMIP message: " + e.getMessage(), e,
                clientConnection.getRemoteAddress(),
                clientConnection.getRemotePort()
            );
        }
    }

    /**
     * Sends an error response to the client.
     *
     * @param clientConnection the client connection
     * @param exception the transport exception that occurred
     * @param out the output stream
     */
    private void sendErrorResponse(ClientConnection clientConnection, KmipTransportException exception,
                                 DataOutputStream out) {
        try {
            // TODO: Implement error response generation
            // For now, just log the error
            log.error("Would send error response to {}: {}",
                    clientConnection.getConnectionString(), exception.getMessage());
        } catch (Exception e) {
            log.error("Failed to send error response to {}: {}",
                    clientConnection.getConnectionString(), e.getMessage());
        }
    }

    /**
     * Handles connection errors.
     *
     * @param clientConnection the client connection
     * @param exception the I/O exception that occurred
     */
    private void handleConnectionError(ClientConnection clientConnection, IOException exception) {
        if (exception.getMessage() != null && exception.getMessage().contains("Connection reset")) {
            log.warn("Client {} disconnected abruptly (Connection reset)",
                    clientConnection.getConnectionString());
        } else if (exception instanceof EOFException) {
            log.info("Client {} closed the connection", clientConnection.getConnectionString());
        } else {
            log.error("I/O error handling client connection {}: {}",
                    clientConnection.getConnectionString(), exception.getMessage());
        }
    }

    /**
     * Cleans up a client connection.
     *
     * @param clientConnection the client connection to clean up
     */
    private void cleanupClientConnection(ClientConnection clientConnection) {
        try {
            // Mark connection as inactive
            clientConnection.markInactive();

            // Remove from active connections
            activeClientConnections.remove(clientConnection.getConnectionId());
            activeConnections.decrementAndGet();

            // Notify protocol handler
            protocolHandler.onConnectionClosed(clientConnection);

            // Close socket if not already closed
            Socket socket = clientConnection.getSocket();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            if (config.getLogging().isLogConnections()) {
                log.info("Client connection cleaned up: {} (Duration: {}s, Requests: {})",
                        clientConnection.getConnectionString(),
                        clientConnection.getConnectionDurationSeconds(),
                        clientConnection.getRequestsProcessed());
            }

        } catch (Exception e) {
            log.error("Error cleaning up client connection {}: {}",
                    clientConnection.getConnectionString(), e.getMessage());
        }
    }

    /**
     * Utility method for converting byte arrays to hex strings.
     *
     * @param bytes the byte array to convert
     * @return hex string representation
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString();
    }

    /**
     * Utility method for converting byte arrays to hex strings with offset and length.
     *
     * @param bytes the byte array to convert
     * @param offset the starting offset
     * @param length the number of bytes to convert
     * @return hex string representation
     */
    private String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length && i < bytes.length; i++) {
            sb.append(String.format("%02X ", bytes[i] & 0xFF));
            if ((i - offset + 1) % 16 == 0) sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Stops the KMIP TCP server gracefully.
     *
     * @throws KmipTransportException if there's an error during shutdown
     */
    public void stop() throws KmipTransportException {
        if (!running.compareAndSet(true, false)) {
            log.warn("KMIP TCP Server is not running");
            return;
        }

        try {
            serverState = ServerState.STOPPING;
            log.info("Stopping KMIP TCP Server...");

            // Close server socket to stop accepting new connections
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                log.info("Server socket closed");
            }

            // Shutdown connection acceptor
            if (connectionExecutor != null) {
                connectionExecutor.shutdown();
                try {
                    if (!connectionExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        connectionExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    connectionExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            // Shutdown client handler executor
            if (clientHandlerExecutor != null) {
                clientHandlerExecutor.shutdown();
                try {
                    if (!clientHandlerExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                        clientHandlerExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    clientHandlerExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            // Close all active client connections
            for (ClientConnection connection : activeClientConnections.values()) {
                try {
                    connection.markInactive();
                    if (!connection.getSocket().isClosed()) {
                        connection.getSocket().close();
                    }
                } catch (Exception e) {
                    log.warn("Error closing client connection {}: {}",
                            connection.getConnectionString(), e.getMessage());
                }
            }
            activeClientConnections.clear();
            activeConnections.set(0);

            serverState = ServerState.STOPPED;
            log.info("KMIP TCP Server stopped successfully");

        } catch (Exception e) {
            serverState = ServerState.ERROR;
            throw new KmipTransportException(
                KmipTransportException.TransportErrorType.CONNECTION_FAILED,
                "Error stopping KMIP TCP Server", e);
        }
    }

    /**
     * Gets the current server state.
     *
     * @return the current server state
     */
    public ServerState getServerState() {
        return serverState;
    }

    /**
     * Gets the number of active connections.
     *
     * @return the number of active connections
     */
    public int getActiveConnectionCount() {
        return activeConnections.get();
    }

    /**
     * Gets server statistics.
     *
     * @return a map of server statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("serverState", serverState.getDisplayName());
        stats.put("activeConnections", activeConnections.get());
        stats.put("totalConnections", activeClientConnections.size());
        stats.put("port", config.getPort());
        stats.put("maxConnections", config.getMaxConnections());
        return stats;
    }
}