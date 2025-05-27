package com.kmip.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.kmip.server.transport.tcp.KmipTcpServer;
import com.kmip.server.transport.tcp.config.TcpServerConfig;
import com.kmip.server.protocol.handler.KmipProtocolHandler;
import com.kmip.server.protocol.codec.KmipParser;
import com.kmip.server.protocol.codec.KmipEncoder;

@SpringBootApplication
@ComponentScan(basePackages = "com.kmip.server")
@EnableConfigurationProperties(TcpServerConfig.class)
public class KmipServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KmipServerApplication.class, args);
    }

    @Bean
    public KmipTcpServer kmipTcpServer(TcpServerConfig config,
                                      javax.net.ssl.SSLContext sslContext,
                                      KmipProtocolHandler protocolHandler,
                                      KmipParser kmipParser,
                                      KmipEncoder kmipEncoder) {
        return new KmipTcpServer(config, sslContext, protocolHandler, kmipParser, kmipEncoder);
    }
}