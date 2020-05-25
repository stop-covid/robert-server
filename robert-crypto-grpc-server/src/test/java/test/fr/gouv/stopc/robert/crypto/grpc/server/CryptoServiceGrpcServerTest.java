package test.fr.gouv.stopc.robert.crypto.grpc.server;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.*;
import fr.gouv.stopc.robert.crypto.grpc.server.storage.cryptographic.service.ICryptographicStorageService;
import fr.gouv.stopc.robert.crypto.grpc.server.storage.cryptographic.service.IServerKeyStorageService;
import fr.gouv.stopc.robert.crypto.grpc.server.storage.cryptographic.service.impl.ServerKeyStorageServiceImpl;
import fr.gouv.stopc.robert.crypto.grpc.server.storage.model.ClientIdentifierBundle;
import fr.gouv.stopc.robert.server.common.DigestSaltEnum;
import fr.gouv.stopc.robert.server.common.utils.TimeUtils;
import fr.gouv.stopc.robert.server.crypto.exception.RobertServerCryptoException;
import fr.gouv.stopc.robert.server.crypto.structure.impl.CryptoAESGCM;
import fr.gouv.stopc.robert.server.crypto.structure.impl.CryptoHMACSHA256;
import fr.gouv.stopc.robert.server.crypto.structure.impl.CryptoSkinny64;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.protobuf.ByteString;

import fr.gouv.stopc.robert.crypto.grpc.server.CryptoServiceGrpcServer;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.CryptoGrpcServiceImplGrpc.CryptoGrpcServiceImplImplBase;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.CryptoGrpcServiceImplGrpc.CryptoGrpcServiceImplStub;

import fr.gouv.stopc.robert.crypto.grpc.server.storage.service.IClientKeyStorageService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.ICryptoServerConfigurationService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.impl.CryptoGrpcServiceBaseImpl;
import fr.gouv.stopc.robert.crypto.grpc.server.service.impl.CryptoServerConfigurationServiceImpl;
import fr.gouv.stopc.robert.crypto.grpc.server.service.impl.ECDHKeyServiceImpl;
import fr.gouv.stopc.robert.server.common.utils.ByteUtils;
import fr.gouv.stopc.robert.server.crypto.service.CryptoService;
import fr.gouv.stopc.robert.server.crypto.service.impl.CryptoServiceImpl;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import test.fr.gouv.stopc.robert.crypto.grpc.server.utils.CryptoTestUtils;

import javax.crypto.KeyGenerator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(SpringExtension.class)
class CryptoServiceGrpcServerTest {

    private final static String UNEXPECTED_FAILURE_MESSAGE = "Should not fail";
    private final static byte[] SERVER_COUNTRY_CODE = new byte[] { (byte) 0x33 };
    private final static int NUMBER_OF_BUNDLES = 4 * 4 * 24;

    final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private ManagedChannel inProcessChannel;

    private CryptoServiceGrpcServer server;

    private CryptoGrpcServiceImplImplBase service;

    private ICryptoServerConfigurationService serverConfigurationService;

    private CryptoService cryptoService;

    @Mock
    private IServerKeyStorageService serverKeyStorageService;

    @InjectMocks
    private ECDHKeyServiceImpl keyService;

    private IClientKeyStorageService clientStorageService;

    @Mock
    private ICryptographicStorageService cryptographicStorageService;

    private int currentEpochId;

    @BeforeEach
    void beforeEach() throws IOException {

        serverConfigurationService = new CryptoServerConfigurationServiceImpl();

        cryptoService = new CryptoServiceImpl();

        clientStorageService = new MockClientKeyStorageService();

        service = new CryptoGrpcServiceBaseImpl(serverConfigurationService,
                cryptoService,
                keyService,
                clientStorageService,
                serverKeyStorageService);

        when(this.cryptographicStorageService.getServerKeyPair())
                .thenReturn(Optional.ofNullable(CryptoTestUtils.generateECDHKeyPair()));

        byte[] keyToEncodeKeys = new byte[32];
        new SecureRandom().nextBytes(keyToEncodeKeys);
        when(this.cryptographicStorageService.getKeyForEncryptingKeys()).thenReturn(keyToEncodeKeys);

        String serverName = InProcessServerBuilder.generateName();

        server = new CryptoServiceGrpcServer(
                InProcessServerBuilder.forName(serverName)
                .directExecutor()
                , 0, service);
        server.start();
        inProcessChannel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());

