package fr.gouv.stopc.robert.config.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConfigurationUpdateRequest {

	private String appName;
	
	private String profile;
	
	private String propertiesAsBinary;
	
}
