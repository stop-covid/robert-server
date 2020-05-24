package fr.gouv.stopc.robert.config.dao;

import java.util.List;
import java.util.Map;

import fr.gouv.stopc.robert.config.dto.ConfigurationHistoryEntry;

/**
 * Interface defining the DAO method for configurations
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
public interface IRobertConfigurationDao {

	/**
	 * Function retrieving the configuration history entries for an application with
	 * a given name for a given profile
	 * 
	 * @param appName the name of the application
	 * @param profile the profile on which the application is running
	 * @return the history entries
	 * @since 0.0.1-SNAPSHOT
	 */
	List<ConfigurationHistoryEntry> getHistory(String appName, String profile);

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
