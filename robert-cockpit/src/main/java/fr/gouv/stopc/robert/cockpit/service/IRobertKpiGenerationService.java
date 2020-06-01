package fr.gouv.stopc.robert.cockpit.service;

import java.time.LocalDateTime;
import java.util.List;

import fr.gouv.stopc.robert.cockpit.dto.StopCovidKpi;

/**
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
public interface IRobertKpiGenerationService {

	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @since 0.0.1-SNAPSHOT
	 */
	List<StopCovidKpi> computeKpis(LocalDateTime fromDate, LocalDateTime toDate);

}
