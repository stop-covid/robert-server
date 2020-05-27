package fr.gouv.stopc.robert.config.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComparisonResult implements Comparable<ComparisonResult>{

	private String key;
	
	private String currentValue;
	
	private String newValue;

	@Override
	public int compareTo(ComparisonResult o) {
		return o.getKey().compareTo(key);
	}
	
	
}
