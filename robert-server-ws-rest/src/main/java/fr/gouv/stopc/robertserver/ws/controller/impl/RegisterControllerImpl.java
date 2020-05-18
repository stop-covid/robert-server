package fr.gouv.stopc.robertserver.ws.controller.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.bson.internal.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.protobuf.ByteString;

import fr.gouv.stopc.robert.crypto.grpc.server.client.service.ICryptoServerGrpcClient;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.EncryptedEphemeralTupleRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.EncryptedEphemeralTupleResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.GenerateIdentityRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.GenerateIdentityResponse;
import fr.gouv.stopc.robert.server.common.service.IServerConfigurationService;
import fr.gouv.stopc.robert.server.common.utils.TimeUtils;
import fr.gouv.stopc.robertserver.database.model.ApplicationConfigurationModel;
import fr.gouv.stopc.robertserver.database.model.Registration;
import fr.gouv.stopc.robertserver.database.service.IApplicationConfigService;
import fr.gouv.stopc.robertserver.database.service.IRegistrationService;
import fr.gouv.stopc.robertserver.ws.controller.IRegisterController;
import fr.gouv.stopc.robertserver.ws.dto.AlgoConfigDto;
import fr.gouv.stopc.robertserver.ws.dto.RegisterResponseDto;
import fr.gouv.stopc.robertserver.ws.exception.RobertServerException;
import fr.gouv.stopc.robertserver.ws.service.CaptchaService;
import fr.gouv.stopc.robertserver.ws.utils.MessageConstants;
import fr.gouv.stopc.robertserver.ws.vo.RegisterVo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RegisterControllerImpl implements IRegisterController {

    private final IRegistrationService registrationService;

    private final IServerConfigurationService serverConfigurationService;

    private final IApplicationConfigService applicationConfigService;

    private final CaptchaService captchaService;

    private  final ICryptoServerGrpcClient cryptoServerClient;

    @Inject
    public RegisterControllerImpl(final IRegistrationService registrationService,
            final IServerConfigurationService serverConfigurationService,
            final IApplicationConfigService applicationConfigService,
            final CaptchaService captchaService,
            final ICryptoServerGrpcClient cryptoServerClient) {

        this.registrationService = registrationService;
        this.serverConfigurationService = serverConfigurationService;
        this.applicationConfigService = applicationConfigService;
        this.captchaService = captchaService;
        this.cryptoServerClient = cryptoServerClient;

    }

    @Override
    public ResponseEntity<RegisterResponseDto> register(RegisterVo registerVo) throws RobertServerException {

        // TODO: Enable this when the capcha becomes
        //        if (StringUtils.isEmpty(registerVo.getCaptcha())) {
        //            log.error("The 'captcha' is required.");
        //            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        //        }

        // TODO: Unable this when the  ECDH Public Key sharing is enabled
        //        if (StringUtils.isEmpty(registerVo.getClientPublicECDHKey())) {
        //            log.error("The client ECDH public is required.");
        //            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        //        }

        if (!this.captchaService.verifyCaptcha(registerVo)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        byte[] clientPublicECDHKey = Base64.decode(registerVo.getClientPublicECDHKey());

        GenerateIdentityRequest request = GenerateIdentityRequest.newBuilder()
                .setClientPublicKey(ByteString.copyFrom(clientPublicECDHKey))
                .build();

        Optional<GenerateIdentityResponse> response = this.cryptoServerClient.generateIdentity(request);

        if(!response.isPresent()) {
            log.error("Unable to generate an identity for the client");
            throw new RobertServerException(MessageConstants.ERROR_OCCURED);
        }

        GenerateIdentityResponse identity = response.get();

        Registration registration = Registration.builder()
                .permanentIdentifier(identity.getIdA().toByteArray())
                .build();

        Optional<Registration> registered = this.registrationService.saveRegistration(registration);


        if (registered.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(processRegistration(registerVo, identity));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private RegisterResponseDto processRegistration(RegisterVo registerVo, GenerateIdentityResponse identity) throws RobertServerException {

        RegisterResponseDto registerResponseDto = new RegisterResponseDto();

        List<ApplicationConfigurationModel> serverConf = this.applicationConfigService.findAll();
        if (CollectionUtils.isEmpty(serverConf)) {
            registerResponseDto.setFilteringAlgoConfig(Collections.emptyList());
        } else {
            registerResponseDto
            .setFilteringAlgoConfig(serverConf.stream().map(item -> AlgoConfigDto.builder().name(item.getName()).value(item.getValue()).build()).collect(Collectors.toList()));
        }

        final byte countrycode = this.serverConfigurationService.getServerCountryCode();

        final long tpstStart = this.serverConfigurationService.getServiceTimeStart();
        final int numberOfEpochs = 4 * 24 * 4;

        final int currentEpochId = TimeUtils.getCurrentEpochFrom(tpstStart);

        registerResponseDto.setTimeStart(tpstStart);

        byte[] clientPublicECDHKey = Base64.decode(registerVo.getClientPublicECDHKey());

        EncryptedEphemeralTupleRequest request = EncryptedEphemeralTupleRequest.newBuilder()
                .setCountryCode(ByteString.copyFrom(new byte[] {countrycode}))
                .setFromEpoch(currentEpochId)
                .setIdA(ByteString.copyFrom(identity.getIdA().toByteArray()))
                .setNumberOfEpochsToGenerate(numberOfEpochs)
                .setClientPublicKey(ByteString.copyFrom(clientPublicECDHKey))
                .build();


        Optional<EncryptedEphemeralTupleResponse> encryptedTuple = this.cryptoServerClient.generateEncryptedEphemeralTuple(request);

        if (!encryptedTuple.isPresent()) {
            log.warn("Could not generate encrypted (EBID, ECC) tuples");
            throw new RobertServerException(MessageConstants.ERROR_OCCURED);
        }

        registerResponseDto.setServerPublicECDHKeyForTuples(Base64.encode(
                encryptedTuple.get().getServerPublicKeyForTuples().toByteArray()));

        registerResponseDto.setTuples(Base64.encode(
                encryptedTuple.get().getEncryptedTuples().toByteArray()));

        registerResponseDto.setServerPublicECDHKeyForKey(Base64.encode(identity.getServerPublicKeyForKey().toByteArray()));

        registerResponseDto.setKey(Base64.encode(identity.getEncryptedSharedKey().toByteArray()));
        return registerResponseDto;
    }

}