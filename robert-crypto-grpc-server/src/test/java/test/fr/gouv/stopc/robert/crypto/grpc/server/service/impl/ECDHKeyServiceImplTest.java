package test.fr.gouv.stopc.robert.crypto.grpc.server.service.impl;

import java.util.Optional;

import fr.gouv.stopc.robert.crypto.grpc.server.model.ClientIdentifierBundle;
import fr.gouv.stopc.robert.server.crypto.exception.RobertServerCryptoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fr.gouv.stopc.robert.crypto.grpc.server.service.IECDHKeyService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.impl.ECDHKeyServiceImpl;
import fr.gouv.stopc.robert.server.common.utils.ByteUtils;
import fr.gouv.stopc.robert.server.crypto.service.CryptoService;
import fr.gouv.stopc.robert.server.crypto.service.impl.CryptoServiceImpl;
import test.fr.gouv.stopc.robert.crypto.grpc.server.utils.CryptoTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class ECDHKeyServiceImplTest {
    
    
    IECDHKeyService keyService = new ECDHKeyServiceImpl();
    
    CryptoService cryptoService = new CryptoServiceImpl();

    @BeforeEach
    public void beforeEach() {
        assertNotNull(keyService);
    }

    @Test
    public void testKeyDerivationFromClientPublicKeySucceeds() {

        // Given
        byte [] clientPublicKey = CryptoTestUtils.generateECDHPublicKey();
        Optional<ClientIdentifierBundle> clientIdentifierBundle = null;

        try {
            // When
            clientIdentifierBundle = this.keyService.deriveKeysFromClientPublicKey(clientPublicKey);
        } catch (RobertServerCryptoException e) {
            fail("Should not happen");
        }

        // Then
        assertTrue(clientIdentifierBundle.isPresent());
        assertNotNull(clientIdentifierBundle.get().getKeyTuples());
        assertNotNull(clientIdentifierBundle.get().getKeyMac());
    }
}
