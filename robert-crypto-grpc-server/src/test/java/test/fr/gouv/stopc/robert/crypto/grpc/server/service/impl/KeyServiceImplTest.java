package test.fr.gouv.stopc.robert.crypto.grpc.server.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fr.gouv.stopc.robert.crypto.grpc.server.model.ClientECDHBundle;
import fr.gouv.stopc.robert.crypto.grpc.server.service.IKeyService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.impl.KeyServiceImpl;
import fr.gouv.stopc.robert.server.common.utils.ByteUtils;
import fr.gouv.stopc.robert.server.crypto.service.CryptoService;
import fr.gouv.stopc.robert.server.crypto.service.impl.CryptoServiceImpl;

@ExtendWith(SpringExtension.class)
public class KeyServiceImplTest {
    
    
    IKeyService keyService = new KeyServiceImpl();
    
    CryptoService cryptoService = new CryptoServiceImpl();

    @BeforeEach
    public void beforeEach() {
        assertNotNull(keyService);
    }

    @Test
    public void testGenerateECHKeysForEncryptionWhenClientPublicKeyIsNull() {
 
        // Given
        byte [] clientPublicKey = null;
 
        // When
        Optional<ClientECDHBundle> keys = this.keyService.generateECHKeysForEncryption(clientPublicKey);

        // Then
        assertFalse(keys.isPresent());
        
    }

    @Test
    public void testGenerateECHKeysForEncryptionWhenClientPublicKeyHasWrongSize() {

        // Given
        byte [] clientPublicKey = ByteUtils.generate(50);
 
        // When
        Optional<ClientECDHBundle> keys = this.keyService.generateECHKeysForEncryption(clientPublicKey);

        // Then
        assertFalse(keys.isPresent());

    }

    @Test
    public void testGenerateECHKeysForEncryptionWhenClientPublicKeyHasRightSize() {

        // Given
        byte [] clientPublicKey = this.cryptoService.generateECDHPublicKey();
 
        // When
        Optional<ClientECDHBundle> keys = this.keyService.generateECHKeysForEncryption(clientPublicKey);

        // Then
        assertTrue(keys.isPresent());
        assertNotNull(keys.get().getGeneratedSharedSecret());
        assertNotNull(keys.get().getServerPublicKey());
        assertEquals(clientPublicKey.length ,keys.get().getServerPublicKey().length);

    }
}
