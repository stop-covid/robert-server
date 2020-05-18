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
import fr.gouv.stopc.robert.crypto.grpc.server.service.IECDHKeyService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.impl.ECDHKeyServiceImpl;
import fr.gouv.stopc.robert.server.common.utils.ByteUtils;
import fr.gouv.stopc.robert.server.crypto.service.CryptoService;
import fr.gouv.stopc.robert.server.crypto.service.impl.CryptoServiceImpl;
import test.fr.gouv.stopc.robert.crypto.grpc.server.utils.CryptoTestUtils;

@ExtendWith(SpringExtension.class)
public class ECDHKeyServiceImplTest {
    
    
    IECDHKeyService keyService = new ECDHKeyServiceImpl();
    
    CryptoService cryptoService = new CryptoServiceImpl();

    @BeforeEach
    public void beforeEach() {
        assertNotNull(keyService);
    }

    @Test
    public void testGenerateECDHKeysForEncryptionWhenClientPublicKeyIsNullFails() {
 
        // Given
        byte [] clientPublicKey = null;
 
        // When
        Optional<ClientECDHBundle> keys = this.keyService.generateECHDKeysForEncryption(clientPublicKey);

        // Then
        assertFalse(keys.isPresent());
        
    }

    @Test
    public void testGenerateECDHKeysForEncryptionWhenClientPublicKeyHasWrongSizeFails() {

        // Given
        byte [] clientPublicKey = ByteUtils.generateRandom(50);
 
        // When
        Optional<ClientECDHBundle> keys = this.keyService.generateECHDKeysForEncryption(clientPublicKey);

        // Then
        assertFalse(keys.isPresent());

    }

    @Test
    public void testGenerateECDHKeysForEncryptionWhenClientPublicKeyHasRightSizeSucceeds() {

        // Given
        byte [] clientPublicKey = CryptoTestUtils.generateECDHPublicKey();

        // When
        Optional<ClientECDHBundle> keys = this.keyService.generateECHDKeysForEncryption(clientPublicKey);

        // Then
        assertTrue(keys.isPresent());
        assertNotNull(keys.get().getGeneratedSharedSecret());
        assertNotNull(keys.get().getServerPublicKey());
        assertEquals(clientPublicKey.length ,keys.get().getServerPublicKey().length);

    }
}
