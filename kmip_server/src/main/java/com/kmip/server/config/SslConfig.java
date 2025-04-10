package com.kmip.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

@Configuration
public class SslConfig {

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${server.ssl.key-store:#{null}}")
    private Resource keyStoreResource;

    @Value("${server.ssl.key-store-password:#{null}}")
    private String keyStorePassword;

    @Value("${server.ssl.key-store-type:JKS}")
    private String keyStoreType;

    @Value("${server.ssl.key-password:#{null}}")
    private String keyPassword;

    // Optional: TrustStore configuration can be added similarly if needed

    @Bean
    public SSLContext sslContext() throws Exception {
        if (!sslEnabled || keyStoreResource == null || !keyStoreResource.exists()) {
            // If SSL is disabled or keystore not configured/found, return a default context
            // This might allow the app to start but SSL won't function.
            // Alternatively, throw an exception if SSL is mandatory.
            System.err.println("WARN: SSL is disabled or keystore not configured/found. Creating default SSLContext.");
            return SSLContext.getDefault(); // Or handle error more strictly
        }

        KeyStore ks = KeyStore.getInstance(keyStoreType);
        try (InputStream ksInputStream = keyStoreResource.getInputStream()) {
            ks.load(ksInputStream, keyStorePassword != null ? keyStorePassword.toCharArray() : null);
        }

        // Use key-password if provided, otherwise fallback to key-store-password
        char[] actualKeyPassword = StringUtils.hasText(keyPassword) ? 
                                   keyPassword.toCharArray() : 
                                   (keyStorePassword != null ? keyStorePassword.toCharArray() : null);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, actualKeyPassword);

        // Initialize TrustManagerFactory (using same keystore as truststore for simplicity here)
        // For production, use a separate truststore if needed
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks); 

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2"); // Specify TLS version
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        System.out.println("INFO: Custom SSLContext bean created successfully.");
        return sslContext;
    }
} 