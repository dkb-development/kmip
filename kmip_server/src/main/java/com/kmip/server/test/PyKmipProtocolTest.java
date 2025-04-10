package com.kmip.server.test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Test class to send a PyKMIP-compatible request to the server.
 */
public class PyKmipProtocolTest {

    // TTLV Type constants
    private static final byte TYPE_STRUCTURE = 0x01;
    
    public static void main(String[] args) throws Exception {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
        
        // Install the all-trusting trust manager
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        
        // Create SSL socket factory
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        
        // Connect to the server
        try (SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket("localhost", 5697)) {
            socket.setEnabledProtocols(new String[] { "TLSv1.2" });
            socket.startHandshake();
            
            // Create a simple request
            byte[] request = createSimpleRequest();
            
            // Send the request
            OutputStream out = socket.getOutputStream();
            out.write(request);
            out.flush();
            
            // Read the response
            byte[] response = new byte[1024];
            int bytesRead = socket.getInputStream().read(response);
            
            if (bytesRead > 0) {
                System.out.println("Received response (" + bytesRead + " bytes):");
                printHex(response, 0, bytesRead);
            } else {
                System.out.println("No response received");
            }
        }
    }
    
    /**
     * Creates a simple KMIP request in the exact format that PyKMIP would send.
     */
    private static byte[] createSimpleRequest() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // Write the tag (0x420078 - Request Message)
        dos.writeByte(0x42);
        dos.writeByte(0x00);
        dos.writeByte(0x78);
        dos.writeByte(0x00); // Padding byte
        
        // Write the type (0x01 - Structure)
        dos.writeByte(TYPE_STRUCTURE);
        
        // Write a placeholder for the length (will be filled in later)
        dos.writeInt(0);
        
        // Get the encoded bytes
        byte[] encodedBytes = baos.toByteArray();
        
        // Fix the length field (bytes 5-8)
        // Set it to a small value (e.g., 16) for testing
        int payloadLength = 16;
        encodedBytes[5] = (byte)((payloadLength >> 24) & 0xFF);
        encodedBytes[6] = (byte)((payloadLength >> 16) & 0xFF);
        encodedBytes[7] = (byte)((payloadLength >> 8) & 0xFF);
        encodedBytes[8] = (byte)(payloadLength & 0xFF);
        
        return encodedBytes;
    }
    
    /**
     * Prints a byte array as hexadecimal.
     */
    private static void printHex(byte[] bytes, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            System.out.printf("%02X ", bytes[i] & 0xFF);
            if ((i - offset + 1) % 16 == 0) {
                System.out.println();
            }
        }
        System.out.println();
    }
}
