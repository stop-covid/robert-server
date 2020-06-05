package fr.gouv.stopc.robert.server.batch.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoringResult {

	private Integer duration;
	
	private Integer nbcontacts;
	
	private Double rssiScore;
	
}
