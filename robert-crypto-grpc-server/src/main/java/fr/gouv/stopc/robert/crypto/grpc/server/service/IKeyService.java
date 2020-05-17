package fr.gouv.stopc.robert.crypto.grpc.server.service;

import java.util.Optional;

import fr.gouv.stopc.robert.crypto.grpc.server.model.ECDHKeys;

public interface IKeyService {
    
    Optional<ECDHKeys> generateECHKeysForEncryption(byte[] clientPublicKey);
}
