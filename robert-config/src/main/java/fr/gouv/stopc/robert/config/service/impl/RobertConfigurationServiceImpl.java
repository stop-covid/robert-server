package fr.gouv.stopc.robert.config.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

import feign.Feign;
import fr.gouv.stopc.robert.config.client.IRobertAppClient;
import fr.gouv.stopc.robert.config.dao.IRobertConfigurationDao;
import fr.gouv.stopc.robert.config.dto.ConfigurationHistoryEntry;
import fr.gouv.stopc.robert.config.dto.FunctionalConfiguration;
import fr.gouv.stopc.robert.config.service.IRobertConfigurationService;
import fr.gouv.stopc.robert.config.util.IConfigurationUpdateResults;

/**
 * Default implementation of the <code>IRobertConfigurationService</code>
 * interface.
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Service(value = "DefaultRobertConfigurationService")
public class RobertConfigurationServiceImpl implements IRobertConfigurationService {

	/**
	 * Pattern to use for client URL
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private final static String clientUrlPattern = "http://%s:%d";

	/**
	 * The DAO for the access to the configuration
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private IRobertConfigurationDao dao;

	/**
	 * The application instances discovery client
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private EurekaClient discoveryClient;

	/**
	 * Spring injection constructor
	 * 
	 * @param dao             the <code>IRobertConfigurationDao</code> bean to use
	 * @param discoveryClient the <code>EurekaClient</code> bean to use
	 * @since 0.0.1-SNAPSHOT
	 */
	public RobertConfigurationServiceImpl(IRobertConfigurationDao dao, EurekaClient discoveryClient) {
		this.discoveryClient = discoveryClient;
		this.dao = dao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String updateConfiguration(String profile, FunctionalConfiguration configuration) {
		// Refresh the configuration associated to the profile and return the list of
		// applications to notify
		List<String> updateResult = dao.updateConfiguration(profile, configuration);

		if (updateResult == null) {
			// Application list is null -> update failed
			return IConfigurationUpdateResults.CONFIGURATION_UPDATE_FAILED;
		}

		if (updateResult.isEmpty()) {
			// Application list is empty -> update ok but parameters had same value as
			// before
			return IConfigurationUpdateResults.NOTHING_TO_UPDATE;
		}

		for (String appName : updateResult) {
			// Retrieve all instances of the application with name appName
			Application eurekaApp = discoveryClient.getApplication(appName);
			if (eurekaApp == null || CollectionUtils.isEmpty(eurekaApp.getInstances())) {
				// Application is not registered on Eureka or no instance is available
				return IConfigurationUpdateResults.CONFIGURATION_UPDATED_NO_INSTANCE_TO_REFRESH;
			} else {
				// For each instance call the /actuator/refresh endpoint to reload the
				// configuration
				for (InstanceInfo instanceApp : eurekaApp.getInstances()) {
					// Retrieve the hostname and port
					String hostName = instanceApp.getHostName();
					int port = instanceApp.getPort();
					// Compute the URL
					String clientUrl = String.format(clientUrlPattern, hostName, port);
					// Instantiante the REST client
					IRobertAppClient refreshClient = Feign.builder().target(IRobertAppClient.class, clientUrl);
					// Call the refresh method
					refreshClient.refresh();
				}
			}
		}
		// Configuration is updated and applications instances are refreshed
		return IConfigurationUpdateResults.CONFIGURATION_UPDATED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ConfigurationHistoryEntry> getHistory(String profile) {
		return dao.getHistory(profile);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FunctionalConfiguration getConfiguration(String profile) {
		return dao.getConfiguration(profile);
	}

}
