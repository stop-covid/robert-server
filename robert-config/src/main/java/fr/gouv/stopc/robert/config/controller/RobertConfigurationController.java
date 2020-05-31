package fr.gouv.stopc.robert.config.controller;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.gouv.stopc.robert.config.dto.ConfigurationHistoryEntry;
import fr.gouv.stopc.robert.config.dto.FunctionalConfiguration;
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
	 * Spring injection constructor
	 * 
	 * @param service the <code>IRobertConfigurationService</code> bean to use
	 * @since 0.0.1-SNAPSHOT
	 */
	public RobertConfigurationController(IRobertConfigurationService service) {
		this.service = service;
	}

	/**
	 * Update the functional configuration for a given profile
	 * 
	 * @param appName          the name of the application to update
	 * @param profile          the profile the application is running on
	 * @param newConfiguration the configuration in json format
	 * @return a message containing the result of the update operation
	 */
	@RolesAllowed("${robert.config.authorized-roles}")
	@PutMapping(path = "/{profile}")
	public ResponseEntity<String> updateConfiguration(@PathVariable("profile") String profile,
			@RequestBody FunctionalConfiguration newConfiguration) {
		// Compute the key / value version of the configuration
		String result = service.updateConfiguration(profile, newConfiguration);
		HttpStatus status = HttpStatus.OK;
		if (IConfigurationUpdateResults.CONFIGURATION_UPDATE_FAILED.equals(result)) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return ResponseEntity.status(status).body(result);
	}

	/**
	 * Retrieve the history of modifications of the functional configuration for a
	 * given Spring profile
	 * 
	 * @param profile the spring profile
	 * @return the history of modifications
	 * @since 0.0.1-SNAPSHOT
	 */
	@RolesAllowed("functionnal-admin")
	@GetMapping(path = "/history/{profile}")
	public ResponseEntity<List<ConfigurationHistoryEntry>> getFunctionalConfigurationHistory(
			@PathVariable(name = "profile") String profile) {
		return ResponseEntity.ok(service.getHistory(profile));
	}

	/**
	 * Retrieve functionnal configuration for a Spring profile
	 * 
	 * @param profile the spring profile
	 * @return the history of modifications
	 * @since 0.0.1-SNAPSHOT
	 */
	@RolesAllowed("functionnal-admin")
	@GetMapping(path = "/{profile}")
	public ResponseEntity<FunctionalConfiguration> getFunctionalConfiguration(
			@PathVariable(name = "profile") String profile) {
		return ResponseEntity.ok(service.getConfiguration(profile));
	}
}
