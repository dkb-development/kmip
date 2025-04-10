package com.kmip.server.test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * A simple KMIP server that follows the PyKMIP protocol exactly.
 */
public class SimpleKmipServer {
    
    // TTLV Type constants
    private static final byte TYPE_STRUCTURE = 0x01;
    
    private static final int PORT = 5697;
    private static final int HEADER_SIZE = 8;
    
    private SSLServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean running = false;
    
    /**
     * Starts the server.
     */
    public void start() throws Exception {
        // Load the keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream is = SimpleKmipServer.class.getResourceAsStream("/keystore.jks")) {
            keyStore.load(is, "password".toCharArray());
        }
        
        // Create key manager factory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "password".toCharArray());
        
        // Create SSL context
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kmf.getKeyManagers(), null, null);
        
        // Create server socket factory
        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
        
        // Create server socket
        serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(PORT);
        serverSocket.setEnabledProtocols(new String[] { "TLSv1.2" });
        serverSocket.setNeedClientAuth(false);
        
        // Create thread pool
        executorService = Executors.newFixedThreadPool(10);
        
        // Start accepting connections
        running = true;
        System.out.println("Server started on port " + PORT);
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Stops the server.
     */
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null) {
            serverSocket.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
        System.out.println("Server stopped");
    }
    
    /**
     * Handles a client connection.
     */
    private void handleClient(Socket clientSocket) {
        String clientIp = clientSocket.getInetAddress().getHostAddress();
        System.out.println("Client connected: " + clientIp);
        
        try {
            // Set socket timeout to prevent hanging
            clientSocket.setSoTimeout(30000); // 30 seconds timeout
            
            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();
            
            // Read the header (8 bytes)
            byte[] header = new byte[HEADER_SIZE];
            int bytesRead = in.read(header);
            
            if (bytesRead == HEADER_SIZE) {
                // Extract the length from the header (bytes 4-7)
                int length = ((header[4] & 0xFF) << 24) |
                             ((header[5] & 0xFF) << 16) |
                             ((header[6] & 0xFF) << 8) |
                             (header[7] & 0xFF);
                
                System.out.println("Received request with length: " + length);
                
                // Read the payload
                byte[] payload = new byte[length];
                bytesRead = in.read(payload);
                
                if (bytesRead == length) {
                    // Create a simple response
                    byte[] response = createSimpleResponse();
                    
                    // Send the response
                    out.write(response);
                    out.flush();
                    
                    System.out.println("Sent response (" + response.length + " bytes)");
                } else {
                    System.err.println("Failed to read payload: expected " + length + " bytes, got " + bytesRead);
                }
            } else {
                System.err.println("Failed to read header: expected " + HEADER_SIZE + " bytes, got " + bytesRead);
            }
            
            // Close the client socket
            clientSocket.close();
            System.out.println("Client connection closed: " + clientIp);
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
    
    /**
     * Creates a simple KMIP response in the exact format that PyKMIP expects.
     */
    private byte[] createSimpleResponse() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // Write the tag (0x42007B - Response Message)
        dos.writeByte(0x42);
        dos.writeByte(0x00);
        dos.writeByte(0x7B);
        dos.writeByte(0x00); // Padding byte
        
        // Write the type (0x01 - Structure)
        dos.writeByte(TYPE_STRUCTURE);
        
        // Write the length (16 bytes)
        dos.writeInt(16);
        
        // Write a simple payload (16 bytes of zeros)
        for (int i = 0; i < 16; i++) {
            dos.writeByte(0);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Main method to start the server.
     */
    public static void main(String[] args) throws Exception {
        SimpleKmipServer server = new SimpleKmipServer();
        server.start();
    }
}
