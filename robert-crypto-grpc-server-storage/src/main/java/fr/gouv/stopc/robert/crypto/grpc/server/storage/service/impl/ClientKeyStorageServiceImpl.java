package fr.gouv.stopc.robert.crypto.grpc.server.storage.service.impl;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import fr.gouv.stopc.robert.crypto.grpc.server.storage.exception.RobertServerStorageException;
import org.bson.internal.Base64;
import org.springframework.stereotype.Service;

import fr.gouv.stopc.robert.crypto.grpc.server.storage.cryptographic.service.ICryptographicStorageService;
import fr.gouv.stopc.robert.crypto.grpc.server.storage.database.model.ClientIdentifier;
import fr.gouv.stopc.robert.crypto.grpc.server.storage.database.repository.ClientIdentiferRepository;
import fr.gouv.stopc.robert.crypto.grpc.server.storage.model.ClientIdentifierBundle;
import fr.gouv.stopc.robert.crypto.grpc.server.storage.service.IClientKeyStorageService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClientKeyStorageServiceImpl implements IClientKeyStorageService {

    private final static int MAX_ID_CREATION_ATTEMPTS = 10;

    private ICryptographicStorageService cryptographicStorageService;

    private ClientIdentiferRepository clientIdentifierRepository;

    @Inject
    public ClientKeyStorageServiceImpl(final ICryptographicStorageService cryptographicStorageService,
            final ClientIdentiferRepository clientIdentifierRepository) {

        this.cryptographicStorageService = cryptographicStorageService;
        this.clientIdentifierRepository = clientIdentifierRepository;

//        ClientIdentifier c = ClientIdentifier.builder()
//                .idA(Base64.encode(generateKey(5)))
//                .keyForMac(Base64.encode(generateRandomKey()))
//                .keyForTuples(Base64.encode(generateRandomKey()))
//                .build();
//        log.info("Trying to save the client identifier : {}", c);
//        c = this.clientIdentifierRepository.saveAndFlush(c);
//        this.clientIdentifierRepository.delete(c);
    }

    private byte[] generateRandomIdentifier() throws RobertServerStorageException {
        byte[] id;
        int i = 0;
        do {
            id = generateKey(5);
            i++;
        } while (this.clientIdentifierRepository.findByIdA(Base64.encode(id)).isPresent() && i < MAX_ID_CREATION_ATTEMPTS);

        if (MAX_ID_CREATION_ATTEMPTS == i) {
            throw new RobertServerStorageException(
                    String.format("Could not generate an id within max attempts %s", MAX_ID_CREATION_ATTEMPTS));
        }

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
    public Optional<ClientIdentifierBundle> createClientIdUsingKeys(byte[] keyForMac, byte[] keyForTuples) {
        try {
            log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            byte[] id = generateRandomIdentifier();
            log.info("//////~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            if (Objects.isNull(keyForMac)) {
                log.error("Provided key for mac is null");
                return Optional.empty();
            }

            if (Objects.isNull(keyForTuples)) {
                log.error("Provided key for tuples is null");
                return Optional.empty();
            }

            byte[] encryptedKeyForMac = this.performEncryption(Cipher.ENCRYPT_MODE, keyForMac, this.cryptographicStorageService.getKeyForEncryptingKeys());

            if (Objects.isNull(encryptedKeyForMac)) {
                log.error("The encrypted key for mac is null");
                return Optional.empty();
            }

            byte[] encryptedKeyForTuples = this.performEncryption(Cipher.ENCRYPT_MODE, keyForTuples, this.cryptographicStorageService.getKeyForEncryptingKeys());

            if (Objects.isNull(encryptedKeyForTuples)) {
                log.error("The encrypted key for tuples is null");
                return Optional.empty();
            }

            ClientIdentifier c = ClientIdentifier.builder()
                    .idA(Base64.encode(id))
                    .keyForMac(Base64.encode(encryptedKeyForMac))
                    .keyForTuples(Base64.encode(encryptedKeyForTuples))
                    .build();
            log.info("Trying to save the client identifier : {}", c);
            this.clientIdentifierRepository.saveAndFlush(c);
            log.info("Saving the client identifier");
            return Optional.of(ClientIdentifierBundle.builder()
                    .id(id)
                    .keyForMac(keyForMac)
                    .keyForTuples(keyForTuples)
                    .build());
        } catch (RobertServerStorageException e) {
            log.error("Storage error when creating registration");
        }
        return Optional.empty();
    }

//    @Override
//    public Optional<ClientIdentifierBundle> createClientIdAndKey() {
//        log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//        byte[] id = generateRandomIdentifier();
//        byte[] key = generateRandomKey();
//        byte[] keyForTuples = generateRandomKey();
//        log.info("//////~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//        if(Objects.isNull(this.cryptographicStorageService.getKeyForEncryptingKeys())) {
//            log.error("The server private key is Null");
//            return Optional.empty();
//        }
//
//        byte[] encryptedKey =  this.performEncryption(Cipher.ENCRYPT_MODE, key, this.cryptographicStorageService.getKeyForEncryptingKeys());
//
//        if (Objects.isNull(encryptedKey)) {
//            log.error("The encrypted key  is Null");
//            return Optional.empty();
//        }
//
//        byte[] encryptedKeyForTuples =  this.performEncryption(Cipher.ENCRYPT_MODE, key, this.cryptographicStorageService.getKeyForEncryptingKeys());
//
//        if (Objects.isNull(encryptedKeyForTuples)) {
//            log.error("The encrypted key for tuples is Null");
//            return Optional.empty();
//        }
//
//        ClientIdentifier c = ClientIdentifier.builder()
//                .idA(Base64.encode(id))
//                .key(Base64.encode(encryptedKey))
//                .keyForTuples(Base64.encode(encryptedKeyForTuples))
//                .build();
//        log.info("Trying to save the client identifier : {}", c);
//        this.clientIdentifierRepository.saveAndFlush(c);
//        log.info("Saving the client identifier");
//        return Optional.of(ClientIdentifierBundle.builder()
//                .id(id)
//                .key(key)
//                .keyForTuples(keyForTuples)
//                .build());
//    }

    @Override
    public Optional<ClientIdentifierBundle> findKeyById(byte[] id) {
        return this.clientIdentifierRepository.findByIdA(Base64.encode(id))
                .map(client -> {

                    byte[] secret = this.cryptographicStorageService.getKeyForEncryptingKeys();
                    if(Objects.isNull(secret)) {
                        log.error("The secret is null to decrypt the client");
                        return null;
                    }

                    byte[] decryptedKeyForMac = this.performEncryption(Cipher.DECRYPT_MODE, Base64.decode(client.getKeyForMac()),
                            secret);

                    if(Objects.isNull(decryptedKeyForMac)) {
                        log.error("The decrypted client key is null.");
                        return null;
                    }

                    byte[] decryptedKeyForTuples = this.performEncryption(Cipher.DECRYPT_MODE, Base64.decode(client.getKeyForTuples()),
                            secret);

                    if(Objects.isNull(decryptedKeyForTuples)) {
                        log.error("The decrypted client key for tuples is null.");
                        return null;
                    }

                    return ClientIdentifierBundle.builder()
                            .id(Arrays.copyOf(id, id.length))
                            .keyForMac(Arrays.copyOf(decryptedKeyForMac, decryptedKeyForMac.length))
                            .keyForTuples(Arrays.copyOf(decryptedKeyForTuples, decryptedKeyForTuples.length))
                            .build();
                });

    }

    @Override
    public void deleteClientId(byte[] id) {
        this.clientIdentifierRepository.findByIdA(Base64.encode(id)).ifPresent(this.clientIdentifierRepository::delete);
    }

    // TODO: use AES256-GCM?
    private byte[] performEncryption(int mode, byte[] toEncrypt, byte[] key) {

        try {
//            log.info("To Encrypt = {}",toEncrypt);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//            log.info("KEY encoded = {}",  key);
            SecretKeySpec secret = new SecretKeySpec(key, "AES");
            cipher.init(mode, secret);

            byte[] encrypted = cipher.doFinal(toEncrypt);
//            log.info("Encrypted data = {}", encrypted);
//            
//            cipher.init(Cipher.DECRYPT_MODE, secret);
//            
//            log.info("Decrypted data = {}", cipher.doFinal(encrypted));

            return encrypted;
        } catch (IllegalArgumentException | InvalidKeyException | NoSuchAlgorithmException
                | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {

            log.error("An expected error occurred when trying to perform encryption operation with the mode {} due to {}", mode, e.getMessage());
            e.printStackTrace();
        } 
        return null;
    }


}
