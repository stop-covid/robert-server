package fr.gouv.stopc.robert.crypto.grpc.server.service.impl;

import fr.gouv.stopc.robert.crypto.grpc.server.service.ClientIdentifierBundle;
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

    private final HashMap<ByteArray, byte[]> idKeyHashMap = new HashMap<>();

    private byte[] generateRandomIdentifier() {
        byte[] id;
        do {
            id = generateKey(5);
        } while (this.idKeyHashMap.containsKey(id));
        return id;
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
    public ClientIdentifierBundle createClientIdAndKey() {
        byte[] id = generateRandomIdentifier();
        byte[] key = generateRandomKey();

        this.idKeyHashMap.put(new ByteArray(id), key);
        return new ClientIdentifierBundle(id, key);
    }

    @Override
    public ClientIdentifierBundle findKeyById(byte[] id) {
        byte[] key = this.idKeyHashMap.get(new ByteArray(id));
        if (Objects.isNull(key)) {
            return null;
        }

        return new ClientIdentifierBundle().builder()
                .id(Arrays.copyOf(id, id.length))
                .key(Arrays.copyOf(key, key.length))
                .build();
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
