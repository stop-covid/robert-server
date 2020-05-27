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
		// Refresh the configuration associated to the profile
		List<String> updateResult = dao.updateConfiguration(profile, configuration);

		if (updateResult == null) {
			return IConfigurationUpdateResults.CONFIGURATION_UPDATE_FAILED;
		}

		if (updateResult.isEmpty()) {
			return IConfigurationUpdateResults.NOTHING_TO_UPDATE;
		}

		for (String appName : updateResult) {
			// Retrieve all instances of the application
			Application eurekaApp = discoveryClient.getApplication(appName);
			// For each instance call the /actuator/refresh endpoint to reload the
			// configuration
			if (eurekaApp == null || CollectionUtils.isEmpty(eurekaApp.getInstances())) {
				return IConfigurationUpdateResults.CONFIGURATION_UPDATED_NO_INSTANCE_TO_REFRESH;
			} else {
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
		// Refresh configuration
		return IConfigurationUpdateResults.CONFIGURATION_UPDATED;
	}

	@Override
	public List<ConfigurationHistoryEntry> getHistory(String profile) {
		return dao.getHistory(profile);
	}

	@Override
	public FunctionalConfiguration getConfiguration(String profile) {
		return dao.getConfiguration(profile);
	}

}
