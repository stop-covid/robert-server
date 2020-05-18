package test.fr.gouv.stopc.robert.crypto.grpc.server.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECGenParameterSpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CryptoTestUtils {

    private  CryptoTestUtils() {
        throw new AssertionError();
    }
 
    public static byte[] generateECDHPublicKey() {
        try {
            // Generate ephemeral ECDH keypair
            KeyPairGenerator kpg;
            kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = kpg.generateKeyPair();

            return keyPair.getPublic().getEncoded();

        } catch (NoSuchAlgorithmException | IllegalStateException | InvalidAlgorithmParameterException e) {
            log.error("Unable to generate ECDH public key", e.getMessage());
        }

        return null;
    }

}
