package fr.gouv.stopc.robert.cockpit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Component;

import fr.gouv.stopc.robert.cockpit.dto.EndPointKpi;
import fr.gouv.stopc.robert.cockpit.dto.RobertServerKpi;
import fr.gouv.stopc.robert.cockpit.dto.SubmissionCodeServerKpi;

/**
 * Factory for Kpis generation tests
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Component
public class StopCovidKpiFactory {

	/**
	 * Generates Submission Code Server Kpis for a single day
	 * 
	 * @param date the date of the Kpis
	 * @return the list of Submission Code Server Kpi for the given date
	 * @since 0.0.1-SNAPSHOT
	 */
	public List<SubmissionCodeServerKpi> generateScsDayRangeKpi(LocalDate date) {
		List<SubmissionCodeServerKpi> result = new ArrayList<>();
		result.add(SubmissionCodeServerKpi.builder().date(date).nbExpiredCodes(1L).nbLongCodesUsed(1L)
				.nbShortCodesUsed(1L).build());
		return result;
	}

	/**
	 * Generates Robert Server Kpis for a single day
	 * 
	 * @param date the date of the Kpis
	 * @return the list of Robert Server Kpi for the given date
	 * @since 0.0.1-SNAPSHOT
	 */
	public List<RobertServerKpi> generateRsDayRangeKpi(LocalDate date) {
		List<RobertServerKpi> result = new ArrayList<>();
		result.add(RobertServerKpi.builder().date(date).nbAlertedUsers(1L).nbExposedButNotAtRiskUsers(1L).build());
		return result;
	}

	/**
	 * Generates Supervision Kpis for a single day
	 * 
	 * @param date the date of the Kpis
	 * @return the list of Robert Server Kpi for the given date
	 * @since 0.0.1-SNAPSHOT
	 */
	public List<EndPointKpi> generateDayRangeSupervisionKpi(LocalDate date) {
		List<EndPointKpi> result = new ArrayList<>();
		result.add(new EndPointKpi(date, new HashMap<>()));
		return result;
	}

	/**
	 * Generates Submission Code Server Kpis for a single week
	 * 
	 * @param from beginning date of the week
	 * @param to   ending date of the week
	 * @return a list containing 7 <code>SubmissionCodeServerKpi</code>
	 * @since 0.0.1-SNAPSHOT
	 */
	public List<SubmissionCodeServerKpi> generateScsWeekRangeKpi(LocalDate from, LocalDate to) {
		List<SubmissionCodeServerKpi> result = new ArrayList<>();
		for (LocalDate date = from; !date.isEqual(to); date = date.plusDays(1L)) {
			result.add(SubmissionCodeServerKpi.builder().date(date).nbExpiredCodes(Long.valueOf(date.getDayOfMonth()))
					.nbLongCodesUsed(Long.valueOf(date.getDayOfMonth()))
					.nbShortCodesUsed(Long.valueOf(date.getDayOfMonth())).build());
		}
		return result;
	}

	/**
	 * Generates Robert Server Kpis for a single week
	 * 
	 * @param from beginning date of the week
	 * @param to   ending date of the week
	 * @return a list containing 7 <code>RobertServerKpi</code>
	 * @since 0.0.1-SNAPSHOT
	 */
	public List<RobertServerKpi> generateRsWeekRangeKpi(LocalDate from, LocalDate to) {
		List<RobertServerKpi> result = new ArrayList<>();
		for (LocalDate date = from; !date.isEqual(to); date = date.plusDays(1L)) {
			result.add(RobertServerKpi.builder().date(date).nbAlertedUsers(Long.valueOf(date.getDayOfMonth()))
					.nbExposedButNotAtRiskUsers(Long.valueOf(date.getDayOfMonth())).build());
		}
		return result;
	}

	/**
	 * Generates Supervision Kpis for a single week
	 * 
	 * @param from beginning date of the week
	 * @param to   ending date of the week
	 * @return a list containing 7 <code>EndPointKpi</code>
	 * @since 0.0.1-SNAPSHOT
	 */
	public List<EndPointKpi> generateWeekRangeSupervisionKpi(LocalDate from, LocalDate to) {
		List<EndPointKpi> result = new ArrayList<>();
		for (LocalDate date = from; !date.isEqual(to); date = date.plusDays(1L)) {
			result.add(new EndPointKpi(date, new HashMap<>()));
		}
		return result;
	}

	/**
	 * Generates Submission Code Server Kpis for a single week with missing days
	 * 
	 * @param from beginning date of the week
	 * @param to   ending date of the week
	 * @return a list containing 5 <code>SubmissionCodeServerKpi</code>
	 * @since 0.0.1-SNAPSHOT
	 */
	public List<SubmissionCodeServerKpi> generateScsWeekRangeMissingDaysKpi(LocalDate from, LocalDate to) {
		List<SubmissionCodeServerKpi> result = new ArrayList<>();
		for (LocalDate date = from; !date.isEqual(to); date = date.plusDays(1L)) {
			result.add(SubmissionCodeServerKpi.builder().date(date).nbExpiredCodes(Long.valueOf(date.getDayOfMonth()))
					.nbLongCodesUsed(Long.valueOf(date.getDayOfMonth()))
					.nbShortCodesUsed(Long.valueOf(date.getDayOfMonth())).build());
		}
		return result.subList(0, 5);
	}

	/**
	 * Generates Robert Server Kpis for a single week with missing days
	 * 
	 * @param from beginning date of the week
	 * @param to   ending date of the week
	 * @return a list containing 5 <code>RobertServerKpi</code>
	 * @since 0.0.1-SNAPSHOT
	 */
	public List<RobertServerKpi> generateRsWeekRangeMissingDaysKpi(LocalDate from, LocalDate to) {
		List<RobertServerKpi> result = new ArrayList<>();
		for (LocalDate date = from; !date.isEqual(to); date = date.plusDays(1L)) {
			result.add(RobertServerKpi.builder().date(date).nbAlertedUsers(Long.valueOf(date.getDayOfMonth()))
					.nbExposedButNotAtRiskUsers(Long.valueOf(date.getDayOfMonth())).build());
		}
		return result.subList(0, 5);
	}

	/**
	 * Generates Supervision Kpis for a single week with missing days
	 * 
	 * @param from beginning date of the week
	 * @param to   ending date of the week
	 * @return a list containing 5 <code>EndPointKpi</code>
	 * @since 0.0.1-SNAPSHOT
	 */
	public List<EndPointKpi> generateSupervisionWeekRangeMissingDaysKpi(LocalDate from, LocalDate to) {
		List<EndPointKpi> result = new ArrayList<>();
		for (LocalDate date = from; !date.isEqual(to); date = date.plusDays(1L)) {
			result.add(new EndPointKpi(date, new HashMap<>()));
		}
		return result.subList(0, 5);
	}

	/**
	 * Generates Submission Code Server Kpis for a single month
	 * 
	 * @param from beginning date of the month
	 * @param to   ending date of the month
	 * @return a list containing 5 <code>SubmissionCodeServerKpi</code>
	 * @since 0.0.1-SNAPSHOT
	 */
	public List<SubmissionCodeServerKpi> generateScsMonthRangeKpi(LocalDate from, LocalDate to) {
		List<SubmissionCodeServerKpi> result = new ArrayList<>();
		for (LocalDate date = from; !date.isEqual(to); date = date.plusDays(1L)) {
			result.add(SubmissionCodeServerKpi.builder().date(date).nbExpiredCodes(Long.valueOf(date.getDayOfMonth()))
					.nbLongCodesUsed(Long.valueOf(date.getDayOfMonth()))
					.nbShortCodesUsed(Long.valueOf(date.getDayOfMonth())).build());
		}
		return result;
	}

	/**
	 * Generates Robert Server Kpis for a single month
	 * 
	 * @param from beginning date of the month
	 * @param to   ending date of the month
	 * @return a list containing 5 <code>RobertServerKpi</code>
	 * @since 0.0.1-SNAPSHOT
	 */
	public List<RobertServerKpi> generateRsMonthRangeKpi(LocalDate from, LocalDate to) {
		List<RobertServerKpi> result = new ArrayList<>();
		for (LocalDate date = from; !date.isEqual(to); date = date.plusDays(1L)) {
			result.add(RobertServerKpi.builder().date(date).nbAlertedUsers(Long.valueOf(date.getDayOfMonth()))
					.nbExposedButNotAtRiskUsers(Long.valueOf(date.getDayOfMonth())).build());
		}
		return result;
	}

	/**
	 * Generates Supervision Kpis for a single month
	 * 
	 * @param from beginning date of the month
	 * @param to   ending date of the month
	 * @return a list containing 5 <code>EndPointKpi</code>
	 * @since 0.0.1-SNAPSHOT
	 */
	public List<EndPointKpi> generateMonthRangeSupervisionKpi(LocalDate from, LocalDate to) {
		List<EndPointKpi> result = new ArrayList<>();
		for (LocalDate date = from; !date.isEqual(to); date = date.plusDays(1L)) {
			result.add(new EndPointKpi(date, new HashMap<>()));
		}
		return result;
	}

}
