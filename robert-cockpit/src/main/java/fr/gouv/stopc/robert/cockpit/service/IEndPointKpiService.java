package fr.gouv.stopc.robert.cockpit.service;

import java.time.LocalDate;
import java.util.List;

import fr.gouv.stopc.robert.cockpit.dto.EndPointKpi;

/**
 * 
 * @author plant-stopcovid
 *
 */
public interface IEndPointKpiService {

	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public List<EndPointKpi> getKpi(LocalDate fromDate, LocalDate toDate);

}
