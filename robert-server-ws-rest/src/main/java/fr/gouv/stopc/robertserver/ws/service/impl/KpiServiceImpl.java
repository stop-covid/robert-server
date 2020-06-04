package fr.gouv.stopc.robertserver.ws.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.gouv.stopc.robert.server.common.service.IServerConfigurationService;
import fr.gouv.stopc.robert.server.common.utils.TimeUtils;
import fr.gouv.stopc.robertserver.database.service.IRegistrationService;
import fr.gouv.stopc.robertserver.ws.service.IKpiService;
import fr.gouv.stopc.robertserver.ws.vo.RobertServerKpi;

/**
 * 
 * @author plant-stopcovid
 *
 */
@Service
public class KpiServiceImpl implements IKpiService {

	/**
	 * The server configuration provider
	 */
	private IServerConfigurationService configServer;

	/**
	 * The registration management service
	 */
	private IRegistrationService registrationDbService;

	/**
	 * Spring Injection constructor
	 * 
	 * @param configServerService   the <code>IServerConfigurationService</code>
	 *                              bean to inject
	 * @param registrationDbService the <code>IRegistrationService</code> bean to
	 *                              inject
	 */
	public KpiServiceImpl(IServerConfigurationService configServerService, IRegistrationService registrationDbService) {
		this.configServer = configServerService;
		this.registrationDbService = registrationDbService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RobertServerKpi> computeKpi(LocalDate fromDate, LocalDate toDate) {
		List<Integer> epochsOfDayStart = new ArrayList<>();
		// Need to iterate to toDate + 1 to have the kpis for toDate
		for (LocalDate date = fromDate; date.isBefore(toDate.plusDays(2L)); date = date.plusDays(1L)) {
			epochsOfDayStart.add((int) TimeUtils.getEpochFromDate(date, configServer.getServiceTimeStart()));
		}

		// Retrieve list of kpi for each day
		List<Long> nbAlertedUsers = registrationDbService.countNbAlertedUsers(epochsOfDayStart);
		List<Long> nbExposedUsersNotAtRisk = registrationDbService.countNbExposedButNotAtRiskUsers(epochsOfDayStart);

		List<RobertServerKpi> result = new ArrayList<>(nbAlertedUsers.size());

		// Aggregate values into RobertServerKpi. Start at index 1 because index 0 is
		// the Kpis of the day before fromDate
		for (int i = 1; i < epochsOfDayStart.size(); i++) {
			result.add(new RobertServerKpi(
					TimeUtils.getDateFromEpoch(epochsOfDayStart.get(i), configServer.getServiceTimeStart()),
					nbAlertedUsers.get(i), nbExposedUsersNotAtRisk.get(i)));
		}

		return result;
	}

}
