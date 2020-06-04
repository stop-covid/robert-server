package fr.gouv.stopc.robertserver.ws.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author plant-stopcovid
 *
 */
@Getter
@Setter
@Component
@RefreshScope
@ConfigurationProperties("tracing.app")
public class MobileAppConfiguration {

	/**
	 * Number of calls per day made by the mobile app to check the status
	 */
	private Integer checkStatusFrequency;

	/**
	 * Hello messages retention duration on the mobile app
	 */
	private Integer dataRetentionPeriod;

	/**
	 * Contagious period duration before symptoms span
	 */
	private Integer preSymptomsSpan;

	/**
	 * Beginning hour of the day to start status requests
	 */
	private Integer minHourContactNotif;

	/**
	 * Ending hour of the day to start status requests
	 */
	private Integer maxHourContactNotif;

	/**
	 * Availability of the App
	 */
	private Boolean appAvailability;

	/**
	 * Force upgrade of the mobile App
	 */
	private Boolean appUpgrade;

}
