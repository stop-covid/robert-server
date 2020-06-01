package fr.gouv.stopc.robert.config.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class FunctionalConfiguration {

	private AccountManagement accountManagement;

	private ProximityTracing proximityTracing;

}
