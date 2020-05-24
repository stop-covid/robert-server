package fr.gouv.stopc.robert.crypto.grpc.server.service.impl;

import fr.gouv.stopc.robert.crypto.grpc.server.model.ClientIdentifierBundle;
import fr.gouv.stopc.robert.crypto.grpc.server.service.IClientKeyStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

@Service
@Slf4j
public class ClientKeyStorageService implements IClientKeyStorageService {

    private final HashMap<ByteArray, ClientIdentifierBundle> idKeyHashMap = new HashMap<>();

    private final static int MAX_ID_CREATION_ATTEMPTS = 10;

    private byte[] generateRandomIdentifier() {
        byte[] id;
        int i = 0;
        do {
            id = generateKey(5);
            i++;
        } while (this.idKeyHashMap.containsKey(id) && i < MAX_ID_CREATION_ATTEMPTS);
        return i == MAX_ID_CREATION_ATTEMPTS ? null : id;
    }

    public byte [] generateRandomKey() {
        byte [] ka = null;

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");

            //Creating a SecureRandom object
            SecureRandom secRandom = new SecureRandom();

            //Initializing the KeyGenerator
            keyGen.init(secRandom);

            //Creating/Generating a key
            Key key = keyGen.generateKey();
            ka = key.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not generate 256-bit key");
        }
        return ka;
    }

    private byte[] generateKey(final int nbOfbytes) {
        byte[] rndBytes = new byte[nbOfbytes];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(rndBytes);
        return rndBytes;
    }

    @Override
    public Optional<ClientIdentifierBundle> createClientIdUsingKeys(byte[] kaMac, byte[] kaTuples) {
        byte[] id = generateRandomIdentifier();

        ClientIdentifierBundle clientBundle = ClientIdentifierBundle.builder()
                .id(id)
                .keyMac(kaMac)
                .keyTuples(kaTuples)
                .build();
        this.idKeyHashMap.put(new ByteArray(id), clientBundle);
        return Optional.of(clientBundle);
    }

    @Override
    public Optional<ClientIdentifierBundle> findKeyById(byte[] id) {
        ClientIdentifierBundle bundle = this.idKeyHashMap.get(new ByteArray(id));
        if (Objects.isNull(bundle)) {
            return Optional.empty();
        }

        return Optional.of(bundle);
    }

    @Override
    public void deleteClientId(byte[] id) {
        this.idKeyHashMap.remove(new ByteArray(id));
    }

    private class ByteArray {
        public final byte[] bytes;
        public ByteArray(byte[] bytes) {
            this.bytes = bytes;
        }
        @Override
        public boolean equals(Object rhs) {
            return rhs != null && rhs instanceof ByteArray
                    && Arrays.equals(bytes, ((ByteArray)rhs).bytes);
        }
        @Override
        public int hashCode() {
            return Arrays.hashCode(bytes);
        }
    }
}
