package fr.gouv.stopc.robert.config.client;

import feign.RequestLine;

/**
 * Interface defining REST method that can be called from ROBERT-CONFIG
 * 
 * @author plant-stopconvid
 * @version 0.0.1-SNAPSHOT
 */
public interface IRobertAppClient {

	/**
	 * Do a refresh of the target application configuration.
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	@RequestLine("POST /actuator/refresh")
	void refresh();

}
