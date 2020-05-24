package fr.gouv.stopc.robert.config.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigurationHistoryEntry {

	private String message;
	
	private LocalDateTime date;
	
}
