package fr.gouv.stopc.robert.config.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ProximityTracing {
	
	private App app;
	
	private Ble ble;
	
	private Float riskThreshold;
	
	private Integer rssi1m;
	
	private Integer mu0;
	
	private Float r0;
}
