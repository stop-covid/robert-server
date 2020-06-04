package fr.gouv.stopc.robertserver.ws.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration of the scoring algorithm to send to the mobile app
 * 
 * @author plant-stopcovid
 */
@Getter
@Setter
@Component
@RefreshScope
@ConfigurationProperties(prefix = "tracing.ble")
public class ScoringAlgorithmConfiguration {

	/**
	 * Maximum number of contacts by epoch
	 */
	private Integer simultaneousContacts;

	/**
	 * Receiving & Transmitting gain to apply for each phone model
	 */
	private List<SignalCalibration> signalCalibrationPerModel = new ArrayList<>();

	/**
	 * Window duration
	 */
	private Integer tWin;

	/**
	 * Overlap between two sliding windows
	 */
	private Integer tOverlap;

	/**
	 * Weight for measures depending on received packet in a window
	 */
	private List<Integer> delta = new ArrayList<>();

	/**
	 * Limit power of the BLE signal
	 */
	private Integer p0;

	/**
	 * Constant used to average the risk
	 */
	private Integer b;

	/**
	 * Max number of message per window per contact per epoch
	 */
	private Integer maxSampleSize;

	/**
	 * Threshold under which the risk is low
	 */	
	private Float riskThresholdLow;

	/**
	 * Threshold beyond which the risk is high
	 */
	private Float riskThresholdHigh;

	/**
	 * Minimum score of risk
	 */
	private Integer riskMin;

	/**
	 * Maximum score of risk
	 */
	private Integer riskMax;

	/**
	 * Minimum duration to take into account the contact
	 */
	private Integer dThreshold;

	/**
	 * Threshold for the RSSI
	 */
	private Integer rssiThreshold;

	/**
	 * Flag indicating if peaks need to be deleted
	 */
	private Boolean tagPeak;

	/**
	 * Flag indicating if RSSI values needs to be corrected
	 */
	private Boolean flagCalib;

	/**
	 * Way the informations are provided
	 */
	private String flagMode;
}
