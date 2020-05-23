package fr.gouv.stopc.robert.server.crypto.structure.impl;

import fr.gouv.stopc.robert.server.crypto.exception.RobertServerCryptoException;
import fr.gouv.stopc.robert.server.crypto.structure.CryptoAES;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

public class CryptoAESGCM extends CryptoAES {

    private static final String AES_ENCRYPTION_CIPHER_SCHEME = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;

    /**
     * @param key to be used for cipher
     */
    public CryptoAESGCM(byte[] key) {
        super(key, AES_ENCRYPTION_CIPHER_SCHEME);
    }

    @Override
    public byte[] encrypt(byte[] plainText) throws RobertServerCryptoException {
        return super.encrypt(plainText);
    }

    @Override
    public byte[] decrypt(byte[] cipherText) throws RobertServerCryptoException {
        this.algorithmParameterSpec = new GCMParameterSpec(128, cipherText, 0, IV_LENGTH);
        byte[] toDecrypt = new byte[cipherText.length - IV_LENGTH];
        System.arraycopy(cipherText, IV_LENGTH, toDecrypt, 0, cipherText.length - IV_LENGTH);
        return super.decrypt(toDecrypt);
    }
}
