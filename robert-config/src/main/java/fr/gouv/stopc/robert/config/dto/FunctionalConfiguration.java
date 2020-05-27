package fr.gouv.stopc.robert.config.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FunctionalConfiguration {

	private AccountManagement accountManagement;

	private ProximityTracing proximityTracing;

}
