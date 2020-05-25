package fr.gouv.stopc.robert.crypto.grpc.server.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.Option;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.*;
import fr.gouv.stopc.robert.server.common.utils.ByteUtils;
import fr.gouv.stopc.robert.server.common.utils.TimeUtils;
import fr.gouv.stopc.robert.server.crypto.structure.impl.CryptoAESGCM;
import fr.gouv.stopc.robert.server.crypto.structure.impl.CryptoAESOFB;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;

import fr.gouv.stopc.robert.crypto.grpc.server.messaging.CryptoGrpcServiceImplGrpc.CryptoGrpcServiceImplImplBase;
import fr.gouv.stopc.robert.crypto.grpc.server.storage.model.ClientIdentifierBundle;
import fr.gouv.stopc.robert.crypto.grpc.server.storage.service.IClientKeyStorageService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.ICryptoServerConfigurationService;
import fr.gouv.stopc.robert.crypto.grpc.server.service.IECDHKeyService;
import fr.gouv.stopc.robert.server.common.DigestSaltEnum;
import fr.gouv.stopc.robert.server.crypto.callable.TupleGenerator;
import fr.gouv.stopc.robert.server.crypto.exception.RobertServerCryptoException;
import fr.gouv.stopc.robert.server.crypto.model.EphemeralTuple;
import fr.gouv.stopc.robert.server.crypto.service.CryptoService;
import fr.gouv.stopc.robert.server.crypto.structure.impl.CryptoHMACSHA256;
import fr.gouv.stopc.robert.server.crypto.structure.impl.CryptoSkinny64;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CryptoGrpcServiceBaseImpl extends CryptoGrpcServiceImplImplBase {

    private final ICryptoServerConfigurationService serverConfigurationService;
    private final CryptoService cryptoService;
    private final IECDHKeyService keyService;
    private final IClientKeyStorageService clientStorageService;

    @Inject
    public CryptoGrpcServiceBaseImpl(final ICryptoServerConfigurationService serverConfigurationService,
            final CryptoService cryptoService,
            final IECDHKeyService keyService,
            final IClientKeyStorageService clientStorageService) {

        this.serverConfigurationService = serverConfigurationService;
        this.cryptoService = cryptoService;
        this.keyService = keyService;
        this.clientStorageService = clientStorageService;
    }

    @Override
    public void createRegistration(CreateRegistrationRequest request,
                                   StreamObserver<CreateRegistrationResponse> responseObserver) {

        try {
            // Derive K_A and K_A_Tuples from client public key for the new registration
            Optional<ClientIdentifierBundle> clientIdentifierBundle = this.keyService.deriveKeysFromClientPublicKey(request.getClientPublicKey().toByteArray());

            if (!clientIdentifierBundle.isPresent()) {
                responseObserver.onError(new RobertServerCryptoException("Unable to create keys for registration"));
                return;
            }

            clientIdentifierBundle = this.clientStorageService.createClientIdUsingKeys(
                    clientIdentifierBundle.get().getKeyForMac(),
                    clientIdentifierBundle.get().getKeyForTuples());

            if(!clientIdentifierBundle.isPresent()) {
                responseObserver.onError(new RobertServerCryptoException("Unable to create a registration"));
                return;
            }

            Optional<TuplesGenerationResult> encryptedTuples = generateEncryptedTuples(
                    clientIdentifierBundle.get().getKeyForTuples(),
                    clientIdentifierBundle.get().getId(),
                    request.getFromEpochId(),
                    request.getNumberOfEpochBundles(),
                    request.getServerCountryCode().byteAt(0));

            if (!encryptedTuples.isPresent()) {
                responseObserver.onError(new RobertServerCryptoException("Unhandled exception while creating registration"));
                return;
            }

            CreateRegistrationResponse response = CreateRegistrationResponse
                    .newBuilder()
                    .setIdA(ByteString.copyFrom(clientIdentifierBundle.get().getId()))
                    .setTuples(ByteString.copyFrom(encryptedTuples.get().getEncryptedTuples()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (RobertServerCryptoException e) {
            responseObserver.onError(new RobertServerCryptoException("Unhandled exception while creating registration"));
        }
    }

    @Builder
    @Getter
    @AllArgsConstructor
    private static class TuplesGenerationResult {
        byte[] encryptedTuples;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class EpheremalTupleJson {
        private int epochId;
        private EpheremalTupleEbidEccJson key;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class EpheremalTupleEbidEccJson {
        private byte[] ebid;
        private byte[] ecc;
    }

    private java.util.List<EpheremalTupleJson> mapEphemeralTuples(Collection<EphemeralTuple> tuples) {
        ArrayList<EpheremalTupleJson> mappedTuples = new ArrayList<>();

        for (EphemeralTuple tuple : tuples) {
            mappedTuples.add(EpheremalTupleJson.builder()
                    .epochId(tuple.getEpochId())
                    .key(EpheremalTupleEbidEccJson.builder()
                            .ebid(tuple.getEbid())
                            .ecc(tuple.getEncryptedCountryCode())
                            .build())
                    .build());
        }
        return mappedTuples;
    }

    private Optional<TuplesGenerationResult> generateEncryptedTuples(byte[] tuplesEncryptionKey,
                                                                     byte[] id,
                                                                     int epochId,
                                                                     int nbOfEpochs,
                                                                     byte serverCountryCode) {
        // TODO: provide M/96 K_S keys for tuple generation for next M epochs
        // Generate tuples
        final byte[] serverKey = this.serverConfigurationService.getServerKey();
        final byte[] federationKey = this.serverConfigurationService.getFederationKey();
        final TupleGenerator tupleGenerator = new TupleGenerator(serverKey, federationKey, 1);
        try {
            final Collection<EphemeralTuple> ephemeralTuples = tupleGenerator.exec(
                    id,
                    epochId,
                    nbOfEpochs,
                    serverCountryCode
            );
            tupleGenerator.stop();

            if (!CollectionUtils.isEmpty(ephemeralTuples)) {
                // TODO: careful EphemeralTuple is different from the one in the current REST API,
                // resulting JSONs don't match
                ObjectMapper objectMapper = new ObjectMapper();
                byte[] tuplesAsBytes = objectMapper.writeValueAsBytes(mapEphemeralTuples(ephemeralTuples));
                CryptoAESGCM cryptoAESGCM = new CryptoAESGCM(tuplesEncryptionKey);
                return Optional.of(TuplesGenerationResult.builder().encryptedTuples(cryptoAESGCM.encrypt(tuplesAsBytes)).build());
            }
            return Optional.empty();
        } catch (JsonProcessingException | RobertServerCryptoException e) {
            return Optional.empty();
        }
    }

    @Override
    public void getIdFromAuth(GetIdFromAuthRequest request,
                              StreamObserver<GetIdFromAuthResponse> responseObserver) {
        Optional<AuthRequestValidationResult> validationResult = validateAuthRequest(
                request.getEbid().toByteArray(),
                request.getEpochId(),
                request.getTime(),
                request.getMac().toByteArray(),
                DigestSaltEnum.valueOf((byte)request.getRequestType()));

        if (!validationResult.isPresent()) {
            responseObserver.onError(new RobertServerCryptoException("Could not validate auth request"));
            return;
        }

        responseObserver.onNext(GetIdFromAuthResponse.newBuilder()
                .setIdA(ByteString.copyFrom(validationResult.get().getId()))
                .setEpochId(validationResult.get().getEpochId())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getIdFromStatus(GetIdFromStatusRequest request,
                                StreamObserver<GetIdFromStatusResponse> responseObserver) {
        Optional<AuthRequestValidationResult> validationResult = validateAuthRequest(
                request.getEbid().toByteArray(),
                request.getEpochId(),
                request.getTime(),
                request.getMac().toByteArray(),
                DigestSaltEnum.STATUS);

        if (!validationResult.isPresent()) {
            responseObserver.onError(new RobertServerCryptoException("Could not validate auth request"));
            return;
        }

        Optional<ClientIdentifierBundle> clientIdentifierBundle = this.clientStorageService.findKeyById(validationResult.get().getId());
        if (!clientIdentifierBundle.isPresent()) {
            responseObserver.onError(new RobertServerCryptoException("Unknown id"));
            return;
        }

        Optional<TuplesGenerationResult> encryptedTuples = generateEncryptedTuples(
                clientIdentifierBundle.get().getKeyForTuples(),
                clientIdentifierBundle.get().getId(),
                request.getFromEpochId(),
                request.getNumberOfEpochBundles(),
                request.getServerCountryCode().byteAt(0));

        if (!encryptedTuples.isPresent()) {
            responseObserver.onError(new RobertServerCryptoException("Unhandled exception while creating registration"));
            return;
        }

        responseObserver.onNext(GetIdFromStatusResponse.newBuilder()
                .setIdA(ByteString.copyFrom(validationResult.get().getId()))
                .setEpochId(validationResult.get().getEpochId())
                .setTuples(ByteString.copyFrom(encryptedTuples.get().getEncryptedTuples()))
                .build());
        responseObserver.onCompleted();
    }

    private byte[] getIdFromDecryptedEBID(byte[] ebid) {
        byte[] idA = new byte[5];
        System.arraycopy(ebid, 3, idA, 0, idA.length);
        return idA;
    }

    private int getEpochIdFromDecryptedEBID(byte[] ebid) {
        byte[] epochId = new byte[3];
        System.arraycopy(ebid, 0, epochId, 0, epochId.length);
        return ByteUtils.convertEpoch24bitsToInt(epochId);
    }

    @Builder
    @AllArgsConstructor
    @Getter
    private static class EbidContent {
        byte[] idA;
        int epochId;
    }

    // TODO: handle edge cases at edges of an epoch (start and finish) by trying and previous K_S
    private EbidContent decryptEBID(byte[] ebid, long timeReceived) throws RobertServerCryptoException {
        int epoch = TimeUtils.getNumberOfEpochsBetween(this.serverConfigurationService.getServiceTimeStart(), timeReceived);
        byte[] decryptedEbid = this.cryptoService.decryptEBID(
                new CryptoSkinny64(this.serverConfigurationService.getServerKeyForEpochId(epoch)),
                ebid);
        byte[] idA = getIdFromDecryptedEBID(decryptedEbid);
        int epochId = getEpochIdFromDecryptedEBID(decryptedEbid);

        if (epochId < 0) {
            log.error("Epoch from EBID is negative");
            return null;
        } else if (epoch != epochId) {
            log.error("Epoch from EBID and accompanying epoch do not match");
            return null;
        }

        return EbidContent.builder().epochId(epochId).idA(idA).build();
    }

    private byte[] decryptECC(byte[] ebid, byte encryptedCountryCode) throws RobertServerCryptoException {
        return this.cryptoService.decryptCountryCode(
                new CryptoAESOFB(this.serverConfigurationService.getFederationKey()), ebid, encryptedCountryCode);
    }

    @Override
    public void getInfoFromHelloMessage(GetInfoFromHelloMessageRequest request,
                                        StreamObserver<GetInfoFromHelloMessageResponse> responseObserver) {
        byte[] idA;
        int epochId;
        byte[] cc;
        try {
            // Decrypt EBID
            EbidContent ebidContent = decryptEBID(request.getEbid().toByteArray(), request.getTimeReceived());

            if (Objects.isNull(ebidContent)) {
                responseObserver.onError(new RobertServerCryptoException("Could not decrypt EBID"));
                return;
            }

            idA = ebidContent.getIdA();
            epochId = ebidContent.getEpochId();
        } catch (RobertServerCryptoException e) {
            responseObserver.onError(new RobertServerCryptoException("Could not decrypt EBID"));
            return;
        }

        try {
            // Decrypt ECC
            cc = decryptECC(request.getEbid().toByteArray(), request.getEcc().byteAt(0));
        } catch (RobertServerCryptoException e) {
            responseObserver.onError(new RobertServerCryptoException("Could not decrypt ECC"));
            return;
        }

        GetInfoFromHelloMessageResponse response = GetInfoFromHelloMessageResponse.newBuilder()
                .setIdA(ByteString.copyFrom(idA))
                .setEpochId(epochId)
                .setCountryCode(ByteString.copyFrom(cc))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteId(DeleteIdRequest request,
                         StreamObserver<DeleteIdResponse> responseObserver) {
        Optional<AuthRequestValidationResult> validationResult = validateAuthRequest(
                request.getEbid().toByteArray(),
                request.getEpochId(),
                request.getTime(),
                request.getMac().toByteArray(),
                DigestSaltEnum.DELETE_HISTORY);

        if (!validationResult.isPresent()) {
            responseObserver.onError(new RobertServerCryptoException("Could not validate auth request"));
            return;
        }

        this.clientStorageService.deleteClientId(validationResult.get().getId());

        responseObserver.onNext(DeleteIdResponse.newBuilder()
                .setIdA(ByteString.copyFrom(validationResult.get().getId()))
                .build());
        responseObserver.onCompleted();
    }

    @Builder
    @Getter
    @AllArgsConstructor
    private static class AuthRequestValidationResult {
        private byte[] id;
        private int epochId;
    }

    private byte[] addEbidComponents(byte[] encryptedEbid, int epochId, long time) {
        byte[] all = new byte[encryptedEbid.length + Integer.BYTES + Long.BYTES];
        System.arraycopy(encryptedEbid, 0, all, 0, encryptedEbid.length);
        System.arraycopy(ByteUtils.intToBytes(epochId), 0, all, encryptedEbid.length, Integer.BYTES);
        System.arraycopy(
                ByteUtils.longToBytes(time),
                0,
                all,
                encryptedEbid.length + Integer.BYTES,
                Long.BYTES);
        return all;
    }

    private Optional<AuthRequestValidationResult> validateAuthRequest(byte[] encryptedEbid,
                                                                      int epochId,
                                                                      long time,
                                                                      byte[] mac,
                                                                      DigestSaltEnum type) {
        try {
            EbidContent ebidContent = decryptEBID(encryptedEbid, epochId);

            Optional<ClientIdentifierBundle> clientIdentifierBundle = this.clientStorageService.findKeyById(ebidContent.getIdA());
            if (!clientIdentifierBundle.isPresent()) {
                return Optional.empty();
            }
            boolean valid = this.cryptoService.macValidationForType(
                                new CryptoHMACSHA256(clientIdentifierBundle.get().getKeyForMac()),
                                addEbidComponents(encryptedEbid, epochId, time),
                                mac,
                                type);
            if (valid) {
                return Optional.of(AuthRequestValidationResult.builder()
                        .epochId(ebidContent.getEpochId())
                        .id(ebidContent.getIdA())
                        .build());
            }
        } catch (RobertServerCryptoException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    // TODO: delete OLD implementations below

//    @Override
//    public void generateEBID(GenerateEBIDRequest request,
//            StreamObserver<EBIDResponse> responseObserver) {
//        try {
//            responseObserver.onNext(EBIDResponse.newBuilder()
//                    .setEbid(ByteString.copyFrom(this.cryptoService.generateEBID(
//                            new CryptoSkinny64(this.serverConfigurationService.getServerKey()),
//                            request.getEpochId(),
//                            request.getIdA().toByteArray())))
//                    .build());
//            responseObserver.onCompleted();
//        } catch (Exception e) {
//            responseObserver.onError(e);
//        }
//    }
//
//    @Override
//    public void decryptEBID(DecryptEBIDRequest request,
//            StreamObserver<EBIDResponse> responseObserver) {
//        try {
//            responseObserver.onNext(EBIDResponse.newBuilder()
//                    .setEbid(ByteString.copyFrom(
//                            this.cryptoService.decryptEBID(
//                                    new CryptoSkinny64(this.serverConfigurationService.getServerKey()),
//                                    request.getEbid().toByteArray())))
//                    .build());
//            responseObserver.onCompleted();
//        } catch (Exception e) {
//            responseObserver.onError(e);
//        }
//    }
//
//    @Override
//    public void encryptCountryCode(EncryptCountryCodeRequest request,
//            StreamObserver<EncryptCountryCodeResponse> responseObserver) {
//        try {
//            responseObserver.onNext(EncryptCountryCodeResponse.newBuilder()
//                    .setEncryptedCountryCode(ByteString.copyFrom(this.cryptoService.encryptCountryCode(
//                            new CryptoAESOFB(this.serverConfigurationService.getFederationKey()),
//                            request.getEbid().toByteArray(),
//                            request.getCountryCode().byteAt(0))))
//                    .build());
//            responseObserver.onCompleted();
//        } catch (Exception e) {
//            responseObserver.onError(e);
//        }
//    }
//
//    @Override
//    public void decryptCountryCode(DecryptCountryCodeRequest request,
//            StreamObserver<DecryptCountryCodeResponse> responseObserver) {
//        try {
//            responseObserver.onNext(DecryptCountryCodeResponse.newBuilder()
//                    .setCountryCode(ByteString.copyFrom(this.cryptoService.decryptCountryCode(
//                            new CryptoAESOFB(this.serverConfigurationService.getFederationKey()),
//                            request.getEbid().toByteArray(),
//                            request.getEncryptedCountryCode().byteAt(0))))
//                    .build());
//            responseObserver.onCompleted();
//        } catch (Exception e) {
//            responseObserver.onError(e);
//        }
//    }
//
//    @Override
//    public void generateMacHello(MacHelloGenerationRequest request,
//            StreamObserver<MacHelloGenerationResponse> responseObserver) {
//        try {
//
//            responseObserver.onNext(MacHelloGenerationResponse.newBuilder()
//                    .setMacHelloMessage(ByteString.copyFrom(this.cryptoService.generateMACHello(
//                            new CryptoHMACSHA256(request.getKa().toByteArray()),
//                            request.getHelloMessage().toByteArray()))).build());
//            responseObserver.onCompleted();
//
//        } catch (Exception e) {
//            responseObserver.onError(e);
//        }
//    }
//
//    @Override
//    public void validateMacHello(MacHelloValidationRequest request,
//            StreamObserver<MacValidationResponse> responseObserver) {
//        try {
//
//            responseObserver.onNext(MacValidationResponse.newBuilder()
//                    .setIsValid(this.cryptoService.macHelloValidation(
//                            new CryptoHMACSHA256(request.getKa().toByteArray()),
//                            request.getDataToValidate().toByteArray())).build());
//            responseObserver.onCompleted();
//
//        } catch (Exception e) {
//            responseObserver.onError(e);
//        }
//    }
//
//    @Override
//    public void validateMacEsr(MacEsrValidationRequest request,
//            StreamObserver<MacValidationResponse> responseObserver) {
//
//        try {
//            responseObserver.onNext(MacValidationResponse.newBuilder()
//                    .setIsValid(this.cryptoService.macESRValidation(
//                            new CryptoHMACSHA256(request.getKa().toByteArray()),
//                            request.getDataToValidate().toByteArray(),
//                            request.getMacToMatchWith().toByteArray())).build());
//            responseObserver.onCompleted();
//        } catch (Exception e) {
//            responseObserver.onError(e);
//        }
//
//    }
//
//    @Override
//    public void validateMacForType(MacValidationForTypeRequest request,
//            StreamObserver<MacValidationResponse> responseObserver) {
//
//        try {
//            DigestSaltEnum salt = DigestSaltEnum.valueOf(request.getPrefixe().byteAt(0));
//
//            if (Objects.isNull(salt)) {
//                responseObserver.onError(new RobertServerCryptoException("Invalid salt value"));
//            } else {
//                responseObserver.onNext(MacValidationResponse.newBuilder()
//                        .setIsValid(this.cryptoService.macValidationForType(
//                                new CryptoHMACSHA256(request.getKa().toByteArray()),
//                                request.getDataToValidate().toByteArray(),
//                                request.getMacToMatchWith().toByteArray(),
//                                salt)).build());
//                responseObserver.onCompleted();
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            responseObserver.onError(e);
//        }
//
//    }
//
//    @Override
//    public void generateIdentity(GenerateIdentityRequest request,
//            StreamObserver<GenerateIdentityResponse> responseObserver) {
//
//        Optional<ServerECDHBundle> keys = this.keyService.generateECHDKeysForEncryption(
//                request.getClientPublicKey().toByteArray());
//
//        ClientIdentifierBundle clientIdentifierBundle = this.clientStorageService.createClientIdAndKey();
//
//        if(Objects.isNull(clientIdentifierBundle)) {
//            responseObserver.onError(new RobertServerCryptoException("Unable to generate the client"));
//        }
//        else if(!keys.isPresent()) {
//            responseObserver.onError(new RobertServerCryptoException("Unable to generate an ECDH Key"));
//        }
//        else {
//            byte[] encrypted = this.cryptoService.performAESOperation(Cipher.ENCRYPT_MODE, clientIdentifierBundle.getKey(),
//                    keys.get().getGeneratedSharedSecret());
//
//            if(Objects.isNull(encrypted)) {
//                responseObserver.onError(new RobertServerCryptoException("Unable encrypt the client key"));
//            }
//            else {
//                GenerateIdentityResponse response = GenerateIdentityResponse
//                        .newBuilder()
//                        .setIdA(ByteString.copyFrom(clientIdentifierBundle.getId()))
//                        .setServerPublicKeyForKey(ByteString.copyFrom(keys.get().getServerPublicKey()))
//                        .setEncryptedSharedKey(ByteString.copyFrom(encrypted))
//                        .build();
//
//                responseObserver.onNext(response);
//                responseObserver.onCompleted();
//
//            }
//
//        }
//
//    }
//
//    public void generateEncryptedEphemeralTuple(EncryptedEphemeralTupleBundleRequest request,
//            io.grpc.stub.StreamObserver<EncryptedEphemeralTupleBundleResponse> responseObserver) {
//
//        Optional<ServerECDHBundle> keys = this.keyService.generateECHDKeysForEncryption(
//                request.getClientPublicKey().toByteArray());
//
//        if(!keys.isPresent()) {
//            responseObserver.onError(new RobertServerCryptoException("Unable to generate an ECDH Key"));
//        } else {
//
//            final byte[] serverKey = this.serverConfigurationService.getServerKey();
//            final byte[] federationKey = this.serverConfigurationService.getFederationKey();
//            final TupleGenerator tupleGenerator = new TupleGenerator(serverKey, federationKey, 1);
//            try {
//                final Collection<EphemeralTuple> ephemeralTuples = tupleGenerator.exec(
//                        request.getIdA().toByteArray(),
//                        request.getFromEpoch(),
//                        request.getNumberOfEpochsToGenerate(),
//                        request.getCountryCode().byteAt(0)
//                        );
//                tupleGenerator.stop();
//
//                if (!CollectionUtils.isEmpty(ephemeralTuples)) {
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    byte[] tuplesAsBytes = objectMapper.writeValueAsBytes(ephemeralTuples);
//                    byte[] encryptedTuples = this.cryptoService.performAESOperation(Cipher.ENCRYPT_MODE, tuplesAsBytes,  keys.get().getGeneratedSharedSecret());
//
//                    responseObserver.onNext(EncryptedEphemeralTupleBundleResponse.newBuilder()
//                            .setEncryptedTuples(ByteString.copyFrom(encryptedTuples))
//                            .setServerPublicKeyForTuples(ByteString.copyFrom(keys.get().getServerPublicKey()))
//                            .build());
//                }
//                responseObserver.onCompleted();
//            } catch (Exception e) {
//
//                responseObserver.onError(e);
//            } finally {
//                tupleGenerator.stop();
//            }
//        }
//
//    }
}
 
