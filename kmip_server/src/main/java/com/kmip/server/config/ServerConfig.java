package com.kmip.server.config;

import com.kmip.server.transport.tcp.KmipTcpServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class ServerConfig {

    private final KmipTcpServer kmipTcpServer;

    public ServerConfig(KmipTcpServer kmipTcpServer) {
        this.kmipTcpServer = kmipTcpServer;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void startServer() {
        new Thread(kmipTcpServer::start).start();
    }
}