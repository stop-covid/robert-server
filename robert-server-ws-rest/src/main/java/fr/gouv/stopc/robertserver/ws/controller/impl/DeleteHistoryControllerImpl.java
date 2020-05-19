package fr.gouv.stopc.robertserver.ws.controller.impl;

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;

import fr.gouv.stopc.robert.crypto.grpc.server.client.service.ICryptoServerGrpcClient;
import fr.gouv.stopc.robert.crypto.grpc.server.messaging.MacValidationForTypeRequest;
import fr.gouv.stopc.robert.server.common.DigestSaltEnum;
import fr.gouv.stopc.robertserver.database.model.Registration;
import fr.gouv.stopc.robertserver.database.service.IRegistrationService;
import fr.gouv.stopc.robertserver.ws.controller.IDeleteHistoryController;
import fr.gouv.stopc.robertserver.ws.dto.DeleteHistoryResponseDto;
import fr.gouv.stopc.robertserver.ws.exception.RobertServerException;
import fr.gouv.stopc.robertserver.ws.service.AuthRequestValidationService;
import fr.gouv.stopc.robertserver.ws.utils.MessageConstants;
import fr.gouv.stopc.robertserver.ws.vo.DeleteHistoryRequestVo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeleteHistoryControllerImpl implements IDeleteHistoryController {

	private final IRegistrationService registrationService;

	private final AuthRequestValidationService authRequestValidationService;

	private final ICryptoServerGrpcClient cryptoServerClient;

	@Inject
	public DeleteHistoryControllerImpl(final ICryptoServerGrpcClient cryptoServerClient,
									   final IRegistrationService registrationService,
									   final AuthRequestValidationService authRequestValidationService) {

		this.cryptoServerClient = cryptoServerClient;
		this.registrationService = registrationService;
		this.authRequestValidationService = authRequestValidationService;
	}

	@Override
	public ResponseEntity<DeleteHistoryResponseDto> deleteHistory(DeleteHistoryRequestVo deleteHistoryRequestVo) throws RobertServerException {

		Optional<ResponseEntity> entity = this.authRequestValidationService.validateRequestForAuth(deleteHistoryRequestVo, new DeleteHistoryMacValidator(this.cryptoServerClient),
				new DeleteHistoryAuthenticatedRequestHandler());

		if (entity.isPresent()) {
			return entity.get();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	private class DeleteHistoryMacValidator implements AuthRequestValidationService.IMacValidator {

		private final ICryptoServerGrpcClient cryptoServerClient;

		public DeleteHistoryMacValidator(final ICryptoServerGrpcClient cryptoServerClient) {

			this.cryptoServerClient = cryptoServerClient;
		}

		@Override
		public boolean validate(byte[] key, byte[] toCheck, byte[] mac) {

			boolean res;

			try {
				MacValidationForTypeRequest request = MacValidationForTypeRequest.newBuilder().setKa(ByteString.copyFrom(key)).setDataToValidate(ByteString.copyFrom(toCheck))
					.setMacToMatchWith(ByteString.copyFrom(mac)).setPrefixe(ByteString.copyFrom(new byte[] { DigestSaltEnum.DELETE_HISTORY.getValue() })).build();
				res = this.cryptoServerClient.validateMacForType(request);
			} catch (Exception e) {
				log.error(e.getMessage());
				res = false;
			}
			return res;
		}

	}

	private class DeleteHistoryAuthenticatedRequestHandler implements AuthRequestValidationService.IAuthenticatedRequestHandler {

		@Override
		public Optional<ResponseEntity> validate(Registration record, int epoch) {

			if (Objects.isNull(record)) {
				return Optional.of(ResponseEntity.notFound().build());
			}

			// Clear ExposedEpoch list then save the updated registration
			if (Objects.nonNull(record.getExposedEpochs())) {
				record.getExposedEpochs().clear();
				DeleteHistoryControllerImpl.this.registrationService.saveRegistration(record);
			}

			DeleteHistoryResponseDto statusResponse = DeleteHistoryResponseDto.builder().success(true).message(MessageConstants.SUCCESSFUL_OPERATION.getValue()).build();

			return Optional.of(ResponseEntity.ok(statusResponse));
		}

	}

}
