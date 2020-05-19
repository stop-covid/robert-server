package fr.gouv.stopc.robertserver.ws.controller.impl;

import java.util.Objects;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import fr.gouv.stopc.robertserver.ws.controller.IReportController;
import fr.gouv.stopc.robertserver.ws.dto.ReportBatchResponseDto;
import fr.gouv.stopc.robertserver.ws.dto.VerifyResponseDto;
import fr.gouv.stopc.robertserver.ws.exception.RobertServerBadRequestException;
import fr.gouv.stopc.robertserver.ws.exception.RobertServerException;
import fr.gouv.stopc.robertserver.ws.exception.RobertServerUnauthorizedException;
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

    private ContactDtoService contactDtoService;

    private RestTemplate restTemplate;

    @Value("${submission.code.server.host}")
    private String serverCodeHost;

    @Value("${submission.code.server.port}")
    private String serverCodePort;

    @Value("${submission.code.server.verify.path}")
    private String serverCodeVerificationUri;

    @Inject
    public ReportControllerImpl(ContactDtoService contactDtoService, RestTemplate restTemplate) {

        this.contactDtoService = contactDtoService;
        this.restTemplate = restTemplate;
    }

    private boolean areBothFieldsPresent(ReportBatchRequestVo reportBatchRequestVo) {
        return !CollectionUtils.isEmpty(reportBatchRequestVo.getContacts())
                && StringUtils.isNotEmpty(reportBatchRequestVo.getContactsAsBinary());
    }

    private boolean areBothFieldsAbsent(ReportBatchRequestVo reportBatchRequestVo) {
        return CollectionUtils.isEmpty(reportBatchRequestVo.getContacts())
                && StringUtils.isEmpty(reportBatchRequestVo.getContactsAsBinary());
    }

    @Override
    public ResponseEntity<ReportBatchResponseDto> reportContactHistory(ReportBatchRequestVo reportBatchRequestVo) throws RobertServerException {

        if (CollectionUtils.isEmpty(reportBatchRequestVo.getContacts())) {
            log.warn("No contacts in request");
            return ResponseEntity.badRequest().build();
        }

        if (areBothFieldsPresent(reportBatchRequestVo)) {
            log.warn("Contacts and ContactsAsBinary are both present");
            return ResponseEntity.badRequest().build();
        } else if (areBothFieldsAbsent(reportBatchRequestVo)) {
            log.warn("Contacts and ContactsAsBinary are absent");
            return ResponseEntity.badRequest().build();
        }

        checkValidityToken(reportBatchRequestVo.getToken());

        contactDtoService.saveContacts(reportBatchRequestVo.getContacts());

        ReportBatchResponseDto reportBatchResponseDto = ReportBatchResponseDto.builder().message(MessageConstants.SUCCESSFUL_OPERATION.getValue()).success(Boolean.TRUE).build();
        return ResponseEntity.ok(reportBatchResponseDto);
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

        ResponseEntity<VerifyResponseDto> response = null;
        try {
            response = restTemplate.getForEntity(constructUri(token), VerifyResponseDto.class);
        } catch (RestClientException e) {
            log.error("Unable to verify the token due to {}", e.getMessage());
            throw new RobertServerBadRequestException(MessageConstants.ERROR_OCCURED.getValue());
        }

        if (Objects.isNull(response) ||  !response.getBody().isValid()) {
            log.warn("Verifying the token failed");
            throw new RobertServerUnauthorizedException(MessageConstants.INVALID_AUTHENTICATION.getValue());
        }
        
        log.info("Verifying the token succeeded");
    }

    private String getCodeType(String token) {

        return token.length() == 6 ? "2" : "1";
    }

    private String constructUri(String token) {

        return UriComponentsBuilder.newInstance().scheme("http").host(serverCodeHost).port(serverCodePort)
                .path(serverCodeVerificationUri)
                .queryParam("code", token)
                .queryParam("type", getCodeType(token))
                .build().toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    class VerifyRequestVo {

        private String code;

        private String type;

    }

}
