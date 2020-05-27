package fr.gouv.stopc.robert.config.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class SignalCalibration {

	private String model;
	
	private Integer emissionGain;
	
	private Integer receptionGain;

	@Override
	public String toString() {
		return String.format("{model: %s, emissionGain: %d, receptionGain: %d}", model, emissionGain, receptionGain);
	}
	
}
