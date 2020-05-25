package fr.gouv.stopc.robert.crypto.grpc.server.storage.service;

import java.util.Optional;

import fr.gouv.stopc.robert.crypto.grpc.server.storage.model.ClientIdentifierBundle;

public interface IClientKeyStorageService {

    /**
     * Create a random identifier and an associated random key
     * This information is stored permanently
     * @return
     */
    Optional<ClientIdentifierBundle> createClientIdAndKey();

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
