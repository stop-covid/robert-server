package test.fr.gouv.stopc.robert.crypto.grpc.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.CreateRegistrationRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.CreateRegistrationResponse;
import fr.gouv.stopc.robert.server.common.utils.TimeUtils;
import fr.gouv.stopc.robert.server.crypto.exception.RobertServerCryptoException;
import fr.gouv.stopc.robert.server.crypto.model.EphemeralTuple;
import fr.gouv.stopc.robert.server.crypto.structure.impl.CryptoAESGCM;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.protobuf.ByteString;

import fr.gouv.stopc.robert.crypto.grpc.server.CryptoServiceGrpcServer;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.CryptoGrpcServiceImplGrpc;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.CryptoGrpcServiceImplGrpc.CryptoGrpcServiceImplImplBase;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.CryptoGrpcServiceImplGrpc.CryptoGrpcServiceImplStub;

import fr.gouv.stopc.robert.crypto.grpc.server.service.IClientKeyStorageService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.ICryptoServerConfigurationService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.IECDHKeyService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.impl.ClientKeyStorageService;
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

@ExtendWith(SpringExtension.class)
public class CryptoServiceGrpcServerTest {

    private final static String UNEXPECTED_FAILURE_MESSAGE = "Should not fail";

    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
    private ManagedChannel inProcessChannel;

    private CryptoServiceGrpcServer server;

    private CryptoGrpcServiceImplImplBase service;

    private ICryptoServerConfigurationService serverConfigurationService;

    private CryptoService cryptoService;

    private IECDHKeyService keyService;

    private IClientKeyStorageService clientStorageService;

    @BeforeEach
    public void beforeEach() throws IOException {

        serverConfigurationService = new CryptoServerConfigurationServiceImpl();

        cryptoService=  new CryptoServiceImpl();

        keyService = new ECDHKeyServiceImpl();

        clientStorageService = new ClientKeyStorageService();

        service = new CryptoGrpcServiceBaseImpl(serverConfigurationService,
                cryptoService,
                keyService,
                clientStorageService);

        String serverName = InProcessServerBuilder.generateName();

        server = new CryptoServiceGrpcServer(
                InProcessServerBuilder.forName(serverName)
                .directExecutor()
                , 0, service);
        server.start();
        inProcessChannel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());
    }

    @AfterEach
    public void tearDown() throws Exception {
        server.stop();
    }

    private final static byte[] SERVER_COUNTRY_CODE = new byte[] { (byte)0x33 };
    @Test
    public void testCreateRegistrationSucceeds() {
        try {
            int numberOfBundles = 4 * 4 * 24;
            // Given
            CreateRegistrationRequest request = CreateRegistrationRequest
                    .newBuilder()
                    .setClientPublicKey(ByteString.copyFrom(CryptoTestUtils.generateECDHPublicKey()))
                    .setFromEpochId(TimeUtils.getCurrentEpochFrom(this.serverConfigurationService.getServiceTimeStart()))
                    .setNumberOfEpochBundles(numberOfBundles)
                    .setServerCountryCode(ByteString.copyFrom(SERVER_COUNTRY_CODE))
                    .build();

            CryptoGrpcServiceImplStub stub = CryptoGrpcServiceImplGrpc.newStub(inProcessChannel);

            final List<CreateRegistrationResponse> response = new ArrayList<>();

            final CountDownLatch latch = new CountDownLatch(1);

            StreamObserver<CreateRegistrationResponse> responseObserver =
                    new StreamObserver<CreateRegistrationResponse>() {
                        @Override
                        public void onNext(CreateRegistrationResponse value) {
                            response.add(value);
                        }

                        @Override
                        public void onError(Throwable t) {
                            fail();
                        }

                        @Override
                        public void onCompleted() {
                            latch.countDown();
                        }
                    };

            // When
            stub.createRegistration(request, responseObserver);

            // Then
            assertTrue(latch.await(1, TimeUnit.SECONDS));
            assertEquals(1, response.size());
            CreateRegistrationResponse createRegistrationResponse = response.get(0);
            assertTrue(ByteUtils.isNotEmpty(createRegistrationResponse.getIdA().toByteArray()));
            byte[] tuples = createRegistrationResponse.getTuples().toByteArray();
            assertTrue(ByteUtils.isNotEmpty(tuples));
            CryptoAESGCM aesGcm = new CryptoAESGCM(
                    this.clientStorageService.findKeyById(
                            createRegistrationResponse.getIdA().toByteArray()).get().getKeyTuples());
            try {
                byte[] decryptedTuples = aesGcm.decrypt(tuples);
                ObjectMapper objectMapper = new ObjectMapper();
                Collection<EphemeralTuple> decodedTuples = objectMapper.readValue(
                        decryptedTuples,
                        new TypeReference<Collection<EphemeralTuple>>(){});
                assertEquals(numberOfBundles, decodedTuples.size());
            } catch (RobertServerCryptoException | IOException e) {
                fail(UNEXPECTED_FAILURE_MESSAGE);
            }
        } catch (InterruptedException e) {
            fail(e.getMessage());
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
