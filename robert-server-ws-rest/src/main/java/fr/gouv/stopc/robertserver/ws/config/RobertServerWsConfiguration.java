package fr.gouv.stopc.robertserver.ws.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import fr.gouv.stopc.robert.crypto.grpc.server.client.service.ICryptoServerGrpcClient;

@Component
public class RobertServerWsConfiguration {

@Inject
	public RobertServerWsConfiguration(ICryptoServerGrpcClient cryptoServerClient, ApplicationConfig config) {
		cryptoServerClient.init(config.getRobertCryptoServerHost(), Integer.parseInt( config.getRobertCryptoServerPort()));
	}

	@Bean
	public RestTemplate restTemplate() {

		return new RestTemplate();
	}
}
