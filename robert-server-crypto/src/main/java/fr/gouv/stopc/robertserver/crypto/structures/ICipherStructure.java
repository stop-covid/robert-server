package fr.gouv.stopc.robertserver.crypto.structures;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

public interface ICipherStructure {

    Cipher getCipher();

    /**
     * IV Used in Cipher initialization.
     * @return
     */
    IvParameterSpec getIv();
}
