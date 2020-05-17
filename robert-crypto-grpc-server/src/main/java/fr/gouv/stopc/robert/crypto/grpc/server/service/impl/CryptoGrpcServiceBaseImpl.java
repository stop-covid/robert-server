package fr.gouv.stopc.robert.crypto.grpc.server.service.impl;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.protobuf.ByteString;

import fr.gouv.stopc.robert.crypto.grpc.server.messaging.CryptoGrpcServiceImplGrpc.CryptoGrpcServiceImplImplBase;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.DecryptCountryCodeRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.DecryptCountryCodeResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.DecryptEBIDRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.EBIDResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.EncryptCountryCodeRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.EncryptCountryCodeResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.EphemeralTupleRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.EphemeralTupleResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.GenerateEBIDRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.GenerateIdentityRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.GenerateIdentityResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.MacEsrValidationRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.MacHelloGenerationRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.MacHelloGenerationResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.MacHelloValidationRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.MacValidationForTypeRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.MacValidationResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.model.ClientECDHBundle;
import fr.gouv.stopc.robert.crypto.grpc.server.model.ClientIdentifierBundle;
import fr.gouv.stopc.robert.crypto.grpc.server.service.IClientKeyStorageService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.ICryptoServerConfigurationService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.IKeyService;
import fr.gouv.stopc.robert.server.common.DigestSaltEnum;
import fr.gouv.stopc.robert.server.crypto.callable.TupleGenerator;
import fr.gouv.stopc.robert.server.crypto.exception.RobertServerCryptoException;
import fr.gouv.stopc.robert.server.crypto.model.EphemeralTuple;
import fr.gouv.stopc.robert.server.crypto.service.CryptoService;
import fr.gouv.stopc.robert.server.crypto.structure.impl.Crypto3DES;
import fr.gouv.stopc.robert.server.crypto.structure.impl.CryptoAES;
import fr.gouv.stopc.robert.server.crypto.structure.impl.CryptoHMACSHA256;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CryptoGrpcServiceBaseImpl extends CryptoGrpcServiceImplImplBase {


    private final ICryptoServerConfigurationService serverConfigurationService;
    private final CryptoService cryptoService;
    private final IKeyService keyService;
    private final IClientKeyStorageService clientStorageService;

    @Inject
    public CryptoGrpcServiceBaseImpl(final ICryptoServerConfigurationService serverConfigurationService,
            final CryptoService cryptoService,
            final IKeyService keyService,
            final IClientKeyStorageService clientStorageService) {

        this.serverConfigurationService = serverConfigurationService;
        this.cryptoService = cryptoService;
        this.keyService = keyService;
        this.clientStorageService = clientStorageService;
    }

    @Override
    public void generateEphemeralTuple(EphemeralTupleRequest request,
            StreamObserver<EphemeralTupleResponse> responseObserver) {

        final byte[] serverKey = this.serverConfigurationService.getServerKey();
        final byte[] federationKey = this.serverConfigurationService.getFederationKey();
        final TupleGenerator tupleGenerator = new TupleGenerator(serverKey, federationKey, 1);
        try {
            final Collection<EphemeralTuple> ephemeralTuples = tupleGenerator.exec(
                    request.getIdA().toByteArray(),
                    request.getCurrentEpochID(),
                    request.getNumberOfEpochsToGenerate(),
                    request.getCountryCode().byteAt(0)
                    );
            tupleGenerator.stop();

            if (!CollectionUtils.isEmpty(ephemeralTuples)) {

                ephemeralTuples.stream()
                .map(tuple -> EphemeralTupleResponse
                        .newBuilder()
                        .setEbid(ByteString.copyFrom(tuple.getEbid()))
                        .setEcc(ByteString.copyFrom(tuple.getEncryptedCountryCode()))
                        .setEpochId(tuple.getEpoch())
                        .build())
                .forEach(response -> responseObserver.onNext(response));

            }
            responseObserver.onCompleted();
        } catch (Exception e) {

            responseObserver.onError(e);
        } finally {
            tupleGenerator.stop();
        }
    }

    @Override
    public void generateEBID(GenerateEBIDRequest request,
            StreamObserver<EBIDResponse> responseObserver) {
        try {
            responseObserver.onNext(EBIDResponse.newBuilder()
                    .setEbid(ByteString.copyFrom(this.cryptoService.generateEBID(
                            new Crypto3DES(this.serverConfigurationService.getServerKey()),
                            request.getEpochId(),
                            request.getIdA().toByteArray())))
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void decryptEBID(DecryptEBIDRequest request,
            StreamObserver<EBIDResponse> responseObserver) {
        try {
            responseObserver.onNext(EBIDResponse.newBuilder()
                    .setEbid(ByteString.copyFrom(
                            this.cryptoService.decryptEBID(
                                    new Crypto3DES(this.serverConfigurationService.getServerKey()),
                                    request.getEbid().toByteArray())))
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void encryptCountryCode(EncryptCountryCodeRequest request,
            StreamObserver<EncryptCountryCodeResponse> responseObserver) {
        try {
            responseObserver.onNext(EncryptCountryCodeResponse.newBuilder()
                    .setEncryptedCountryCode(ByteString.copyFrom(this.cryptoService.encryptCountryCode(
                            new CryptoAES(this.serverConfigurationService.getFederationKey()),
                            request.getEbid().toByteArray(),
                            request.getCountryCode().byteAt(0))))
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void decryptCountryCode(DecryptCountryCodeRequest request,
            StreamObserver<DecryptCountryCodeResponse> responseObserver) {
        try {
            responseObserver.onNext(DecryptCountryCodeResponse.newBuilder()
                    .setCountryCode(ByteString.copyFrom(this.cryptoService.decryptCountryCode(
                            new CryptoAES(this.serverConfigurationService.getFederationKey()),
                            request.getEbid().toByteArray(),
                            request.getEncryptedCountryCode().byteAt(0))))
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void generateMacHello(MacHelloGenerationRequest request,
            StreamObserver<MacHelloGenerationResponse> responseObserver) {
        try {

            responseObserver.onNext(MacHelloGenerationResponse.newBuilder()
                    .setMacHelloMessage(ByteString.copyFrom(this.cryptoService.generateMACHello(
                            new CryptoHMACSHA256(request.getKa().toByteArray()),
                            request.getHelloMessage().toByteArray()))).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void validateMacHello(MacHelloValidationRequest request,
            StreamObserver<MacValidationResponse> responseObserver) {
        try {

            responseObserver.onNext(MacValidationResponse.newBuilder()
                    .setIsValid(this.cryptoService.macHelloValidation(
                            new CryptoHMACSHA256(request.getKa().toByteArray()),
                            request.getDataToValidate().toByteArray())).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void validateMacEsr(MacEsrValidationRequest request,
            StreamObserver<MacValidationResponse> responseObserver) {

        try {
            responseObserver.onNext(MacValidationResponse.newBuilder()
                    .setIsValid(this.cryptoService.macESRValidation(
                            new CryptoHMACSHA256(request.getKa().toByteArray()),
                            request.getDataToValidate().toByteArray(),
                            request.getMacToMatchWith().toByteArray())).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }

    }

    @Override
    public void validateMacForType(MacValidationForTypeRequest request,
            StreamObserver<MacValidationResponse> responseObserver) {

        try {
            DigestSaltEnum salt = DigestSaltEnum.valueOf(request.getPrefixe().byteAt(0));

            if (Objects.isNull(salt)) {
                responseObserver.onError(new RobertServerCryptoException("Invalid salt value"));
            } else {
                responseObserver.onNext(MacValidationResponse.newBuilder()
                        .setIsValid(this.cryptoService.macValidationForType(
                                new CryptoHMACSHA256(request.getKa().toByteArray()),
                                request.getDataToValidate().toByteArray(),
                                request.getMacToMatchWith().toByteArray(),
                                salt)).build());
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            responseObserver.onError(e);
        }

    }

    @Override
    public void generateIdentity(GenerateIdentityRequest request,
            StreamObserver<GenerateIdentityResponse> responseObserver) {

        Optional<ClientECDHBundle> keys = this.keyService.generateECHDKeysForEncryption(
                request.getClientPublicKey().toByteArray());

        ClientIdentifierBundle clientIdentifierBundle = this.clientStorageService.createClientIdAndKey();

        if(Objects.isNull(clientIdentifierBundle)) {
            responseObserver.onError(new RobertServerCryptoException("Unable to generate the client"));
        }
        else if(!keys.isPresent()) {
            responseObserver.onError(new RobertServerCryptoException("Unable to generate an ECDH Keys"));
        }
        else {
            byte[] encrypted = this.cryptoService.aesEncrypt(clientIdentifierBundle.getKey(),
                    keys.get().getGeneratedSharedSecret());

            if(Objects.isNull(encrypted)) {
                responseObserver.onError(new RobertServerCryptoException("Unable encrypt the client key"));
            }
            else {
                GenerateIdentityResponse response = GenerateIdentityResponse
                        .newBuilder()
                        .setIdA(ByteString.copyFrom(clientIdentifierBundle.getId()))
                        .setServerPublicKeyForKey(ByteString.copyFrom(keys.get().getGeneratedSharedSecret()))
                        .setEncryptedSharedKey(ByteString.copyFrom(encrypted))
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();

            }

        }

    }
}
 
