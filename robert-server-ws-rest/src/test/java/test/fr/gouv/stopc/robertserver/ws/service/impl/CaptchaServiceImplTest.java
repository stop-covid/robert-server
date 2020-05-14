package test.fr.gouv.stopc.robertserver.ws.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import fr.gouv.stopc.robert.server.common.service.IServerConfigurationService;
import fr.gouv.stopc.robertserver.ws.dto.CaptchaDto;
import fr.gouv.stopc.robertserver.ws.service.impl.CaptchaServiceImpl;
import fr.gouv.stopc.robertserver.ws.vo.RegisterVo;

@ExtendWith(SpringExtension.class)
public class CaptchaServiceImplTest {

	private static final String HOSTNAME = "fr.gouv.stopc";

	@InjectMocks
	private CaptchaServiceImpl captchaServiceImpl;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private IServerConfigurationService serverConfigurationService;

	@Test
	public void testVerifyCaptchaWhenVoIsNull() {

		// When
		boolean isVerified = this.captchaServiceImpl.verifyCaptcha(null);

		// Then
		assertTrue(isVerified);
	}

	@Test
	public void testVerifyCaptchaWhenVoIsNotNull() {

		// Given
		CaptchaDto captchaDto = CaptchaDto.builder().success(true).challengeTimestamp(new Date()).appPackageName(HOSTNAME).build();
		when(this.restTemplate.postForEntity(any(String.class), any(), any())).thenReturn(ResponseEntity.ok(captchaDto));

		when(this.serverConfigurationService.getCaptchaAppPackageName()).thenReturn(HOSTNAME);
		when(this.serverConfigurationService.getCaptchaChallengeTimestampTolerance()).thenReturn(3600);

		// When
		boolean isVerified = this.captchaServiceImpl.verifyCaptcha(RegisterVo.builder().build());

		// Then
		assertTrue(isVerified);
	}

}
