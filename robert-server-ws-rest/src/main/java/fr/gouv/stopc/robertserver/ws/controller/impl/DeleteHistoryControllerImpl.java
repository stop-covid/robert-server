package fr.gouv.stopc.robertserver.ws.controller.impl;

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import fr.gouv.stopc.robert.crypto.grpc.server.messaging.DeleteIdResponse;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.GetIdFromAuthResponse;
import fr.gouv.stopc.robert.server.common.DigestSaltEnum;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import fr.gouv.stopc.robert.crypto.grpc.server.client.service.ICryptoServerGrpcClient;
import fr.gouv.stopc.robertserver.database.model.Registration;
import fr.gouv.stopc.robertserver.database.service.IRegistrationService;
import fr.gouv.stopc.robertserver.ws.controller.IDeleteHistoryController;
import fr.gouv.stopc.robertserver.ws.dto.DeleteHistoryResponseDto;
import fr.gouv.stopc.robertserver.ws.exception.RobertServerException;
import fr.gouv.stopc.robertserver.ws.service.AuthRequestValidationService;
import fr.gouv.stopc.robertserver.ws.vo.DeleteHistoryRequestVo;

@Service
public class DeleteHistoryControllerImpl implements IDeleteHistoryController {

	private final IRegistrationService registrationService;
	private final AuthRequestValidationService authRequestValidationService;

	@Inject
	public DeleteHistoryControllerImpl(final IRegistrationService registrationService,
									   final AuthRequestValidationService authRequestValidationService) {
		this.registrationService = registrationService;
		this.authRequestValidationService = authRequestValidationService;
	}

	@Override
	public ResponseEntity<DeleteHistoryResponseDto> deleteHistory(DeleteHistoryRequestVo deleteHistoryRequestVo)
			throws RobertServerException {
		AuthRequestValidationService.ValidationResult<DeleteIdResponse> validationResult =
				authRequestValidationService.validateRequestForUnregister(deleteHistoryRequestVo);

		if (Objects.nonNull(validationResult.getError())) {
			return ResponseEntity.badRequest().build();
		}

		DeleteIdResponse authResponse = validationResult.getResponse();
		Optional<Registration> registrationRecord = this.registrationService.findById(authResponse.getIdA().toByteArray());

		if (registrationRecord.isPresent()) {
			Registration record = registrationRecord.get();

			// Clear ExposedEpoch list then save the updated registration
			if (Objects.nonNull(record.getExposedEpochs())) {
				record.getExposedEpochs().clear();
				registrationService.saveRegistration(record);
			}

			DeleteHistoryResponseDto statusResponse = DeleteHistoryResponseDto.builder().success(true).build();

			return ResponseEntity.ok(statusResponse);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
