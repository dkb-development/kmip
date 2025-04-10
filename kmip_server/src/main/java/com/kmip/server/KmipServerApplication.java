package com.kmip.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import com.kmip.server.transport.tcp.KmipTcpServer;
import com.kmip.server.protocol.codec.KmipEncoder;
import com.kmip.server.protocol.codec.KmipParser;
import com.kmip.server.operation.KmipRequestHandler;

@SpringBootApplication
@ComponentScan(basePackages = "com.kmip.server")
public class KmipServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KmipServerApplication.class, args);
        // Keep the application running
        Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered. Waiting for main thread to complete...");
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        // Keep the main thread alive
        try {
            System.out.println("KMIP Server is running. Press Ctrl+C to stop.");
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Bean
    public KmipTcpServer kmipTcpServer(javax.net.ssl.SSLContext sslContext, KmipParser kmipParser, KmipEncoder kmipEncoder, KmipRequestHandler kmipRequestHandler) {
        return new KmipTcpServer(sslContext, kmipParser, kmipEncoder, kmipRequestHandler);
    }
}