package fr.gouv.stopc.robert.config.service;

import java.util.Map;

import fr.gouv.stopc.robert.config.dto.ConfigurationHistory;

/**
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
public interface IRobertConfigurationService {

	/**
	 * Function retrieving the configuration history entries for an application with
	 * a given name for a given profile
	 * 
	 * @param appName the name of the application
	 * @param profile the profile on which the application is running
	 * @return the history entries
	 * @since 0.0.1-SNAPSHOT
	 */
	ConfigurationHistory getHistory(String appName, String profile);

	/**
	 * Function updating the configuration of an application with a given name for a
	 * given profile
	 * 
	 * @param appName       the name of the application
	 * @param profile       the profile on which the application is running
	 * @param configuration the new configuration to take into account
	 * @return a message telling what is the result of the update
	 * @since 0.0.1-SNAPSHOT
	 */
	String updateConfiguration(String appName, String profile, Map<String, Object> configuration);
}
