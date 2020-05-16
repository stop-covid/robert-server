package fr.gouv.stopc.robert.crypto.grpc.server.service;

public interface IClientKeyStorageService {
    /**
     * Create a random identifier and an associated random key
     * This information is stored permanently
     * @return
     */
    ClientIdentifierBundle createClientIdAndKey();

    /**
     * Get the key corresponding to the provided id
     * @param id
     * @return
     */
    ClientIdentifierBundle findKeyById(byte[] id);

    /**
     * Delete the record corresponding to the provided id, effectively destroying the key
     */
    void deleteClientId(byte[] id);
}
