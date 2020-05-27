package fr.gouv.stopc.robert.config.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountManagement {

	private Integer appAutonomy;
	
	private Integer maxSimultaneousRegister;
	
}
