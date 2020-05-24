package fr.gouv.stopc.robert.config.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import fr.gouv.stopc.robert.config.dto.ConfigurationHistory;
import fr.gouv.stopc.robert.config.service.IRobertConfigurationService;
import fr.gouv.stopc.robert.config.util.IConfigurationUpdateResults;

/**
 * REST controller that allows to :<br>
 * - update the configuration of an application for a given profile<br>
 * - retrieve the modification history of an application for a given profile
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@RestController
@RequestMapping("/api/v1/config")
public class RobertConfigurationController {

	/**
	 * The service for configuration manipulations
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private IRobertConfigurationService service;

	/**
	 * A Yaml encoder/decoder
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private Yaml yamlCodec = new Yaml();

	/**
	 * Spring injection constructor
	 * 
	 * @param service the <code>IRobertConfigurationService</code> bean to use
	 * @since 0.0.1-SNAPSHOT
	 */
	public RobertConfigurationController(IRobertConfigurationService service) {
		this.service = service;
	}

	/**
	 * Function updating the configuration of an application for a given profile
	 * 
	 * @param appName           the name of the application to update
	 * @param profile           the profile the application is running on
	 * @param yamlConfiguration the configuration in YAML format
	 * @return a message containing the result of the update operation
	 */
	@PutMapping(path = "/{appName}/{profile}", consumes = "application/x-yaml")
	public ResponseEntity<String> updateConfiguration(@PathVariable("appName") String appName,
			@PathVariable("profile") String profile, @RequestBody String yamlConfiguration) {

		// Compute the key / value version of the configuration
		Map<String, Object> configuration = yamlCodec.load(yamlConfiguration);
<<<<<<< HEAD
		
=======
>>>>>>> branch 'feat-hot-reload-configuration' of git@plant-stopcovid.gitlab.inria.fr:stopcovid19/robert-server.git
		String result = service.updateConfiguration(appName, profile, configuration);
		HttpStatus status = HttpStatus.OK;
		if (IConfigurationUpdateResults.CONFIGURATION_UPDATE_FAILED.equals(result)) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return ResponseEntity.status(status).body(result);
	}

	/**
	 * Retrieve the history of modifications of an application for a given
	 * application name and Spring profile
	 * 
	 * @param appName the name of the application
	 * @param profile the profile the application is running on
	 * @return the history of modifications
	 * @since 0.0.1-SNAPSHOT
	 */
	@GetMapping(path = "/history/{appName}/{profile}")
	public ResponseEntity<ConfigurationHistory> getConfigurationHistory(@PathVariable("appName") String appName,
			@PathVariable(name = "profile") String profile) {
		return ResponseEntity.ok(service.getHistory(appName, profile));
	}
}
