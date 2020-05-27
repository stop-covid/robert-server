package fr.gouv.stopc.robert.config.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class App {

	private Integer checkStatusFrequency;

	private Integer dataRetentionPeriod;

}
