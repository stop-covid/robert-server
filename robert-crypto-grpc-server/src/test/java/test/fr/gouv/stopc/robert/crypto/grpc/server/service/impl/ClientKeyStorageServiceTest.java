package test.fr.gouv.stopc.robert.crypto.grpc.server.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fr.gouv.stopc.robert.crypto.grpc.server.model.ClientIdentifierBundle;
import fr.gouv.stopc.robert.crypto.grpc.server.service.impl.ClientKeyStorageService;

@ExtendWith(SpringExtension.class)
public class ClientKeyStorageServiceTest {
    private ClientKeyStorageService clientKeyStorageService;

    @BeforeEach
    void beforeEach() {
        this.clientKeyStorageService = new ClientKeyStorageService();
    }

    @Test
    public void testCreateIdAndKeySucceeds() {
        ClientIdentifierBundle bundle = this.clientKeyStorageService.createClientIdAndKey();
        assertNotNull(bundle.getId());
        assertNotNull(bundle.getKey());
        assertEquals(5, bundle.getId().length);
        assertEquals(32, bundle.getKey().length);
    }

    @Test
    public void testFindIdSucceeds() {
        ClientIdentifierBundle bundle = this.clientKeyStorageService.createClientIdAndKey();

        byte[] idCopy = Arrays.copyOf(bundle.getId(), bundle.getId().length);
        ClientIdentifierBundle found = this.clientKeyStorageService.findKeyById(idCopy);
        assertNotNull(found);
        assertTrue(Arrays.equals(found.getId(), bundle.getId()));
        assertTrue(Arrays.equals(found.getKey(), bundle.getKey()));
    }

    @Test
    public void testFindNonExistentIdFails() {
        ClientIdentifierBundle bundle = this.clientKeyStorageService.createClientIdAndKey();
        byte[] id = bundle.getId();
        id[3] = (byte)(id[3] + 1);
        ClientIdentifierBundle found = this.clientKeyStorageService.findKeyById(id);
        assertNull(found);
    }

    @Test
    public void testDeleteClientIdSucceeds() {
        ClientIdentifierBundle bundle = this.clientKeyStorageService.createClientIdAndKey();
        this.clientKeyStorageService.deleteClientId(bundle.getId());
        assertNull(this.clientKeyStorageService.findKeyById(bundle.getId()));
    }
}
