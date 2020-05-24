package fr.gouv.stopc.robertserver.ws.controller.impl;

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import fr.gouv.stopc.robert.crypto.grpc.server.messaging.GetIdFromAuthResponse;
import fr.gouv.stopc.robertserver.ws.dto.DeleteHistoryResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;

import fr.gouv.stopc.robert.crypto.grpc.server.client.service.ICryptoServerGrpcClient;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.MacValidationForTypeRequest;
import fr.gouv.stopc.robert.server.common.DigestSaltEnum;
import fr.gouv.stopc.robertserver.database.model.Registration;
import fr.gouv.stopc.robertserver.database.service.IRegistrationService;
import fr.gouv.stopc.robertserver.ws.controller.IUnregisterController;
import fr.gouv.stopc.robertserver.ws.dto.UnregisterResponseDto;
import fr.gouv.stopc.robertserver.ws.service.AuthRequestValidationService;
import fr.gouv.stopc.robertserver.ws.vo.UnregisterRequestVo;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


@Service
public class UnregisterControllerImpl implements IUnregisterController {

    private final IRegistrationService registrationService;
    private final AuthRequestValidationService authRequestValidationService;

    @Inject
    public UnregisterControllerImpl(final IRegistrationService registrationService,
                                    final AuthRequestValidationService authRequestValidationService) {

        this.registrationService = registrationService;
        this.authRequestValidationService = authRequestValidationService;
    }

    @Override
    public ResponseEntity<UnregisterResponseDto> unregister(UnregisterRequestVo unregisterRequestVo) {
        AuthRequestValidationService.ValidationResult<GetIdFromAuthResponse> validationResult =
                authRequestValidationService.validateRequestForAuth(unregisterRequestVo);

        if (Objects.nonNull(validationResult.getError())) {
            return ResponseEntity.badRequest().build();
        }

        GetIdFromAuthResponse authResponse = validationResult.getResponse();

        Optional<Registration> registrationRecord = this.registrationService.findById(authResponse.getIdA().toByteArray());

        if (registrationRecord.isPresent()) {
            Registration record = registrationRecord.get();

            // Unregister by deleting
            registrationService.delete(record);

            UnregisterResponseDto response = UnregisterResponseDto.builder().success(true).build();

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
