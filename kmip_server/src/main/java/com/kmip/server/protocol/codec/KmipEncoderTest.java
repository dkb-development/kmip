package com.kmip.server.protocol.codec;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Simple test class to debug the TTLV encoding.
 */
public class KmipEncoderTest {

    public static void main(String[] args) throws IOException {
        // Create a simple TTLV structure
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // Tag: 0x42007B (Response Message)
        dos.writeByte(0x42);
        dos.writeByte(0x00);
        dos.writeByte(0x7B);
        dos.writeByte(0x00);

        // Type: 0x01 (Structure)
        dos.writeByte(0x01);

        // Length: 0x00000010 (16 bytes)
        dos.writeInt(16);

        // Value: 16 bytes of zeros
        byte[] value = new byte[16];
        dos.write(value);

        // Get the encoded bytes
        byte[] encodedBytes = baos.toByteArray();

        // Print the encoded bytes as hex
        System.out.println("Encoded bytes:");
        for (int i = 0; i < encodedBytes.length; i++) {
            System.out.printf("%02X ", encodedBytes[i] & 0xFF);
            if ((i + 1) % 16 == 0) {
                System.out.println();
            }
        }
        System.out.println();
    }
}
