package fr.gouv.stopc.robertserver.crypto.structures;

import java.security.InvalidKeyException;

import fr.gouv.stopc.robertserver.crypto.exception.RobertServerCryptoException;

public abstract class CryptoMacStructureAbstract implements ICryptoStructure, IMacStructure {

    /**
     * @param payloadToEncrypt payload to encrypt
     * @return the arguments[0] encrypted with TripleDES algo
     * @throws RobertServerCryptoException 
     */
    @Override
    public byte[] encrypt(byte[] payloadToEncrypt) throws RobertServerCryptoException {
        try {
            this.getMac().init(this.getSecretKey());

            return this.getMac().doFinal(payloadToEncrypt);
        } catch (InvalidKeyException | IllegalStateException e) {
            throw new RobertServerCryptoException(e.getMessage());
        }
    }

}
