package fr.gouv.stopc.robertserver.ws.controller.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.protobuf.InvalidProtocolBufferException;

import fr.gouv.stopc.robertserver.ws.controller.IReportController;
import fr.gouv.stopc.robertserver.ws.dto.ReportBatchResponseDto;
import fr.gouv.stopc.robertserver.ws.exception.RobertServerBadRequestException;
import fr.gouv.stopc.robertserver.ws.exception.RobertServerException;
import fr.gouv.stopc.robertserver.ws.exception.RobertServerUnauthorizedException;
import fr.gouv.stopc.robertserver.ws.proto.ProtoStorage.ContactAsBinaryProto;
import fr.gouv.stopc.robertserver.ws.service.ContactDtoService;
import fr.gouv.stopc.robertserver.ws.utils.MessageConstants;
import fr.gouv.stopc.robertserver.ws.vo.ReportBatchRequestVo;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReportControllerImpl implements IReportController {

	private final ContactDtoService contactDtoService;

	private final RestTemplate restTemplate;

	@Value("${submission.code.server.host}")
	private String serverCodeHost;

	@Value("${submission.code.server.port}")
	private String serverCodePort;

	@Value("${submission.code.server.verify.path}")
	private String serverCodeVerificationUri;

	/**
	 * Spring Injection controller
	 * 
	 * @param contactDtoService the <code>ContactDtoService</code> bean to inject
	 * @param restTemplate the <code>RestTemplate</code> bean to inject
	 */
	public ReportControllerImpl(ContactDtoService contactDtoService, RestTemplate restTemplate) {
		this.contactDtoService = contactDtoService;
		this.restTemplate = restTemplate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<ReportBatchResponseDto> reportContactHistory(ReportBatchRequestVo reportBatchRequestVo)
			throws RobertServerException {

		// Check the content of contactsAsBinary and contacts fields. Only one must be
		// present.
		if (areBothFieldsPresent(reportBatchRequestVo)) {
			log.warn("contacts and contactsAsBinary are both present");
			return ResponseEntity.badRequest().build();
		} else if (areBothFieldsAbsent(reportBatchRequestVo)) {
			log.warn("contacts and contactsAsBinary are absent");
			return ResponseEntity.badRequest().build();
		}

		// Check the validity of the authentication token
		checkValidityToken(reportBatchRequestVo.getToken());

		if (CollectionUtils.isEmpty(reportBatchRequestVo.getContacts())) {
			// Case of a list of contact provided in contactsAsBinary
			try {
				// First decode the protobuf binary string
				ContactAsBinaryProto contactsProto = ContactAsBinaryProto
						.parseFrom(reportBatchRequestVo.getContactsAsBinary().getBytes());
				// List of contacts must not be empty
				if (CollectionUtils.isEmpty(contactsProto.getContactsList())) {
					log.warn("Contact list is empty in contactsAsBinary");
					return ResponseEntity.badRequest().build();
				}
				// Save the contact list
				contactDtoService.saveProtoContacts(contactsProto.getContactsList());
			} catch (InvalidProtocolBufferException e) {
				// Invalid content : malformed varint or negative byte length.
				log.warn("contactsAsBinary content is invalid : ", e);
				return ResponseEntity.badRequest().build();
			}
		} else {
			// Case of a list of contact provided in contacts
			contactDtoService.saveContacts(reportBatchRequestVo.getContacts());
		}
		// Build the response body
		ReportBatchResponseDto reportBatchResponseDto = ReportBatchResponseDto.builder()
				.message(MessageConstants.SUCCESSFUL_OPERATION.getValue()).success(Boolean.TRUE).build();
		return ResponseEntity.ok(reportBatchResponseDto);
	}
	
	/**
	 * Function checking if contacts and contactsAsBinary are present in a given
	 * <code>ReportBatchRequestVo</code>
	 * 
	 * @param reportBatchRequestVo the <code>ReportBatchRequestVo</code> to check
	 * @return true if both field are present else false
	 */
	private boolean areBothFieldsPresent(ReportBatchRequestVo reportBatchRequestVo) {
		return !CollectionUtils.isEmpty(reportBatchRequestVo.getContacts())
				&& StringUtils.isNotEmpty(reportBatchRequestVo.getContactsAsBinary());
	}
	
	/**
	 * Function checking if contacts and contactsAsBinary are missing in a given
	 * <code>ReportBatchRequestVo</code>
	 * 
	 * @param reportBatchRequestVo the <code>ReportBatchRequestVo</code> to check
	 * @return true if both field are missing else false
	 */
	private boolean areBothFieldsAbsent(ReportBatchRequestVo reportBatchRequestVo) {
		return CollectionUtils.isEmpty(reportBatchRequestVo.getContacts())
				&& StringUtils.isEmpty(reportBatchRequestVo.getContactsAsBinary());
	}

	private void checkValidityToken(String token) throws RobertServerException {

		if (StringUtils.isEmpty(token)) {
			log.warn("No token provided");
			throw new RobertServerBadRequestException(MessageConstants.INVALID_DATA.getValue());
		}

		if (token.length() != 6 && token.length() != 36) {
			log.warn("Token size is incorrect");
			throw new RobertServerBadRequestException(MessageConstants.INVALID_DATA.getValue());
		}
		// TODO: Enable this when the token validation service is available
		// ResponseEntity<VerifyResponseDto> response = restTemplate.getForEntity(constructUri(), VerifyResponseDto.class, initHttpEntity(token));

		//		boolean isValid = Optional.ofNullable(response).map(ResponseEntity::getBody).map(VerifyResponseDto::isValid).orElse(false);

		// TODO: If isValid == false, then throw exception (when token validation is enabled).
		if (false) {
			throw new RobertServerUnauthorizedException(MessageConstants.INVALID_AUTHENTICATION.getValue());
		}
	}

	private String getCodeType(String token) {

		return token.length() == 6 ? "6-alphanum" : "UUIDv4";
	}

	private HttpEntity<VerifyRequestVo> initHttpEntity(String token) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new HttpEntity(new VerifyRequestVo(token, getCodeType(token)), headers);
	}

	private String constructUri() {

		return UriComponentsBuilder.newInstance().scheme("http").host(serverCodeHost).port(serverCodePort).path(serverCodeVerificationUri).build().toString();
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	class VerifyRequestVo {

		private String code;

		private String type;

	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	class VerifyResponseDto {

		private boolean valid;

	}

}
