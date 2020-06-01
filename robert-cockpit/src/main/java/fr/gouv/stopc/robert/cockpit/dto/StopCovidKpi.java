package fr.gouv.stopc.robert.cockpit.dto;

import lombok.Data;

/**
 * 
 * @author MROUANET
 *
 */
@Data
public class StopCovidKpi {

	private Long nbRegisteredUsers;

	private Long nbActiveUsers;

	private Long nbContaminatedUsers;

	private Long nbAlertedUsers;

	private Long nbExposedUsers;

	private Long nbUsersHavingDeletedHistory;

	private Long nbUsersHavingLeftViaApp;

	/**
	 * Constructor
	 * 
	 * @param rsKpi
	 * @param scsKpi
	 * @param epKpi
	 */
	public StopCovidKpi(RobertServerKpi rsKpi, SubmissionCodeServerKpi scsKpi, EndPointKpi epKpi) {

		// Fill Robert Server Kpis if present
		if (rsKpi != null) {

		}

		// Fill Submission Code Server Kpis if present
		if (scsKpi != null) {

		}

		// Fill Endpoints Kpis if present
		if (epKpi != null) {

		}
	}

}
