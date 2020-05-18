package fr.gouv.stopc.robert.crypto.grpc.server.service;

import java.util.Optional;

import fr.gouv.stopc.robert.crypto.grpc.server.model.ServerECDHBundle;

public interface IECDHKeyService {
    
    Optional<ServerECDHBundle> generateECHDKeysForEncryption(byte[] clientPublicKey);
}
