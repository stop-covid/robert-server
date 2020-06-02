package fr.gouv.stopc.robert.cockpit.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SubmissionCodeServerKpi {

	private LocalDateTime date;

	private Long nbShortCodesUsed;

	private Long nbLongCodesUsed;

	private Long nbExpiredCodes;
}
