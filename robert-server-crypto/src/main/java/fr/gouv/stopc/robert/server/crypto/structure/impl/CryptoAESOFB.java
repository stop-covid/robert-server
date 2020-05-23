package fr.gouv.stopc.robert.server.crypto.structure.impl;

import javax.crypto.spec.IvParameterSpec;

import fr.gouv.stopc.robert.server.crypto.structure.CryptoAES;
import fr.gouv.stopc.robert.server.crypto.exception.RobertServerCryptoException;
import lombok.extern.slf4j.Slf4j;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;

@Slf4j
public class CryptoAESOFB extends CryptoAES {

    private static final String AES_ENCRYPTION_CIPHER_SCHEME = "AES/OFB/NoPadding";

    public CryptoAESOFB(byte[] key) {
        super(key, AES_ENCRYPTION_CIPHER_SCHEME);
    }

    @Override
    public byte[] encrypt(byte[] plainText) throws RobertServerCryptoException {
        this.algorithmParameterSpec = new IvParameterSpec(plainText);
        return super.encrypt(plainText);
    }

    @Override
    public byte[] decrypt(byte[] cipherText) throws RobertServerCryptoException {
        if (Objects.isNull(this.algorithmParameterSpec)) {
            throw new RobertServerCryptoException("IV must be set before decryption");
        }
        return super.decrypt(cipherText);
    }

    public void setIvForDecryption(byte[] iv) {
        this.algorithmParameterSpec = new IvParameterSpec(iv);
    }
}
