package fr.gouv.stopc.robert.config.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProximityTracing {
	
	private App app;
	
	private Ble ble;
	
	private Float riskThreshold;
	
	private Integer rssi1m;
	
	private Integer mu0;
	
	private Float r0;
}
