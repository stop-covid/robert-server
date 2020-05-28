package fr.gouv.stopc.robert.config.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Ble {

	private Integer simultaneousContacts;

	private List<SignalCalibration> signalCalibrationPerModel = new ArrayList<>();
	
	@JsonProperty("tWin")
	private Integer tWin;

	@JsonProperty("tOverlap")
	private Integer tOverlap;

	private List<Integer> delta = new ArrayList<>();

	private Integer p0;

	private Integer minSampling;

	private Integer b;

	private Integer maxSampleSize;

	private Float riskThresholdlLow;

	private Float riskThresholdMax;

	private Integer riskMin;

	private Integer riskMax;

	@JsonProperty("dThreshold")
	private Integer dThreshold;

	private Integer rssiThreshold;

	private Integer tagPeak;

	private Integer flagCalib;
	
	private String flagMode;

}
