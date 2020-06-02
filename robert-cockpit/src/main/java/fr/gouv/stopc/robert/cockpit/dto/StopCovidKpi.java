package fr.gouv.stopc.robert.cockpit.dto;

import java.time.LocalDate;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * StopCovid Kpis
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Data
public class StopCovidKpi {

	@NotNull
	private LocalDate date;

	@Nullable
	private Long nbRegisteredUsers;

	@Nullable
	private Long nbActiveUsers;

	@Nullable
	private Long nbContaminatedUsers;

	@Nullable
	private Long nbAlertedUsers;

	@Nullable
	private Long nbExposedUsers;

	@Nullable
	private Long nbUsersHavingDeletedHistory;

	@Nullable
	private Long nbUsersHavingLeftViaApp;

	@Nullable
	private Long nbShortCodesUsed;

	@Nullable
	private Long nbLongCodesUsed;

	@Nullable
	private Long nbExpiredCodes;

	/**
	 * Constructor
	 * 
	 * @param rsKpi  the Robert Server Kpis
	 * @param scsKpi the Submission Code Server Kpis
	 * @param epKpi  the Endpoints supervision Kpis
	 * @since 0.0.1-SNAPSHOT
	 */
	public StopCovidKpi(@Nullable RobertServerKpi rsKpi, @Nullable SubmissionCodeServerKpi scsKpi,
			@Nullable EndPointKpi epKpi) {

		// Fill Robert Server Kpis if present
		if (rsKpi != null) {
			this.nbAlertedUsers = rsKpi.getNbAlertedUsers();
			if (rsKpi.getNbExposedButNotAtRiskUsers() != null && rsKpi.getNbAlertedUsers() != null) {
				this.nbExposedUsers = rsKpi.getNbExposedButNotAtRiskUsers() + rsKpi.getNbAlertedUsers();
			}
		}

		// Fill Submission Code Server Kpis if present
		if (scsKpi != null) {
			this.nbLongCodesUsed = scsKpi.getNbLongCodesUsed();
			this.nbShortCodesUsed = scsKpi.getNbShortCodesUsed();
			this.nbExpiredCodes = scsKpi.getNbExpiredCodes();
		}

		// Fill Endpoints Kpis if present
		if (epKpi != null) {
			this.nbRegisteredUsers = epKpi.getRegisterEndpointCalls();
			this.nbUsersHavingLeftViaApp = epKpi.getUnregisterEndpointCalls();
			this.nbUsersHavingDeletedHistory = epKpi.getDeleteHistoryEndpointCalls();
			this.nbContaminatedUsers = epKpi.getReportEndpointCalls();
			this.nbActiveUsers = epKpi.getStatusEndpointCalls();
		}
	}

}
