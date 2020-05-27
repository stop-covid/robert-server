package fr.gouv.stopc.robert.config.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Ble {

	@JsonProperty("nContacts")
	private Integer nContacts;

	private List<SignalCalibration> signalCalibrationPerModel = new ArrayList<>();

	private Integer twin;

	@JsonProperty("tOverlap")
	private Integer tOverlap;

	private List<Integer> delta = new ArrayList<>();

	private Integer p0;

	private Integer minSampling;

	private Integer a;

	private Integer b;

	private Integer maxSampleSize;

	private Float riskSeuilLow;

	private Float riskSeuilMax;

	private Integer riskMin;

	private Integer riskMax;

	@JsonProperty("dSeuil")
	private Integer dSeuil;

	private Integer rssiSeuil;

	private Integer g0tx;

	private Integer tagPeaks;

	private Integer tagCalib;

}
