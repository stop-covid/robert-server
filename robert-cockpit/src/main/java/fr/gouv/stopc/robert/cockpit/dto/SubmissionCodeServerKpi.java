package fr.gouv.stopc.robert.cockpit.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

/**
 * Kpis returned by the Submission Code Server
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
public class SubmissionCodeServerKpi {

	private LocalDate date;

	private Long nbShortCodesUsed;

	private Long nbLongCodesUsed;

	private Long nbExpiredCodes;
}
