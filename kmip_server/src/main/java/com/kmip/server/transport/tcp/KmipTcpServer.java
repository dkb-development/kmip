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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.ByteBuffer;
import java.io.DataInputStream;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.kmip.server.operation.KmipRequestHandler;
import com.kmip.server.protocol.codec.KmipEncoder;
import com.kmip.server.protocol.codec.KmipParser;
import com.kmip.server.protocol.codec.KmipParser.KmipParseException;
import com.kmip.server.protocol.message.KmipMessage;
import com.kmip.server.protocol.tag.KmipTagResolver;

import java.io.DataOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class KmipTcpServer implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(KmipTcpServer.class);

    @Value("${server.port:5696}") // Use configured port or default
    private int port;

    private final ExecutorService executorService;
    private SSLServerSocket serverSocket;
    private boolean running;
    private final SSLContext sslContext;

    @Autowired
    private KmipRequestHandler requestHandler;

    @Autowired
    private KmipParser kmipParser;

    @Autowired
    private KmipEncoder kmipEncoder;

    // KMIP Operation Types (based on KMIP 2.0 spec and PyKMIP implementation)
    private static final byte CREATE_TYPE = 0x01;        // 1 in decimal
    private static final byte GET_TYPE = 0x0A;           // 10 in decimal
    private static final byte DESTROY_TYPE = 0x14;       // 20 in decimal
    private static final byte REKEY_TYPE = 0x18;         // 24 in decimal

    public KmipTcpServer(SSLContext sslContext) { // Inject SSLContext
        this.sslContext = sslContext;
        this.executorService = Executors.newCachedThreadPool();
        this.running = false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    public void start() {
        log.info("Testing SLF4J logger with Lombok @Slf4j annotation.");
        try {
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);

            // Configure SSLServerSocket if needed (e.g., require client auth)
            serverSocket.setNeedClientAuth(false); // Example: Client auth not required

            // Let Java JSSE handle negotiation with its defaults based on the SSLContext
            log.debug("Using default enabled protocols: {}", Arrays.toString(serverSocket.getEnabledProtocols()));
            log.debug("Using default enabled cipher suites: {}", Arrays.toString(serverSocket.getEnabledCipherSuites()));

            running = true;
            log.info("KMIP SSL Server started on port {} (using default SSL negotiation)", port);

            while (running) {
                Socket clientSocket = serverSocket.accept(); // Accepts SSL connections
                InetAddress clientAddress = clientSocket.getInetAddress();
                log.info("New SSL client connection accepted - IP: {}, Port: {}",
                    clientAddress.getHostAddress(),
                    clientSocket.getPort());

                executorService.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            log.error("Error in KMIP SSL server startup", e);
        }
    }

    private String getOperationName(byte operationType) {
        switch (operationType) {
            case CREATE_TYPE:
                return "CREATE";
            case GET_TYPE:
                return "GET";
            case DESTROY_TYPE:
                return "DESTROY";
            case REKEY_TYPE:
                return "REKEY";
            default:
                return "UNKNOWN";
        }
    }

    // Helper method to convert byte array to Hex String
    // This method is replaced by the overloaded versions below

    private void handleClient(Socket clientSocket) {
        try {
            InetAddress clientAddress = clientSocket.getInetAddress();
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                 if (bytesRead == 0) continue;

                log.info("Received KMIP data block from client - IP: {}, Port: {}, Size: {} bytes",
                    clientAddress.getHostAddress(), clientSocket.getPort(), bytesRead);

                try {
                    KmipMessage requestMessage = kmipParser.parse(buffer, bytesRead);
                    logParsedMessage(requestMessage, 0);

                    KmipMessage responseMessage = requestHandler.processRequest(requestMessage);

                    if (responseMessage != null) {
                         log.info("Encoding and sending KMIP response to client {}:{}",
                             clientAddress.getHostAddress(), clientSocket.getPort());

                         // Log the response message structure in detail
                         log.info("=== DETAILED RESPONSE MESSAGE STRUCTURE ===");
                         logParsedMessage(responseMessage, 0);

                         // Check for the presence of specific fields in the response payload
                         KmipMessage batchItem = (KmipMessage) responseMessage.getFields().get(KmipTagResolver.TAG_RESPONSE_BATCH_ITEM).get(0);
                         if (batchItem != null) {
                             KmipMessage responsePayload = (KmipMessage) batchItem.getFields().get(KmipTagResolver.TAG_RESPONSE_PAYLOAD).get(0);
                             if (responsePayload != null) {
                                 log.info("Response Payload Fields: {}", responsePayload.getFields().keySet().stream()
                                     .map(k -> "0x" + Integer.toHexString(k))
                                     .collect(java.util.stream.Collectors.joining(", ")));

                                 // Check for the presence of the SYMMETRIC_KEY field
                                 if (responsePayload.getFields().containsKey(KmipTagResolver.TAG_SYMMETRIC_KEY)) {
                                     log.info("SYMMETRIC_KEY field is present with tag: 0x{}",
                                         Integer.toHexString(KmipTagResolver.TAG_SYMMETRIC_KEY));
                                 } else {
                                     log.warn("SYMMETRIC_KEY field is missing from response payload!");
                                 }
                             }
                         }

                         byte[] responseBytes = kmipEncoder.encode(responseMessage, KmipEncoder.TAG_RESPONSE_MESSAGE);
                         log.info("Sending response ({} bytes): {}", responseBytes.length,
                             bytesToHex(responseBytes, 0, Math.min(100, responseBytes.length)));
                         out.write(responseBytes);
                         out.flush();
                     } else {
                         log.warn("No response message generated for request from {}:{}",
                             clientAddress.getHostAddress(), clientSocket.getPort());
                     }

                } catch (KmipParseException e) {
                    log.error("Error parsing KMIP message from {}: {}", clientAddress.getHostAddress(), e.getMessage());
                } catch (IOException e) {
                    log.error("Error encoding or sending response to {}:{}",
                        clientAddress.getHostAddress(), clientSocket.getPort(), e);
                    break;
                } catch (Exception e) {
                    log.error("Error processing KMIP message from {}: {}", clientAddress.getHostAddress(), e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            // Log specific socket errors
            if (e.getMessage() != null && e.getMessage().contains("Connection reset")) {
                log.warn("Client {} disconnected abruptly (Connection reset).");
            } else if (e instanceof java.io.EOFException) {
                 log.info("Client {} closed the connection.", clientSocket.getInetAddress().getHostAddress());
            } else {
                log.error("IOException handling client connection from {}",
                    clientSocket.getInetAddress().getHostAddress(), e);
            }
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                    log.info("Client connection closed normally - IP: {}",
                        clientSocket.getInetAddress().getHostAddress());
                }
            } catch (IOException e) {
                log.error("Error closing client socket for IP: {}",
                    clientSocket != null ? clientSocket.getInetAddress().getHostAddress() : "UNKNOWN", e);
            }
        }
     }

    // Recursive logging helper
    private void logParsedMessage(KmipMessage message, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        if (indentLevel == 0) {
            log.info("--- Parsed KMIP Message ---");
        }
        // Iterate through the entry set which contains Lists of objects
        for (Map.Entry<Integer, List<Object>> entry : message.getFields().entrySet()) {
            int tag = entry.getKey();
            List<Object> values = entry.getValue();

            // Log each value in the list for the current tag
            for (int i = 0; i < values.size(); i++) {
                 Object value = values.get(i);
                 String valueString;
                 String indexSuffix = values.size() > 1 ? "[" + i + "]" : ""; // Add index if multiple values

                 if (value instanceof KmipMessage) {
                     log.info("{}[Tag: 0x{}{} (Structure)]", indent, Integer.toHexString(tag), indexSuffix);
                     logParsedMessage((KmipMessage) value, indentLevel + 1);
                 } else if (value instanceof byte[]) {
                     valueString = bytesToHex((byte[]) value);
                     log.info("{}[Tag: 0x{}{} (Bytes)] Value: {}", indent, Integer.toHexString(tag), indexSuffix, valueString);
                 } else if (value != null) {
                     valueString = value.toString();
                     String typeInfo = value.getClass().getSimpleName();
                     log.info("{}[Tag: 0x{}{} ({})] Value: {}", indent, Integer.toHexString(tag), indexSuffix, typeInfo, valueString);
                 } else {
                     log.info("{}[Tag: 0x{}{}] Value: null", indent, Integer.toHexString(tag), indexSuffix);
                 }
             }
        }
         if (indentLevel == 0) {
             log.info("--- End Parsed Message ---");
         }
     }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString();
    }

    private String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length && i < bytes.length; i++) {
            sb.append(String.format("%02X ", bytes[i] & 0xFF));
            if ((i - offset + 1) % 16 == 0) sb.append("\n");
        }
        return sb.toString();
    }

    public void stop() {
        running = false;
        executorService.shutdown();
        try {
            if (serverSocket != null) {
                serverSocket.close();
                log.info("KMIP SSL Server stopped");
            }
        } catch (IOException e) {
            log.error("Error closing server socket", e);
        }
    }
}