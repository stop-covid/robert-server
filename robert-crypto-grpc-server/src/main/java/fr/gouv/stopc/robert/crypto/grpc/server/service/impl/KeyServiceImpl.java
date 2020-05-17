package fr.gouv.stopc.robert.crypto.grpc.server.service.impl;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

import javax.crypto.KeyAgreement;

import fr.gouv.stopc.robert.crypto.grpc.server.model.ClientECDHBundle;
import fr.gouv.stopc.robert.crypto.grpc.server.service.IKeyService;
import fr.gouv.stopc.robert.server.common.utils.ByteUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeyServiceImpl implements IKeyService {

    @Override
    public Optional<ClientECDHBundle> generateECHKeysForEncryption(byte[] clientPublicKey) {

        if (ByteUtils.isEmpty(clientPublicKey)) {
            return Optional.empty();
        }

        try {
            // Generate ephemeral ECDH keypair
            KeyPairGenerator kpg;
            kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(256);
            KeyPair keyPair = kpg.generateKeyPair();
            byte[] serverPublicKey = keyPair.getPublic().getEncoded();

            KeyFactory kf = KeyFactory.getInstance("EC");
            X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(clientPublicKey);
            PublicKey otherPublicKey = kf.generatePublic(pkSpec);

            // Perform key agreement
            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(keyPair.getPrivate());
            ka.doPhase(otherPublicKey, true);

            // Read shared secret
            byte[] sharedSecret = ka.generateSecret();
            
            return Optional.of(ClientECDHBundle.builder()
                    .generatedSharedSecret(sharedSecret)
                    .serverPublicKey(serverPublicKey)
                    .build());

        } catch (NoSuchAlgorithmException | InvalidKeySpecException 
                | InvalidKeyException | IllegalStateException e) {

            log.error("Unable to generate ECDH Keys due to {}", e.getMessage());
        }

        return Optional.empty();
    }

}
