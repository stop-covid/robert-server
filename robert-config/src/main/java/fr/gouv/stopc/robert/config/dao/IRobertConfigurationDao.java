package fr.gouv.stopc.robert.config.dao;

import java.util.List;

import fr.gouv.stopc.robert.config.dto.ConfigurationHistoryEntry;
import fr.gouv.stopc.robert.config.dto.FunctionalConfiguration;

/**
 * Interface defining the DAO method for configurations
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
public interface IRobertConfigurationDao {

	/**
	 * Function retrieving the configuration history entries for a given profile
	 * 
	 * @param profile the spring profile name
	 * @return the history entries
	 * @since 0.0.1-SNAPSHOT
	 */
	List<ConfigurationHistoryEntry> getHistory(String profile);

	/**
	 * Function retrieving the configuration for a given profile
	 * 
	 * @param profile the spring profile name
	 * @return the functional configuration
	 * @since 0.0.1-SNAPSHOT
	 */
	FunctionalConfiguration getConfiguration(String profile);

	/**
	 * Function updating the configuration of an application with a given name for a
	 * given profile
	 * 
	 * @param profile       the profile on which the application is running
	 * @param configuration the new configuration to take into account
	 * @return list of application name to notify
	 * @since 0.0.1-SNAPSHOT
	 */
	List<String> updateConfiguration(String profile, FunctionalConfiguration configuration);
}
