package com.kmip.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service("inMemoryKms") // Give it a specific bean name
public class InMemoryKeyManagementService implements KeyManagementService {

    private static final Logger log = LoggerFactory.getLogger(InMemoryKeyManagementService.class);
    private final Map<String, SecretKey> keyStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Override
    public String createSymmetricKey(String algorithm, int keyLength, Map<String, Object> attributes) throws KmipException {
        log.info("Using InMemoryKMS to create Symmetric Key - Algorithm: {}, Length: {}", algorithm, keyLength);
        // TODO: Use attributes map if needed for local generation rules

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
            keyGen.init(keyLength, random); // Use SecureRandom
            SecretKey secretKey = keyGen.generateKey();
            String uniqueID = UUID.randomUUID().toString();

            keyStore.put(uniqueID, secretKey);
            log.info("Successfully generated and stored key locally with ID: {}", uniqueID);
            log.info("Current keyStore size: {}, contents: {}", keyStore.size(), keyStore.keySet());

            return uniqueID;
        } catch (NoSuchAlgorithmException e) {
            log.error("Algorithm {} not supported by JCA KeyGenerator.", algorithm, e);
            throw new KmipException("Unsupported cryptographic algorithm: " + algorithm, e);
        } catch (Exception e) { // Catch other potential errors like InvalidKeyException
             log.error("Error generating key locally with algorithm {} and length {}.", algorithm, keyLength, e);
             throw new KmipException("Failed to generate key locally", e);
        }
    }

    @Override
    public Optional<SecretKey> getSymmetricKey(String uniqueID) {
        log.info("Attempting to retrieve key with ID: {}", uniqueID);

        // Log all available keys for debugging
        log.info("Available keys in store: {}", keyStore.keySet());

        SecretKey key = keyStore.get(uniqueID);
        if (key == null) {
            log.warn("Key with ID {} not found in key store", uniqueID);
        } else {
            log.info("Successfully retrieved key with ID: {}", uniqueID);
        }

        return Optional.ofNullable(key);
    }

    // TODO: Implement other methods (destroy, etc.) by manipulating the keyStore map
}