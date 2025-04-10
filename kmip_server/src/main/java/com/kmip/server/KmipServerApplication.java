package com.kmip.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.kmip.server.transport.tcp.KmipTcpServer;

import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ComponentScan(basePackages = "com.kmip.server")
public class KmipServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KmipServerApplication.class, args);
    }

    @Bean
    public KmipTcpServer kmipTcpServer(javax.net.ssl.SSLContext sslContext) {
        return new KmipTcpServer(sslContext);
    }
} 