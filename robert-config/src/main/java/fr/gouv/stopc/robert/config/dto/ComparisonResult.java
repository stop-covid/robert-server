package fr.gouv.stopc.robert.config.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * VO used to store the comparison result of configuration parameters
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComparisonResult implements Comparable<ComparisonResult> {

	/**
	 * Configuration parameter name
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private String key;

	/**
	 * Current value of the parameter
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private String currentValue;

	/**
	 * New value of the parameter
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private String newValue;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(ComparisonResult o) {
		return o.getKey().compareTo(key);
	}

}
