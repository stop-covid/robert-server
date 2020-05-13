package test.fr.gouv.stopc.robertserver.ws.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.gouv.stopc.robertserver.ws.service.impl.CaptchaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import fr.gouv.stopc.robert.server.common.service.IServerConfigurationService;
import fr.gouv.stopc.robertserver.ws.vo.RegisterVo;


@ExtendWith(SpringExtension.class)
public class CaptchaServiceImplTest {

	@InjectMocks
	private CaptchaServiceImpl captchaServiceImpl;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private IServerConfigurationService serverConfigurationService;

	@Test
	public void testVerifyCaptchaWhenVoIsNull() {

		// When
		boolean isVerified = captchaServiceImpl.verifyCaptcha(null);

		// Then
		assertTrue(isVerified);
	}

	@Test
	public void testVerifyCaptchaWhenVoIsNotNull() {

		// When
		boolean isVerified = captchaServiceImpl.verifyCaptcha(RegisterVo.builder().build());

		// Then
		assertTrue(isVerified);
	}

}
