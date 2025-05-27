package com.kmip.server.config;

import com.kmip.server.transport.tcp.KmipTcpServer;
import com.kmip.server.core.exception.KmipTransportException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class ServerConfig {

    private static final Logger log = LoggerFactory.getLogger(ServerConfig.class);

    private final KmipTcpServer kmipTcpServer;

    public ServerConfig(KmipTcpServer kmipTcpServer) {
        this.kmipTcpServer = kmipTcpServer;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void startServer() {
        new Thread(() -> {
            try {
                kmipTcpServer.start();
            } catch (KmipTransportException e) {
                log.error("Failed to start KMIP TCP Server: {}", e.getMessage(), e);
                // You might want to shut down the application here
                System.exit(1);
            }
        }).start();
    }
}