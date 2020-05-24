package fr.gouv.stopc.robert.config.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 * 
 * @author plant-stopcovid
 *
 */
@Getter
@Configuration
public class RobertConfigurationServerConfig {

	@Value("${spring.cloud.config.server.git.uri}")
	private String gitUri;
	
}
