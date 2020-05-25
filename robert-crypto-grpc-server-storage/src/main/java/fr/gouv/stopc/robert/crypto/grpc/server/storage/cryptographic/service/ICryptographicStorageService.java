package fr.gouv.stopc.robert.crypto.grpc.server.storage.cryptographic.service;

import java.security.Key;
import java.security.KeyPair;
import java.security.Provider;
import java.util.Optional;

public interface ICryptographicStorageService {

    void init(String password, String configFile);
 
    boolean contains(String alias);
 
    void delete(String alias);
    
    void store(String alias, String secretKey);
 
    byte[] getEntry(String alias);

    void addKeys(String serverPublicKey, String serverPrivateKey);

    Optional<KeyPair> getServerKeyPair();

    Provider getProvider();

    byte[] getKeyForEncryptingKeys();
}
