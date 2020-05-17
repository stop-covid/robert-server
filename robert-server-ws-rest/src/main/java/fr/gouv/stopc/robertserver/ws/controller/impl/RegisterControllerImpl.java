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
import fr.gouv.stopc.robert.crypto.grpc.server.request.EphemeralTupleRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.request.GenerateIdentityRequest;
import fr.gouv.stopc.robert.crypto.grpc.server.response.EphemeralTupleResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.response.GenerateIdentityResponse;
import fr.gouv.stopc.robert.server.common.service.IServerConfigurationService;
import fr.gouv.stopc.robert.server.common.utils.TimeUtils;
import fr.gouv.stopc.robertserver.database.model.ApplicationConfigurationModel;
import fr.gouv.stopc.robertserver.database.model.Registration;
import fr.gouv.stopc.robertserver.database.service.IApplicationConfigService;
import fr.gouv.stopc.robertserver.database.service.IRegistrationService;
import fr.gouv.stopc.robertserver.ws.controller.IRegisterController;
import fr.gouv.stopc.robertserver.ws.dto.AlgoConfigDto;
import fr.gouv.stopc.robertserver.ws.dto.RegisterResponseDto;
import fr.gouv.stopc.robertserver.ws.dto.mapper.EpochKeyBundleDtoMapper;
import fr.gouv.stopc.robertserver.ws.exception.RobertServerException;
import fr.gouv.stopc.robertserver.ws.service.CaptchaService;
import fr.gouv.stopc.robertserver.ws.utils.MessageConstants;
import fr.gouv.stopc.robertserver.ws.vo.RegisterVo;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RegisterControllerImpl implements IRegisterController {

    private final IRegistrationService registrationService;

    private final IServerConfigurationService serverConfigurationService;

    private final IApplicationConfigService applicationConfigService;

    private final CaptchaService captchaService;

    private final EpochKeyBundleDtoMapper epochKeyBundleDtoMapper;

    private  final ICryptoServerGrpcClient cryptoServerClient;

    @Inject
    public RegisterControllerImpl(final IRegistrationService registrationService,
                                  final IServerConfigurationService serverConfigurationService,
                                  final IApplicationConfigService applicationConfigService,
                                  final CaptchaService captchaService,
                                  final EpochKeyBundleDtoMapper epochKeyBundleDtoMapper,
                                  final ICryptoServerGrpcClient cryptoServerClient) {

        this.registrationService = registrationService;
        this.serverConfigurationService = serverConfigurationService;
        this.applicationConfigService = applicationConfigService;
        this.captchaService = captchaService;
        this.epochKeyBundleDtoMapper = epochKeyBundleDtoMapper;
        this.cryptoServerClient = cryptoServerClient;

    }

    @Override
    public ResponseEntity<RegisterResponseDto> register(RegisterVo registerVo) throws RobertServerException {

          // TODO: Enable this when the capcha becomes
//        if (StringUtils.isEmpty(registerVo.getCaptcha())) {
//            log.error("The 'captcha' is required.");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }

          // TODO: Unable this when the key sharing is enabled
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        GenerateIdentityResponse identity = response.get();

        Registration registration = Registration.builder()
                .permanentIdentifier(identity.getIdA().toByteArray())
                .build();
        
        Optional<Registration> registreted = this.registrationService.saveRegistration(registration);
                
        
        if (registreted.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(processRegistration(identity));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private RegisterResponseDto processRegistration(GenerateIdentityResponse identity) throws RobertServerException {

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

        EphemeralTupleRequest request = EphemeralTupleRequest.newBuilder()
                .setCountryCode(ByteString.copyFrom(new byte[] {countrycode}))
                .setCurrentEpochID(currentEpochId)
                .setIdA(ByteString.copyFrom(identity.getIdA().toByteArray()))
                .setNumberOfEpochsToGenerate(numberOfEpochs)
                .build();


        List<EphemeralTupleResponse> ephTuples = this.cryptoServerClient.generateEphemeralTuple(request);

        if (CollectionUtils.isEmpty(ephTuples)) {
            log.warn("Could not generate (EBID, ECC) tuples");
            throw new RobertServerException(MessageConstants.ERROR_OCCURED);
        }

        registerResponseDto.setIdsForEpochs(
                this.epochKeyBundleDtoMapper.convert(ephTuples));

        registerResponseDto.setServerPublicECDHKeyForKey(Base64.encode(identity.getServerPublicKeyForKey().toByteArray()));

        registerResponseDto.setKey(Base64.encode(identity.getEncryptedSharedKey().toByteArray()));
        return registerResponseDto;
    }

}