        this.currentEpochId = TimeUtils.getCurrentEpochFrom(this.serverConfigurationService.getServiceTimeStart());
    }

    @AfterEach
    void tearDown() throws Exception {
        server.stop();
    }

    @Test
    void testCreateRegistrationSucceeds() {
        // Given
        CreateRegistrationRequest request = CreateRegistrationRequest
                .newBuilder()
                .setClientPublicKey(ByteString.copyFrom(CryptoTestUtils.generateECDHPublicKey()))
                .setFromEpochId(this.currentEpochId)
                .setNumberOfEpochBundles(NUMBER_OF_BUNDLES)
                .setServerCountryCode(ByteString.copyFrom(SERVER_COUNTRY_CODE))
                .build();

        ObserverExecutionResult res = new ObserverExecutionResult(false);
        CreateRegistrationResponse createRegistrationResponse =
                sendCryptoRequest(
                        request,
                        (stub, req, observer) -> stub.createRegistration(req, observer),
                        (t) -> fail(),
                        res);
        assertTrue(!res.isError());
        assertTrue(ByteUtils.isNotEmpty(createRegistrationResponse.getIdA().toByteArray()));
        byte[] tuples = createRegistrationResponse.getTuples().toByteArray();
        assertTrue(ByteUtils.isNotEmpty(tuples));
        CryptoAESGCM aesGcm = new CryptoAESGCM(
                this.clientStorageService.findKeyById(
                        createRegistrationResponse.getIdA().toByteArray()).get().getKeyForTuples());
        try {
            byte[] decryptedTuples = aesGcm.decrypt(tuples);
            ObjectMapper objectMapper = new ObjectMapper();
            Collection<CryptoGrpcServiceBaseImpl.EphemeralTupleJson> decodedTuples = objectMapper.readValue(
                    decryptedTuples,
                    new TypeReference<Collection<CryptoGrpcServiceBaseImpl.EphemeralTupleJson>>(){});
            assertEquals(NUMBER_OF_BUNDLES, decodedTuples.size());
        } catch (RobertServerCryptoException | IOException e) {
            fail(UNEXPECTED_FAILURE_MESSAGE);
        }
    }

    @Test
    void testCreateRegistrationFakeClientPublicKeyFails() {
        byte[] fakeKey = new byte[32];
        new SecureRandom().nextBytes(fakeKey);

        CreateRegistrationRequest request = CreateRegistrationRequest
                .newBuilder()
                .setClientPublicKey(ByteString.copyFrom(fakeKey))
                .setFromEpochId(this.currentEpochId)
                .setNumberOfEpochBundles(NUMBER_OF_BUNDLES)
                .setServerCountryCode(ByteString.copyFrom(SERVER_COUNTRY_CODE))
                .build();

        ObserverExecutionResult res = new ObserverExecutionResult(false);
        CreateRegistrationResponse createRegistrationResponse =
                sendCryptoRequest(
                        request,
                        (stub, req, observer) -> stub.createRegistration(req, observer),
                        (t) -> log.error(t.getMessage()),
                        res);

        assertNull(createRegistrationResponse);
        assertTrue(res.isError());
    }

    @Test
    void testCreateRegistrationClientPublicKeyNotECDHFails() {
        CreateRegistrationRequest request = CreateRegistrationRequest
                .newBuilder()
                .setClientPublicKey(ByteString.copyFrom(CryptoTestUtils.generateDHPublicKey()))
                .setFromEpochId(this.currentEpochId)
                .setNumberOfEpochBundles(NUMBER_OF_BUNDLES)
                .setServerCountryCode(ByteString.copyFrom(SERVER_COUNTRY_CODE))
                .build();

        ObserverExecutionResult res = new ObserverExecutionResult(false);
        CreateRegistrationResponse createRegistrationResponse =
                sendCryptoRequest(
                        request,
                        (stub, req, observer) -> stub.createRegistration(req, observer),
                        (t) -> assertNotNull(t),
                        res);

        assertNull(createRegistrationResponse);
        assertTrue(res.isError());
    }

    @Test
    void testCreateRegistrationClientPublicKeyImproperECFails() {
        // Client public key generated with EC curve "secp256k1" instead of server's choice of "secp256*r*1"
        CreateRegistrationRequest request = CreateRegistrationRequest
                .newBuilder()
                .setClientPublicKey(ByteString.copyFrom(CryptoTestUtils.generateECDHPublicKey("secp256k1")))
                .setFromEpochId(this.currentEpochId)
                .setNumberOfEpochBundles(NUMBER_OF_BUNDLES)
                .setServerCountryCode(ByteString.copyFrom(SERVER_COUNTRY_CODE))
                .build();

        ObserverExecutionResult res = new ObserverExecutionResult(false);
        CreateRegistrationResponse createRegistrationResponse =
                sendCryptoRequest(
                        request,
                        (stub, req, observer) -> stub.createRegistration(req, observer),
                        (t) -> assertNotNull(t),
                        res);

        assertNull(createRegistrationResponse);
        assertTrue(res.isError());
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    static class AuthRequestBundle {
        private byte[] ebid;
        private int epochId;
        private long time;
        private byte[] mac;
        private DigestSaltEnum requestType;
        private byte[] serverKey;
    }

    private Optional<ClientIdentifierBundle> createId() {
        byte[] keyForMac = new byte[32];
        byte[] keyForTuples = new byte[32];

        return this.clientStorageService.createClientIdUsingKeys(keyForMac, keyForTuples);
    }

    private byte[] generateMac(byte[] ebid, int epochId, long time, byte[] keyForMac, DigestSaltEnum digestSalt) {
        byte[] digest = new byte[] { digestSalt.getValue() };
        byte[] toHash = new byte[digest.length + ebid.length + Integer.BYTES + Integer.BYTES];
        System.arraycopy(digest, 0, toHash, 0, digest.length);
        System.arraycopy(ebid, 0, toHash, digest.length, ebid.length);
        System.arraycopy(ByteUtils.intToBytes(epochId), 0, toHash, digest.length + ebid.length, Integer.BYTES);
        System.arraycopy(ByteUtils.longToBytes(time), 4, toHash, digest.length + ebid.length + Integer.BYTES, Integer.BYTES);

        try {
            CryptoHMACSHA256 hmacsha256 = new CryptoHMACSHA256(keyForMac);
            return hmacsha256.encrypt(toHash);
        } catch (RobertServerCryptoException e) {
            fail();
        }
        return null;
    }

    private byte[] generateEbid(byte[] id, int epochId, byte[] ks) {
        byte[] decryptedEbid = new byte[8];
        System.arraycopy(ByteUtils.intToBytes(epochId), 1, decryptedEbid, 0, Integer.BYTES - 1);
        System.arraycopy(id, 0, decryptedEbid, Integer.BYTES - 1, id.length);

        CryptoSkinny64 cryptoSkinny64 = new CryptoSkinny64(ks);
        byte[] encryptedEbid = null;

        try {
            encryptedEbid = cryptoSkinny64.encrypt(decryptedEbid);
        } catch (RobertServerCryptoException e) {
            fail();
        }

        return encryptedEbid;
    }

    private AuthRequestBundle generateAuthRequestBundle(byte[] id, byte[] keyForMac, DigestSaltEnum digestSalt) {
        long time = System.currentTimeMillis() / 1000 + 2208988800L;
        int epochId = TimeUtils.getNumberOfEpochsBetween(this.serverConfigurationService.getServiceTimeStart(), time);

        // Mock K_S
        byte[] ks = new byte[24];
        new SecureRandom().nextBytes(ks);
        byte[] ebid = generateEbid(id, epochId, ks);
        when(this.serverKeyStorageService.getServerKeyForEpoch(epochId)).thenReturn(ks);

        return new AuthRequestBundle().builder()
                .ebid(ebid)
                .epochId(epochId)
                .time(time)
                .mac(generateMac(ebid, epochId, time, keyForMac, digestSalt))
                .requestType(digestSalt)
                .serverKey(ks)
                .build();
    }

    @Test
    void testGetIdFromAuthRequestSucceeds() {
        Optional<ClientIdentifierBundle> clientIdentifierBundle = createId();
        AuthRequestBundle bundle = generateAuthRequestBundle(
                clientIdentifierBundle.get().getId(),
                clientIdentifierBundle.get().getKeyForMac(),
                DigestSaltEnum.UNREGISTER);

        // Given
        GetIdFromAuthRequest request = GetIdFromAuthRequest
                .newBuilder()
                .setEbid(ByteString.copyFrom(bundle.getEbid()))
                .setEpochId(bundle.getEpochId())
                .setTime(bundle.getTime())
                .setMac(ByteString.copyFrom(bundle.getMac()))
                .setRequestType(bundle.getRequestType().getValue()) // Select a request type
                .build();

        ObserverExecutionResult res = new ObserverExecutionResult(false);
        GetIdFromAuthResponse response =
                sendCryptoRequest(
                        request,
                        (stub, req, observer) -> stub.getIdFromAuth(req, observer),
                        (t) -> fail(),
                        res);
        assertTrue(!res.isError());
        assertTrue(ByteUtils.isNotEmpty(response.getIdA().toByteArray()));

        //TODO
    }

    @Test
    void testGetIdFromAuthRequestFakeEBIDFails() {

    }

    @Test
    void testGetIdFromAuthRequestBadMacFails() {

    }

    @Test
    void testGetIdFromAuthRequestEpochIdsDoNotMatchFails() {

    }

    @Test
    void testGetIdFromAuthRequestUnknownRequestTypeFails() {

    }

    @Test
    void testGetIdFromAuthRequestNegativeTimeFails() {

    }

    @Test
    void testDeleteIdSucceeds() {

    }

    @Test
    void testDeleteIdUnknownIdFails() {

    }

    @Test
    void testDeleteIdFakeEBIDFails() {

    }

    @Test
    void testDeleteIdBadMacFails() {

    }

    @Test
    void testDeleteIdEpochIdsDoNotMatchFails() {

    }

    @Test
    void testDeleteIdNegativeTimeFails() {

    }

    @Test
    void testGetIdFromStatusSucceeds() {

    }

    @Test
    void testGetIdFromStatusFakeEBIDFails() {

    }

    @Test
    void testGetIdFromStatusBadMacFails() {

    }

    @Test
    void testGetIdFromStatusEpochIdsDoNotMatchFails() {

    }

    @Test
    void testGetIdFromStatusNegativeTimeFails() {

    }

    @Test
    void testGetInfoFromHelloMessageSucceeds() {

    }

    @Test
    void testGetInfoFromHelloMessageBadMacFails() {

    }

    @Test
    void testGetInfoFromHelloMessageFakeEbidFails() {

    }

    @Test
    void testGetInfoFromHelloMessageEpochIdsDoNotMatchFails() {

    }

    @Test
    void testGetInfoFromHelloMessageNegativeTimeFails() {

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    static class ObserverExecutionResult {
        boolean error;
    }

    interface StubExecution<T,U> {
        void execute(CryptoGrpcServiceImplStub stub, T t, StreamObserver<U> u);
    }

    interface HandleError {
        void execute(Throwable t);
    }

    <T, U> U sendCryptoRequest(T request, StubExecution<T, U> stubExecution, HandleError handleError, ObserverExecutionResult res) {
        try {
            CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(this.inProcessChannel);

            final List<U> response = new ArrayList<>();

            final CountDownLatch latch = new CountDownLatch(1);

            StreamObserver<U> responseObserver =
                    new StreamObserver<U>() {
                        @Override
                        public void onNext(U value) {
                            response.add(value);
                        }

                        @Override
                        public void onError(Throwable t) {
                            handleError.execute(t);
                            res.setError(true);
                        }

                        @Override
                        public void onCompleted() {
                            latch.countDown();
                        }
                    };

            stubExecution.execute(stub, request, responseObserver);
            // When
            //stub.createRegistration(request, responseObserver);

            if (res.isError()) {
                return null;
            }

            // Then
            assertTrue(latch.await(1, TimeUnit.SECONDS));
            assertEquals(1, response.size());

            return response.get(0);
        } catch (InterruptedException e) {
            fail(UNEXPECTED_FAILURE_MESSAGE);
            return null;
        }
    }

    public class MockClientKeyStorageService implements IClientKeyStorageService {

    private final HashMap<ByteArray, ClientIdentifierBundle> idKeyHashMap = new HashMap<>();

    private final static int MAX_ID_CREATION_ATTEMPTS = 10;

    private byte[] generateRandomIdentifier() {
        byte[] id;
        int i = 0;
        do {
            id = generateKey(5);
            i++;
        } while (this.idKeyHashMap.containsKey(id) && i < MAX_ID_CREATION_ATTEMPTS);
        return i == MAX_ID_CREATION_ATTEMPTS ? null : id;
    }

    public byte [] generateRandomKey() {
        byte [] ka = null;

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");

            //Creating a SecureRandom object
            SecureRandom secRandom = new SecureRandom();

            //Initializing the KeyGenerator
            keyGen.init(secRandom);

            //Creating/Generating a key
            Key key = keyGen.generateKey();
            ka = key.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not generate 256-bit key");
        }
        return ka;
    }

    private byte[] generateKey(final int nbOfbytes) {
        byte[] rndBytes = new byte[nbOfbytes];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(rndBytes);
        return rndBytes;
    }

    @Override
    public Optional<ClientIdentifierBundle> createClientIdUsingKeys(byte[] kaMac, byte[] kaTuples) {
        byte[] id = generateRandomIdentifier();

        ClientIdentifierBundle clientBundle = ClientIdentifierBundle.builder()
                .id(id)
                .keyForMac(kaMac)
                .keyForTuples(kaTuples)
                .build();
        this.idKeyHashMap.put(new ByteArray(id), clientBundle);
        return Optional.of(clientBundle);
    }

    @Override
    public Optional<ClientIdentifierBundle> findKeyById(byte[] id) {
        ClientIdentifierBundle bundle = this.idKeyHashMap.get(new ByteArray(id));
        if (Objects.isNull(bundle)) {
            return Optional.empty();
        }

        return Optional.of(bundle);
    }

    @Override
    public void deleteClientId(byte[] id) {
        this.idKeyHashMap.remove(new ByteArray(id));
    }

    private class ByteArray {
        public final byte[] bytes;
        public ByteArray(byte[] bytes) {
            this.bytes = bytes;
        }
        @Override
        public boolean equals(Object rhs) {
            return rhs != null && rhs instanceof ByteArray
                    && Arrays.equals(bytes, ((ByteArray)rhs).bytes);
        }
        @Override
        public int hashCode() {
            return Arrays.hashCode(bytes);
        }
    }
}


//    @Test
//    public void testGenerateEBID() {
//        try {
//            // Given
//            GenerateEBIDRequest request = GenerateEBIDRequest
//                    .newBuilder()
//                    .setIdA(ByteString.copyFrom(ByteUtils.generateRandom(5))).build();
//
//            CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//            final List<EBIDResponse> response = new ArrayList<EBIDResponse>();
//
//            final CountDownLatch latch = new CountDownLatch(1);
//
//            StreamObserver<EBIDResponse> responseObserver =
//                    new StreamObserver<EBIDResponse>() {
//                @Override
//                public void onNext(EBIDResponse value) {
//                    response.add(value);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    fail();
//                }
//
//                @Override
//                public void onCompleted() {
//                    latch.countDown();
//                }
//            };
//
//            // When
//            stub.generateEBID(request, responseObserver);
//
//            // Then
//            assertTrue(latch.await(1, TimeUnit.SECONDS));
//            assertEquals(1, response.size());
//            assertTrue(ByteUtils.isNotEmpty(response.get(0).getEbid().toByteArray()));
//        } catch (InterruptedException e) {
//            fail(e.getMessage());
//        }
//    }
//
//    @Test
//    public void testDecryptEBID() {
//
//        try {
//            // Given
//            DecryptEBIDRequest request = DecryptEBIDRequest.newBuilder()
//                    .setEbid(ByteString.copyFrom(ByteUtils.generateRandom(8))).build();
//
//            CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//            List<EBIDResponse> response = new ArrayList<EBIDResponse>();
//
//            final CountDownLatch latch = new CountDownLatch(1);
//
//            StreamObserver<EBIDResponse> responseObserver =
//                    new StreamObserver<EBIDResponse>() {
//                @Override
//                public void onNext(EBIDResponse value) {
//                    response.add(value);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    fail();
//                }
//
//                @Override
//                public void onCompleted() {
//                    latch.countDown();
//                }
//            };
//
//            // When
//            stub.decryptEBID(request, responseObserver);
//
//            // Then
//            assertTrue(latch.await(1, TimeUnit.SECONDS));
//            assertEquals(1, response.size());
//            assertTrue(ByteUtils.isNotEmpty(response.get(0).getEbid().toByteArray()));
//        } catch (InterruptedException e) {
//            fail(e.getMessage());
//        }
//
//    }
//
//    @Test
//    public void testEncryptCountryCode() {
//
//        try {
//            // Given
//            EncryptCountryCodeRequest request = EncryptCountryCodeRequest.newBuilder()
//                    .setEbid(ByteString.copyFrom(ByteUtils.generateRandom(8)))
//                    .setCountryCode(ByteString.copyFrom(ByteUtils.generateRandom(1))).build();
//
//            CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//            List<EncryptCountryCodeResponse> response = new ArrayList<EncryptCountryCodeResponse>();
//
//            final CountDownLatch latch = new CountDownLatch(1);
//
//            StreamObserver<EncryptCountryCodeResponse> responseObserver =
//                    new StreamObserver<EncryptCountryCodeResponse>() {
//                @Override
//                public void onNext(EncryptCountryCodeResponse value) {
//                    response.add(value);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    fail();
//                }
//
//                @Override
//                public void onCompleted() {
//                    latch.countDown();
//                }
//            };
//
//            // When
//            stub.encryptCountryCode(request, responseObserver);
//
//            // Then
//            assertTrue(latch.await(1, TimeUnit.SECONDS));
//            assertEquals(1, response.size());
//            assertTrue(ByteUtils.isNotEmpty(response.get(0).getEncryptedCountryCode().toByteArray()));
//        } catch (InterruptedException e) {
//            fail(e.getMessage());
//        }
//
//
//    }
//
//    @Test
//    public void testDecryptCountryCode() {
//
//        // Then
//        try {
//            // Given
//            DecryptCountryCodeRequest request = DecryptCountryCodeRequest.newBuilder()
//                    .setEbid(ByteString.copyFrom(ByteUtils.generateRandom(8)))
//                    .setEncryptedCountryCode(ByteString.copyFrom(ByteUtils.generateRandom(1)))
//                    .build();
//
//            CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//            List<DecryptCountryCodeResponse> response = new ArrayList<DecryptCountryCodeResponse>();
//
//            final CountDownLatch latch = new CountDownLatch(1);
//
//
//            StreamObserver<DecryptCountryCodeResponse> responseObserver =
//                    new StreamObserver<DecryptCountryCodeResponse>() {
//                @Override
//                public void onNext(DecryptCountryCodeResponse value) {
//                    response.add(value);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    fail();
//                }
//
//                @Override
//                public void onCompleted() {
//                    latch.countDown();
//                }
//            };
//
//            // When
//            stub.decryptCountryCode(request, responseObserver);
//
//            // Then
//            assertEquals(1, response.size());
//            assertTrue(ByteUtils.isNotEmpty(response.get(0).getCountryCode().toByteArray()));
//            assertTrue(latch.await(1, TimeUnit.SECONDS));
//
//        } catch (InterruptedException e) {
//            fail(e.getMessage());
//        }
//    }
//
//    @Test
//    public void testGenerateMacHello() {
//
//        try {
//            // Given
//            MacHelloGenerationRequest request  = MacHelloGenerationRequest.newBuilder()
//                    .setKa(ByteString.copyFrom(ByteUtils.generateRandom(16)))
//                    .setHelloMessage(ByteString.copyFrom(ByteUtils.generateRandom(16)))
//                    .build();
//
//            CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//            List<MacHelloGenerationResponse> response = new ArrayList<>();
//
//            final CountDownLatch latch = new CountDownLatch(1);
//
//
//            StreamObserver<MacHelloGenerationResponse> responseObserver =
//                    new StreamObserver<MacHelloGenerationResponse>() {
//                @Override
//                public void onNext(MacHelloGenerationResponse value) {
//                    response.add(value);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    fail();
//                }
//
//                @Override
//                public void onCompleted() {
//                    latch.countDown();
//                }
//            };
//
//            // When
//            stub.generateMacHello(request, responseObserver);
//
//            // Then
//            assertTrue(latch.await(1, TimeUnit.SECONDS));
//            assertEquals(1, response.size());
//            assertTrue(ByteUtils.isNotEmpty(response.get(0).getMacHelloMessage().toByteArray()));
//
//        } catch (InterruptedException e) {
//            fail(e.getMessage());
//        }
//    }
//
//
//    @Test
//    public void testValidateMacHello() {
//
//        try {
//            // Given
//            MacHelloValidationRequest request = MacHelloValidationRequest.newBuilder()
//                    .setKa(ByteString.copyFrom(ByteUtils.generateRandom(16)))
//                    .setDataToValidate(ByteString.copyFrom(ByteUtils.generateRandom(16)))
//                    .build();
//
//            CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//            List<MacValidationResponse> response = new ArrayList<>();
//
//            final CountDownLatch latch = new CountDownLatch(1);
//
//
//            StreamObserver<MacValidationResponse> responseObserver =
//                    new StreamObserver<MacValidationResponse>() {
//                @Override
//                public void onNext(MacValidationResponse value) {
//                    response.add(value);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    fail();
//                }
//
//                @Override
//                public void onCompleted() {
//                    latch.countDown();
//                }
//            };
//
//            // When
//            stub.validateMacHello(request, responseObserver);
//
//            //
//            assertTrue(latch.await(1, TimeUnit.SECONDS));
//            assertEquals(1, response.size());
//            assertFalse(response.get(0).getIsValid());
//        } catch (InterruptedException e) {
//            fail(e.getMessage());
//        }
//    }
//
//    @Test
//    public void testValidateMacEsr() {
//
//        try {
//            // Given
//            MacEsrValidationRequest request = MacEsrValidationRequest.newBuilder()
//                    .setKa(ByteString.copyFrom(ByteUtils.generateRandom(16)))
//                    .setDataToValidate(ByteString.copyFrom(ByteUtils.generateRandom(12)))
//                    .setMacToMatchWith(ByteString.copyFrom(ByteUtils.generateRandom(16)))
//                    .build();
//
//            CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//            List<MacValidationResponse> response = new ArrayList<>();
//
//            final CountDownLatch latch = new CountDownLatch(1);
//
//
//            StreamObserver<MacValidationResponse> responseObserver =
//                    new StreamObserver<MacValidationResponse>() {
//                @Override
//                public void onNext(MacValidationResponse value) {
//                    response.add(value);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    fail();
//                }
//
//                @Override
//                public void onCompleted() {
//                    latch.countDown();
//                }
//            };
//
//            // When
//            stub.validateMacEsr(request, responseObserver);
//
//            //
//            assertTrue(latch.await(1, TimeUnit.SECONDS));
//            assertEquals(1, response.size());
//            assertFalse(response.get(0).getIsValid());
//        } catch (InterruptedException e) {
//            fail(e.getMessage());
//        }
//    }
//
//    @Test
//    public void testValidateMacForType() {
//
//        try {
//            // Given
//            MacValidationForTypeRequest request = MacValidationForTypeRequest.newBuilder()
//                    .setKa(ByteString.copyFrom(ByteUtils.generateRandom(16)))
//                    .setDataToValidate(ByteString.copyFrom(ByteUtils.generateRandom(12)))
//                    .setMacToMatchWith(ByteString.copyFrom(ByteUtils.generateRandom(16)))
//                    .setPrefixe(ByteString.copyFrom(new byte[] { DigestSaltEnum.UNREGISTER.getValue() }))
//                    .build();
//
//            CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//            List<MacValidationResponse> response = new ArrayList<>();
//
//            final CountDownLatch latch = new CountDownLatch(1);
//
//
//            StreamObserver<MacValidationResponse> responseObserver =
//                    new StreamObserver<MacValidationResponse>() {
//                @Override
//                public void onNext(MacValidationResponse value) {
//                    response.add(value);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    fail();
//                }
//
//                @Override
//                public void onCompleted() {
//                    latch.countDown();
//                }
//            };
//
//            // When
//            stub.validateMacForType(request, responseObserver);
//
//            //
//            assertTrue(latch.await(1, TimeUnit.SECONDS));
//            assertEquals(1, response.size());
//            assertFalse(response.get(0).getIsValid());
//        } catch (InterruptedException e) {
//            fail(e.getMessage());
//        }
//    }
//
//    @Test
//    public void testRejectInvalidDigestSalt() {
//        List<MacValidationResponse> response = new ArrayList<>();
//
//        // Given
//        MacValidationForTypeRequest request = MacValidationForTypeRequest.newBuilder()
//                .setKa(ByteString.copyFrom(ByteUtils.generateRandom(16)))
//                .setDataToValidate(ByteString.copyFrom(ByteUtils.generateRandom(12)))
//                .setMacToMatchWith(ByteString.copyFrom(ByteUtils.generateRandom(16)))
//                .setPrefixe(ByteString.copyFrom(new byte[] { (byte)0xFF }))
//                .build();
//
//        CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//        List<Throwable> exceptions  = new ArrayList<>();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//
//        StreamObserver<MacValidationResponse> responseObserver =
//                new StreamObserver<MacValidationResponse>() {
//            @Override
//            public void onNext(MacValidationResponse value) {
//                response.add(value);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//
//                exceptions.add(t);
//            }
//
//            @Override
//            public void onCompleted() {
//                latch.countDown();
//            }
//        };
//
//        stub.validateMacForType(request, responseObserver);
//
//        // When
//        assertFalse(CollectionUtils.isEmpty(exceptions));
//        assertEquals(1, exceptions.size());
//        assertTrue(exceptions.get(0) instanceof StatusRuntimeException);
//    }
//
//    @Test
//    public void testGenerateIdentityWhenBadClientPublicKey() {
//
//        // Given
//        GenerateIdentityRequest request = GenerateIdentityRequest.newBuilder()
//                .setClientPublicKey(ByteString.copyFrom(ByteUtils.generateRandom(32)))
//                .build();
//
//        CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//        List<Throwable> exceptions  = new ArrayList<>();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//
//        StreamObserver<GenerateIdentityResponse> responseObserver =
//                new StreamObserver<GenerateIdentityResponse>() {
//            @Override
//            public void onNext(GenerateIdentityResponse value) {
//                fail();
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                exceptions.add(t);
//            }
//
//            @Override
//            public void onCompleted() {
//                latch.countDown();
//            }
//        };
//
//
//        // When
//        stub.generateIdentity(request, responseObserver);
//
//        // Then
//        assertFalse(CollectionUtils.isEmpty(exceptions));
//        assertEquals(1, exceptions.size());
//        assertTrue(exceptions.get(0) instanceof StatusRuntimeException);
//    }
//
//    @Test
//    public void testGenerateIdentityWhenGoodClientPublicKey() {
//
//        try {
//            // Given
//            byte[] clientPublicKey = CryptoTestUtils.generateECDHPublicKey();
//            GenerateIdentityRequest request = GenerateIdentityRequest.newBuilder()
//                    .setClientPublicKey(ByteString.copyFrom(clientPublicKey))
//                    .build();
//
//            CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//            List<GenerateIdentityResponse> response = new ArrayList<>();
//
//            final CountDownLatch latch = new CountDownLatch(1);
//
//            StreamObserver<GenerateIdentityResponse> responseObserver =
//                    new StreamObserver<GenerateIdentityResponse>() {
//
//                @Override
//                public void onNext(GenerateIdentityResponse value) {
//                    response.add(value);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    fail();
//                }
//
//                @Override
//                public void onCompleted() {
//                    latch.countDown();
//                }
//            };
//
//            // When
//            stub.generateIdentity(request, responseObserver);
//
//            //
//            assertTrue(latch.await(1, TimeUnit.SECONDS));
//            assertEquals(1, response.size());
//            assertNotNull(response.get(0).getIdA());
//        } catch (InterruptedException e) {
//            fail(e.getMessage());
//        }
//    }
//
//    @Test
//    public void testGenerateEncryptedEphemeralTupleWhenBadClientPublicKey() {
//
//        // Given
//        EncryptedEphemeralTupleBundleRequest request = EncryptedEphemeralTupleBundleRequest.newBuilder()
//                .setClientPublicKey(ByteString.copyFrom(ByteUtils.generateRandom(32)))
//                .setIdA(ByteString.copyFrom(ByteUtils.generateRandom(5)))
//                .setCountryCode(ByteString.copyFrom(ByteUtils.generateRandom(1)))
//                .setFromEpoch(2100)
//                .setNumberOfEpochsToGenerate(1)
//                .build();
//
//        CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//        List<Throwable> exceptions  = new ArrayList<>();
//
//        final CountDownLatch latch = new CountDownLatch(1);
//
//        StreamObserver<EncryptedEphemeralTupleBundleResponse> responseObserver =
//                new StreamObserver<EncryptedEphemeralTupleBundleResponse>() {
//            @Override
//            public void onNext(EncryptedEphemeralTupleBundleResponse value) {
//                fail();
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                exceptions.add(t);
//            }
//
//            @Override
//            public void onCompleted() {
//                latch.countDown();
//            }
//        };
//
//
//        // When
//        stub.generateEncryptedEphemeralTuple(request, responseObserver);
//
//        // Then
//        assertFalse(CollectionUtils.isEmpty(exceptions));
//        assertEquals(1, exceptions.size());
//        assertTrue(exceptions.get(0) instanceof StatusRuntimeException);
//    }
//
//    @Test
//    public void testGenerateEncryptedEphemeralTupleWhenGoodClientPublicKey() {
//
//        try {
//            // Given
//            byte[] clientPublicKey = CryptoTestUtils.generateECDHPublicKey();
//            EncryptedEphemeralTupleBundleRequest request = EncryptedEphemeralTupleBundleRequest.newBuilder()
//                    .setClientPublicKey(ByteString.copyFrom(clientPublicKey))
//                    .setIdA(ByteString.copyFrom(ByteUtils.generateRandom(5)))
//                    .setCountryCode(ByteString.copyFrom(ByteUtils.generateRandom(1)))
//                    .setFromEpoch(2100)
//                    .setNumberOfEpochsToGenerate(1)
//                    .build();
//
//            CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);
//
//            List<EncryptedEphemeralTupleBundleResponse> response = new ArrayList<>();
//
//            final CountDownLatch latch = new CountDownLatch(1);
//
//            StreamObserver<EncryptedEphemeralTupleBundleResponse> responseObserver =
//                    new StreamObserver<EncryptedEphemeralTupleBundleResponse>() {
//
//                @Override
//                public void onNext(EncryptedEphemeralTupleBundleResponse value) {
//                    response.add(value);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    fail();
//                }
//
//                @Override
//                public void onCompleted() {
//                    latch.countDown();
//                }
//            };
//
//            // When
//            stub.generateEncryptedEphemeralTuple(request, responseObserver);
//
//            //
//            assertTrue(latch.await(1, TimeUnit.SECONDS));
//            assertEquals(1, response.size());
//            assertNotNull(response.get(0).getEncryptedTuples());
//            assertNotNull(response.get(0).getServerPublicKeyForTuples());
//        } catch (InterruptedException e) {
//            fail(e.getMessage());
//        }
//    }
}
