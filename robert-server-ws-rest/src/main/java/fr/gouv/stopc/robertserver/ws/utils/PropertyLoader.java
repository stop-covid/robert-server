package fr.gouv.stopc.robertserver.ws.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PropertyLoader {

	@Value("${robert.crypto.server.host}")
	private String cryptoServerHost;

	@Value("${robert.crypto.server.port}")
	private String cryptoServerPort;

	/**
	 *
	 * TODO: Remove before any public use.
	 * This should remain only for development uses.
	 */
	@Value("${captcha.magicnumber}")
	private String magicNumber;

	/**
	 * 
	 * @return the verification URL for the captcha
	 */
	@Value("${captcha.verify.url}")
	private String captchaVerificationUrl;

	/**
	 * 
	 * @return the secret to be sent to the captcha server along with challenge response
	 */
	@Value("${captcha.secret}")
	private String captchaSecret;

	/**
	 * 
	 * @return the hostname of the site to check against the response from the captcha server
	 */
	@Value("${captcha.hostname}")
	private String captchaHostname;

    @Value("${submission.code.server.host}")
    private String serverCodeHost;

    @Value("${submission.code.server.port}")
    private String serverCodePort;

    @Value("${submission.code.server.verify.path}")
    private String serverCodeVerificationPath;
}
