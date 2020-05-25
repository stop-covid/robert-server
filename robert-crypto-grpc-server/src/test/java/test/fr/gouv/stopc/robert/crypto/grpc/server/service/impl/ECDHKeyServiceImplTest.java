package test.fr.gouv.stopc.robert.crypto.grpc.server.service.impl;

import java.util.Optional;

import fr.gouv.stopc.robert.crypto.grpc.server.storage.cryptographic.service.ICryptographicStorageService;
import fr.gouv.stopc.robert.crypto.grpc.server.storage.model.ClientIdentifierBundle;
import fr.gouv.stopc.robert.server.crypto.exception.RobertServerCryptoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fr.gouv.stopc.robert.crypto.grpc.server.service.IECDHKeyService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.impl.ECDHKeyServiceImpl;
import fr.gouv.stopc.robert.server.crypto.service.CryptoService;
import fr.gouv.stopc.robert.server.crypto.service.impl.CryptoServiceImpl;
import test.fr.gouv.stopc.robert.crypto.grpc.server.utils.CryptoTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ECDHKeyServiceImplTest {

    @InjectMocks
    private ECDHKeyServiceImpl keyService;

    @Mock
    private ICryptographicStorageService cryptographicStorageService;

    @BeforeEach
    public void beforeEach() {
        assertNotNull(this.keyService);
    }

    @Test
    public void testKeyDerivationFromClientPublicKeySucceeds() {

        // Given
        byte [] clientPublicKey = CryptoTestUtils.generateECDHPublicKey();
        Optional<ClientIdentifierBundle> clientIdentifierBundle = null;

        when(this.cryptographicStorageService.getServerKeyPair())
                .thenReturn(Optional.ofNullable(CryptoTestUtils.generateECDHKeyPair()));

        try {
            // When
            clientIdentifierBundle = this.keyService.deriveKeysFromClientPublicKey(clientPublicKey);
        } catch (RobertServerCryptoException e) {
            fail("Should not happen");
        }

        // Then
        assertTrue(clientIdentifierBundle.isPresent());
        assertNotNull(clientIdentifierBundle.get().getKeyForTuples());
        assertNotNull(clientIdentifierBundle.get().getKeyForMac());
    }
}
