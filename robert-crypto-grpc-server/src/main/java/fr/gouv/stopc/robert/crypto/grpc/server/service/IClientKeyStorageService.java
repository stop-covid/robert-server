package fr.gouv.stopc.robert.crypto.grpc.server.service;

import fr.gouv.stopc.robert.crypto.grpc.server.model.ClientIdentifierBundle;

import java.util.Optional;

public interface IClientKeyStorageService {
    /**
     * Create a random identifier to associated with provided  keys
     * This information is stored permanently
     * @return
     */
    Optional<ClientIdentifierBundle> createClientIdUsingKeys(byte[] kaMac, byte[] kaTuples);

    /**
     * Get the key corresponding to the provided id
     * @param id
     * @return
     */
    Optional<ClientIdentifierBundle> findKeyById(byte[] id);

    /**
     * Delete the record corresponding to the provided id, effectively destroying the key
     */
    void deleteClientId(byte[] id);
}
