package fr.gouv.stopc.robert.crypto.grpc.server.service;

import java.util.Optional;

import fr.gouv.stopc.robert.crypto.grpc.server.model.ClientECDHBundle;

public interface IKeyService {
    
    Optional<ClientECDHBundle> generateECHDKeysForEncryption(byte[] clientPublicKey);
}
