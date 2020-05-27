package fr.gouv.stopc.robert.config.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConfigurationHistory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2896290882288808665L;
	
	private List<ConfigurationHistoryEntry> entries;
	
}
