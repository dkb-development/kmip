package com.kmip.server.service;

import com.kmip.server.core.exception.KmipException;

import javax.crypto.SecretKey;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for managing cryptographic keys.
 * Implementations could use external KMS or local storage.
 */
public interface KeyManagementService {

    /**
     * Creates a new symmetric key.
     *
     * @param algorithm JCA algorithm name (e.g., "AES").
     * @param keyLength Key length in bits.
     * @param attributes Optional map of KMIP attributes associated with the key request.
     * @return The unique identifier (UUID or KMS ID) of the created key.
     * @throws KmipException If key creation fails.
     */
    String createSymmetricKey(String algorithm, int keyLength, Map<String, Object> attributes) throws KmipException;

    /**
     * Retrieves a symmetric key by its unique ID.
     *
     * @param uniqueID The unique identifier of the key.
     * @return An Optional containing the SecretKey if found.
     */
    Optional<SecretKey> getSymmetricKey(String uniqueID);

    // TODO: Add methods for other operations (destroy, rotate, getAttributes etc.)
}