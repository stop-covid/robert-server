package fr.gouv.stopc.robert.crypto.grpc.server.storage.cryptographic.service;

import java.security.KeyPair;
import java.security.Provider;
import java.util.Map;
import java.util.Optional;

public interface ICryptographicStorageService {

    void init(String password, String configFile);
 
    boolean contains(String alias);
 
    void delete(String alias);
    
    void store(String alias, String secretKey);
 
    byte[] getServerKey(int epochId, long serviceTimeStart);

    byte[][] getServerKeys(int epochId, long serviceTimeStart, int nbDays);
 
    byte[] getEntry(String alias);

    void addKeys(String serverPublicKey, String serverPrivateKey);

    Optional<KeyPair> getServerKeyPair();

    Provider getProvider();

    byte[] getKeyForEncryptingKeys();
}
