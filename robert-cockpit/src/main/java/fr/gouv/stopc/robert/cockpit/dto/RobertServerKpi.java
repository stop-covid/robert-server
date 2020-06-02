package fr.gouv.stopc.robert.cockpit.dto;

import java.time.LocalDate;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;

/**
 * Kpis returned by the Robert Server
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
public class RobertServerKpi {

	@NotNull
	private LocalDate date;

	@Nullable
	private Long nbAlertedUsers;

	@Nullable
	private Long nbExposedButNotAtRiskUsers;
}
