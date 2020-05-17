package test.fr.gouv.stopc.robert.crypto.grpc.server.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fr.gouv.stopc.robert.crypto.grpc.server.model.ECDHKeys;
import fr.gouv.stopc.robert.crypto.grpc.server.service.impl.KeyServiceImpl;
import fr.gouv.stopc.robert.server.common.utils.ByteUtils;

@ExtendWith(SpringExtension.class)
public class KeyServiceImplTest {
    
    @InjectMocks
    KeyServiceImpl generationKeyService;

    @BeforeEach
    public void beforeEach() {
        assertNotNull(generationKeyService);
    }

    @Test
    public void testGenerateECHKeysForEncryptionWhenClientPublicKeyIsNull() {
 
        // Given
        byte [] clientPublicKey = null;
 
        // When
        Optional<ECDHKeys> keys = this.generationKeyService.generateECHKeysForEncryption(clientPublicKey);

        // Then
        assertFalse(keys.isPresent());
        
    }

    @Test
    public void testGenerateECHKeysForEncryptionWhenClientPublicKeyHasWrongSize() {

        // Given
        byte [] clientPublicKey = ByteUtils.generate(50);
 
        // When
        Optional<ECDHKeys> keys = this.generationKeyService.generateECHKeysForEncryption(clientPublicKey);

        // Then
        assertFalse(keys.isPresent());

    }

    @Test
    public void testGenerateECHKeysForEncryptionWhenClientPublicKeyHasRightSize() {

        // Given
        byte [] clientPublicKey = new byte [] { 48, 89, 48, 19, 6, 7, 42, -122, 72, -50, 61, 2, 1, 6, 8, 42, -122, 72, -50, 61, 3, 1, 7, 3, 66, 0, 
                                  4, -61, -16, -102, -1, 37, -72, 88, 17, -6, 19, 79, 57, 68, -93, 26, 102, 6, -59, -93, -79, -100, 123, -101,
                                  -113, -14, 87, 21, -52, -38, -30, 72, -26, -35, 67, -46, -115, 42, -112, 64, 45, -40, 82, 100, 
                                  115, 0, 80, -51, -30, 9, 29, 105, -103, -95, -33, -101, -111, 127, 22, 21, 71, 50, 91, -35, 11};
 
        // When
        Optional<ECDHKeys> keys = this.generationKeyService.generateECHKeysForEncryption(clientPublicKey);

        // Then
        assertTrue(keys.isPresent());
        assertNotNull(keys.get().getGeneratedSharedSecret());
        assertNotNull(keys.get().getServerPublicKey());
        assertEquals(clientPublicKey.length ,keys.get().getServerPublicKey().length);

    }
}
