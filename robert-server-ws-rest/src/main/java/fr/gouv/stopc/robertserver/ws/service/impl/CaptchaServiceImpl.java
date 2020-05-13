package fr.gouv.stopc.robertserver.ws.service.impl;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import fr.gouv.stopc.robert.server.common.service.IServerConfigurationService;
import fr.gouv.stopc.robertserver.ws.dto.CaptchaDto;
import fr.gouv.stopc.robertserver.ws.service.CaptchaService;
import fr.gouv.stopc.robertserver.ws.vo.RegisterVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

	final static String URL_VERIFICATION = "https://www.google.com/recaptcha/api/siteverify";

	private RestTemplate restTemplate;

	private IServerConfigurationService serverConfigurationService;

	@Inject
	public CaptchaServiceImpl(RestTemplate restTemplate, IServerConfigurationService serverConfigurationRepository) {

		this.restTemplate = restTemplate;
		this.serverConfigurationService = serverConfigurationRepository;
	}

	@Override
	public boolean verifyCaptcha(final RegisterVo registerVo) {

		// TODO: Remove this forced returned value until reCATPCHA service is really
		// used but method verifyCaptcha
		// must be called for test
		return true || Optional.ofNullable(registerVo) //
				.map(item -> {

					HttpEntity<RegisterVo> request = new HttpEntity(
							new CaptchaVo(item.getCaptcha(), this.serverConfigurationService.getCaptchaSecret())
									.toString(),
							initHttpHeaders());

					Date sendingDate = new Date();

					ResponseEntity<CaptchaDto> response = restTemplate.postForEntity(URL_VERIFICATION, request,
							CaptchaDto.class);

					return Optional.ofNullable(response).map(ResponseEntity::getBody)
							.filter(captchaDto -> !Objects.isNull(captchaDto.getChallengeTimestamp()))
							.map(captchaDto -> {
								log.info("Result of CAPTCHA verification: {}", captchaDto);

								return isSuccess(captchaDto, sendingDate);
							}).orElse(false);

				}).orElse(false);
	}

	private HttpHeaders initHttpHeaders() {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return headers;
	}

	private boolean isSuccess(CaptchaDto captchaDto, Date sendingDate) {

		return this.serverConfigurationService.getCaptchaAppPackageName().equals(captchaDto.getAppPackageName()) && Math.abs(
				sendingDate.getTime() - captchaDto.getChallengeTimestamp().getTime()) <= this.serverConfigurationService
						.getCaptchaChallengeTimestampTolerance() * 1000;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	class CaptchaVo {

		private String secret;

		private String response;

	}

}
